package poker;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import net.named_data.jndn.util.Blob;

@XmlRootElement
public class GameState {
	public long id;
	public List<Player> currentPlayers;
	public int turnCount;
	public String name;
	
	public int toCall;
	
	public List<Integer> revealedCards;
	
	public GameState(){}
	
	public GameState(String n){
		SecureRandom rand = new SecureRandom();
		id = rand.nextLong();
		
		currentPlayers = new ArrayList<Player>();
		turnCount = 0;
		name = n;
		toCall = 0;
		revealedCards = new ArrayList<Integer>();
	}
	
	public long addPlayer(String name, PublicKey key, byte[] encKey){
		SecureRandom rand = new SecureRandom();
		return addPlayer(name, key, rand.nextLong(), encKey);
	}
	
	public long addPlayer(String name, PublicKey key, long id, byte[] encKey){
		currentPlayers.add(new Player(id, name, 500, key, currentPlayers.size(), encKey));
		return id;
	}
	
	public long addPlayer(Player p){
		if(p == null) return -1;
		p.turn = currentPlayers.size();
		currentPlayers.add(p);
		for(Player p2: currentPlayers){
			System.out.println(p2.turn);
		}
		return p.id;
	}
	
	public Player getNext(long id){
		for(int i = 0; i < currentPlayers.size(); i++){
			if(id == currentPlayers.get(i).id)
				return currentPlayers.get((i + 1) % currentPlayers.size());
		}
		return null;
	}
	
	public static GameState unmarshalBlob(Blob blob){
		try{
			JAXBContext jc = JAXBContext.newInstance( GameState.class );
			Unmarshaller u = jc.createUnmarshaller();
			Object element = u.unmarshal(new StringReader(blob.toString()));
			if(element instanceof GameState)
				return (GameState)element;
			else
				return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
//	public static void main(String[] args) throws JAXBException, NoSuchAlgorithmException{
//		
//		
//		//marshalTest();
//	}
	
	/////////////////////////////////////////////////////////////////////
	//			TEST METHODS
	/////////////////////////////////////////////////////////////////////

	public static void marshalTest() throws JAXBException, NoSuchAlgorithmException{
		JAXBContext jc = JAXBContext.newInstance( GameState.class );
//		Unmarshaller u = jc.createUnmarshaller();
//		Object element = u.unmarshal( new File( "foo.xml" ) );
		Marshaller m = jc.createMarshaller();
		StringWriter writer = new StringWriter();
		
		GameState state = new GameState("Test Game");
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		KeyPair pair = gen.generateKeyPair();
//		state.addPlayer("Player 1", pair.getPublic());
//		state.addPlayer("Player 2", pair.getPublic());
//		state.addPlayer("Player 3", pair.getPublic());
//		state.addPlayer("Player 4", pair.getPublic());
//		state.addPlayer("Player 5", pair.getPublic());
		
		m.marshal(state, writer);
		
		System.out.println(writer.toString());
	}
	
	public void print(){
		System.out.println("id : " + id);
		System.out.println("turnCount : " + turnCount);
		System.out.println("name : " + name);
		System.out.println("toCall : " + toCall);
		System.out.println(currentPlayers);
		for(Player p: currentPlayers){
			p.print();
		}
	}
	
	public static void unmarshalTest() throws JAXBException{
		String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><gameState><id>5323268297931061386</id><currentPlayers><id>8223940465461820275</id><turn>0</turn><name>Player 1</name><currentCash>500</currentCash><currentBet>0</currentBet><pubKey>MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFR+/2h8qtaM5bX/2IlsLDCjVy31Abn02rsLnukw9F2f556O+fv4tt5ZmiNRF4Ph6PyQKl5sxWOmFLqwttnfN0nw2d8KdyOMrkXf66xh+XII+dcG5QEyHH0I8N+L96Ami8PsRAIhsfLhcS6w+FL8hg/bpHku6eVYRoEO5d2gBjYwIDAQAB</pubKey></currentPlayers><currentPlayers><id>5706718324996828397</id><turn>0</turn><name>Player 2</name><currentCash>500</currentCash><currentBet>0</currentBet><pubKey>MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFR+/2h8qtaM5bX/2IlsLDCjVy31Abn02rsLnukw9F2f556O+fv4tt5ZmiNRF4Ph6PyQKl5sxWOmFLqwttnfN0nw2d8KdyOMrkXf66xh+XII+dcG5QEyHH0I8N+L96Ami8PsRAIhsfLhcS6w+FL8hg/bpHku6eVYRoEO5d2gBjYwIDAQAB</pubKey></currentPlayers><currentPlayers><id>4420211370714733500</id><turn>0</turn><name>Player 3</name><currentCash>500</currentCash><currentBet>0</currentBet><pubKey>MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFR+/2h8qtaM5bX/2IlsLDCjVy31Abn02rsLnukw9F2f556O+fv4tt5ZmiNRF4Ph6PyQKl5sxWOmFLqwttnfN0nw2d8KdyOMrkXf66xh+XII+dcG5QEyHH0I8N+L96Ami8PsRAIhsfLhcS6w+FL8hg/bpHku6eVYRoEO5d2gBjYwIDAQAB</pubKey></currentPlayers><currentPlayers><id>-4589783260492074768</id><turn>0</turn><name>Player 4</name><currentCash>500</currentCash><currentBet>0</currentBet><pubKey>MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFR+/2h8qtaM5bX/2IlsLDCjVy31Abn02rsLnukw9F2f556O+fv4tt5ZmiNRF4Ph6PyQKl5sxWOmFLqwttnfN0nw2d8KdyOMrkXf66xh+XII+dcG5QEyHH0I8N+L96Ami8PsRAIhsfLhcS6w+FL8hg/bpHku6eVYRoEO5d2gBjYwIDAQAB</pubKey></currentPlayers><currentPlayers><id>360048794480463194</id><turn>0</turn><name>Player 5</name><currentCash>500</currentCash><currentBet>0</currentBet><pubKey>MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFR+/2h8qtaM5bX/2IlsLDCjVy31Abn02rsLnukw9F2f556O+fv4tt5ZmiNRF4Ph6PyQKl5sxWOmFLqwttnfN0nw2d8KdyOMrkXf66xh+XII+dcG5QEyHH0I8N+L96Ami8PsRAIhsfLhcS6w+FL8hg/bpHku6eVYRoEO5d2gBjYwIDAQAB</pubKey></currentPlayers><turnCount>0</turnCount><name>Test Game</name><toCall>0</toCall></gameState>";
		JAXBContext jc = JAXBContext.newInstance( GameState.class );
		Unmarshaller u = jc.createUnmarshaller();
		Object element = u.unmarshal(new StringReader(s));
		System.out.println(element.getClass().toString());
		
		if(element instanceof GameState){
			((GameState)element).print();
		}
	}
	
	
}
