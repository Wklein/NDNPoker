package poker;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import gui.Display;
import ndn.GameAnnouncer;
import ndn.GameControl;
import ndn.GameFinder;
import ndn.GameProtocol;
import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnRegisterSuccess;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.KeyType;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.security.policy.SelfVerifyPolicyManager;
import net.named_data.jndn.util.Blob;
import util.KeyDer;
import util.SecUtil;

public class LocalState {
	private static JAXBContext jc; 
	
	public static long id;
	private static KeyPair keyPair;
	public static SecretKey symKey;
	private static Face face;
	private static GameState gameState;
	private static String name = "Player 1";
	
	private static GameControl control;
	private static GameAnnouncer disc;
	private static GameProtocol proto;
	private static GameFinder finder;
	private static long time;
	private static boolean loopFlag = true;
	private static boolean isGameOwner = true;
	
	public static List<Integer> localCards;
	
	private static List<byte[]> deck;
	
	private static Display display;
	
	public static void drawCard(int card){
		System.out.println("Adding Card");
		localCards.add(card);
		display.getCardTable().addMyCard(card);
		display.repaint();
	}
	
	public static KeyPair getKeyPair(){
		return keyPair;
	}
	public static boolean isGameOwner(){
		return isGameOwner;
	}
	public static Face getFace(){
		return face;
	}
	public static void exitLoop(){
		loopFlag = false;
	}
	public static List<byte[]> getDeck(){
		return deck;
	}
	public static void setDeck(Deck d){
		deck = d.cards;
	}
	private static void init() throws SecurityException, JAXBException{
		deck = new ArrayList<byte[]>();
		localCards = new ArrayList<Integer>();
		jc = JAXBContext.newInstance( GameState.class );
		
		SecureRandom rand = new SecureRandom();
		id = rand.nextLong();
		
		face = new Face();
		
		control = new GameControl();
		disc = new GameAnnouncer();
		proto = new GameProtocol();
		finder = new GameFinder();
		
		MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
	    MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
	    KeyChain keyChain = new KeyChain(new IdentityManager(identityStorage, privateKeyStorage), new SelfVerifyPolicyManager(identityStorage));
	    keyChain.setFace(face);
	    
	    Name keyName = new Name("/ndnpoker/playerKey/" + id);
	    Name certificateName = keyName.getSubName(0, keyName.size() - 1).append("KEY").append(keyName.get(-1)).append("ID-CERT").append("0");
		
	    identityStorage.addKey(keyName, KeyType.RSA, new Blob(KeyDer.PUBLIC, false));
	    privateKeyStorage.setKeyPairForKeyName(keyName, KeyType.RSA, KeyDer.PUBLIC, KeyDer.PRIVATE);
	    face.setCommandSigningInfo(keyChain, certificateName);
		
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPair = keyPairGen.generateKeyPair();
			
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			symKey = keyGen.generateKey();
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			LocalState.processLoop();
		} catch (IOException | EncodingException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void findGames(Interest i) throws IOException{
		if(i == null){
			i = new Interest();
			i.setMustBeFresh(true);
			i.setName(new Name("/ndnpoker/discover"));
		}
		System.out.println("Sending Interest to " + i.getName().toString());
		face.expressInterest(i, finder);
	}
	
	public static void createGame(String n){

		gameState = new GameState(n);
		gameState.addPlayer(name, keyPair.getPublic(), id, SecUtil.encryptSymKey(symKey, keyPair));
		display.displayGameState(gameState);
		try {
			registerGameControl(gameState.id);
			registerGameAnnouncer(gameState.id);
			registerGameProtocol(gameState.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void joinGame(GameState game) throws IOException, EncodingException, InterruptedException{
		isGameOwner = false;
		gameState = game;
		
		try {
			registerGameProtocol(game.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void startGame(){
		gameState.turnCount = 0;
		System.out.println("getting deck");
		deck = SecUtil.getPlainDeck();
		for(byte[] b: deck){
			System.out.println(SecUtil.bytesToLong8(b) % 52);
		}
		chainInterest("shuffle", 1);
	}
	
	public static void chainInterest(String command, int split){
		Player nextPlayer = gameState.getNext(id);

		try {
			for(int i = 0; i < split; i++){
				Interest interest = new Interest();
				interest.setInterestLifetimeMilliseconds(10000);
				Name name = new Name("ndnpoker/" + gameState.id + "/protocol/" + nextPlayer.id + "/" + i + "/" + command);
				System.out.println("Sending Interest : " + name.toString());
				interest.setName(name);
				face.expressInterest(interest, proto);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void chainInterest2(Name n, int split){
		Player nextPlayer = gameState.getNext(id);

		try {
			for(int i = 0; i < split; i++){
				Interest interest = new Interest();
				interest.setInterestLifetimeMilliseconds(10000);
				interest.setMustBeFresh(true);
				n.set(n.toString().replace(String.valueOf(LocalState.id), String.valueOf(nextPlayer.id)));
				System.out.println("Sending Interest : " + n.toString());
				interest.setName(n);
				face.expressInterest(interest, proto);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String marshalPlayerState(){
		Player p = new Player(id, name, 500, keyPair.getPublic(), -1, SecUtil.encryptSymKey(symKey, keyPair));
		return marshalPlayerState(p);
	}
	
	public static String marshalPlayerState(Player player){
		StringWriter s = new StringWriter();
		Marshaller m;
		try {
			JAXBContext tmpJc = JAXBContext.newInstance( Player.class );
			m = tmpJc.createMarshaller();
			m.marshal(player, s);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s.toString();
	}

	public static void registerGameControl(long gameId) throws IOException, SecurityException{
		Name prefix = new Name("/ndnpoker/" + gameId + "/control");
		face.registerPrefix(prefix, control, control);
	}
	
	public static void registerGameAnnouncer(long gameId) throws IOException, SecurityException{
		Name prefix = new Name("/ndnpoker/discover/");
		face.registerPrefix(prefix, disc, disc);
	}
	
	public static void registerGameProtocol(long gameId) throws IOException, SecurityException{
		Name prefix = new Name("/ndnpoker/" + gameId + "/protocol/" + id);
		System.out.println("Registering protocol prefix " + prefix.toString());
		face.registerPrefix(prefix, proto, proto, new OnRegisterSuccess() {
			
			@Override
			public void onRegisterSuccess(Name arg0, long arg1) {
				Interest i = new Interest();
				i.setMustBeFresh(true);
				i.setName(new Name("/ndnpoker/" + gameState.id + "/control/" + id + "/join"));
				System.out.println("Sending Interest " + i.getName().toString());
				try {
					face.expressInterest(i, finder);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
//		System.out.println("Registered");
	}

	public static GameState getGameState(){
		return gameState;
	}
	
	public static String marshalGameState() throws JAXBException{
		StringWriter s = new StringWriter();
		Marshaller m = jc.createMarshaller();
		m.marshal(gameState, s);
		return s.toString();
	}
	
	public static void processLoop() throws IOException, EncodingException, InterruptedException{
		Thread thread = new Thread(new ProcessLoop());
		System.out.println("Starting Process Loop");
		thread.start();
	}
	
	public static class ProcessLoop implements Runnable{
		@Override
		public void run() {
			loopFlag = true;
			while(loopFlag){
				try {
					face.processEvents();
					if(!isGameOwner && gameState != null){
						Interest stateInterest = new Interest();
						stateInterest.setName(new Name("/ndnpoker/" + gameState.id + "/control/" + id + "/state"));
						face.expressInterest(stateInterest, proto);
					}
					Thread.sleep(50);
				} catch (IOException | EncodingException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////
	//							TESTS
	////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) throws Exception{
		display = new Display();
		display.init();
		init();
		
//		registerGameProtocol(2);
//		
//		processLoop();
		
//		createGame("My Game Bitches");
//		findGames(null);
		
	}

	public static Display getDisplay() {
		return display;
	}

	public static void setGameState(GameState gameState) {
		LocalState.gameState = gameState;
	}
	
}
