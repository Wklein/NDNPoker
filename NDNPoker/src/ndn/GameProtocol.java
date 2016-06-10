package ndn;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.MetaInfo;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.util.Blob;
import poker.Deck;
import poker.GameState;
import poker.LocalState;
import poker.Player;
import util.SecUtil;

public class GameProtocol implements OnData, OnInterestCallback, OnRegisterFailed {
	
	private Interest pendingInterest;
	private Interest pendingInterest2;
	
	@Override
	public void onData(Interest arg0, Data arg1) {
		Name name = arg1.getName();
		String command = name.get(name.size()-1).toEscapedString();
//		System.out.println(command);
		
		switch(command){
		case "state":
//			System.out.println(arg1.getContent().toString());
			GameState newState = GameState.unmarshalBlob(arg1.getContent());
			LocalState.setGameState(newState);
			LocalState.getDisplay().displayGameState(newState);
			break;
		case "shuffle":
			String order = name.get(name.size()-2).toEscapedString();
//			System.out.println(name);
//			System.out.println(arg0.getName());
//			System.out.println();
			if(LocalState.isGameOwner()){
				try {
					LocalState.setDeck(Deck.unmarshal(arg1.getContent()));
					for(byte[] b: LocalState.getDeck())
						System.out.println(b[0]);
					Interest i;
					for(Player p: LocalState.getGameState().currentPlayers){
						i = new Interest();
						i.setName(new Name("/ndnpoker/" + LocalState.getGameState().id + "/protocol/" + p.id + "/forcerequestdeck"));
	//					System.out.println("Sending Deck");
						LocalState.getFace().expressInterest(i, this);
					}
					Thread.sleep(1000);
					i = new Interest();
					i.setName(new Name("/ndnpoker/" + LocalState.getGameState().id + "/protocol/" + LocalState.getGameState().currentPlayers.get(1).id + "/" + LocalState.id + "/" + "0" + "/draw"));
					LocalState.getFace().expressInterest(i, this);
					i = new Interest();
					i.setName(new Name("/ndnpoker/" + LocalState.getGameState().id + "/protocol/" + LocalState.getGameState().currentPlayers.get(1).id + "/" + LocalState.id + "/" + "1" + "/draw"));
					LocalState.getFace().expressInterest(i, this);
				
				} catch (JAXBException | IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				try{

					Deck deck = Deck.unmarshal(arg1.getContent());

//					List<byte[]> fullDeck = deck.cards;
//					
//					fullDeck = SecUtil.reEncryptDeck(fullDeck, LocalState.symKey);
//					fullDeck = (List<byte[]>) SecUtil.shuffleDeck(fullDeck);
					
					Blob blobA = Deck.marshal(deck);

					Data data = new Data();
					data.setName(pendingInterest.getName());
					data.setContent(blobA);
					LocalState.getFace().putData(data);
					
					Interest interest = new Interest();
					interest.setName(new Name("/ndnpoker/" + LocalState.getGameState().id + "/control/requestdeck"));
					interest.setInterestLifetimeMilliseconds(5000);
					LocalState.getFace().expressInterest(interest, this);

				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
			break;
		case "requestdeck":
			try {
				LocalState.setDeck(Deck.unmarshal(arg1.getContent()));
//				for(byte[] b: LocalState.getDeck())
//					System.out.println(b[0]);
				Interest i = new Interest();
				Name n = new Name("/ndnpoker/" + LocalState.getGameState().id + "/control/" + LocalState.id + "/requestdraw");
				i.setName(n);
				LocalState.getFace().expressInterest(i, this);
			} catch (JAXBException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "draw":
			int index = Integer.parseInt(name.get(name.size()-2).toEscapedString());
			long playerId = Long.parseLong(arg1.getName().get(arg1.getName().size() - 3).toEscapedString());
			System.out.println(arg1.getName());
			System.out.println(LocalState.id + " " + playerId);
			if(LocalState.id == playerId){
				byte[] card = arg1.getContent().getImmutableArray(); //SecUtil.decryptCard(LocalState.symKey, arg1.getContent().getImmutableArray());
				long cardValue = SecUtil.bytesToLong8(card);
				String s = new String(card);
				LocalState.drawCard((int)(cardValue % 52));
				System.out.println(cardValue % 52);
			}else{
				try{
					for(byte b: arg1.getContent().getImmutableArray())
						System.out.print(b + " ");
					byte[] card = arg1.getContent().getImmutableArray(); //SecUtil.decryptCard(LocalState.symKey, arg1.getContent().getImmutableArray());
					Data data = new Data();
					MetaInfo meta = new MetaInfo();
					meta.setFreshnessPeriod(100);
					data.setMetaInfo(meta);
					data.setContent(new Blob(card));
					if(index % 2 == 0){
						System.out.println(pendingInterest.getName());
						data.setName(pendingInterest.getName());
					}else{
						System.out.println(pendingInterest2.getName());
						data.setName(pendingInterest2.getName());
					}
					LocalState.getFace().putData(data);
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}
			break;
		case "requestdraw":
			try{
				System.out.println("Recieved draw order");
				byte[] indexes = arg1.getContent().getImmutableArray();
				Player nextPlayer = LocalState.getGameState().getNext(LocalState.id);
				
				long nextId = nextPlayer.id;
				
				Interest i = new Interest();
				i.setName(new Name("/ndnpoker/" + LocalState.getGameState().id + "/protocol/" + nextId + "/" + LocalState.id + "/" + indexes[0] + "/draw"));
				LocalState.getFace().expressInterest(i, this);
				i = new Interest();
				i.setName(new Name("/ndnpoker/" + LocalState.getGameState().id + "/protocol/" + nextId + "/" + LocalState.id + "/" + indexes[1] + "/draw"));
				LocalState.getFace().expressInterest(i, this);
			}catch(Exception e){
				e.printStackTrace();
			}
			break;
		case "refreshdeck":
			try{
				LocalState.setDeck(Deck.unmarshal(arg1.getContent()));
				long nextId = LocalState.getGameState().getNext(LocalState.id).id;
				
				Interest i = new Interest();
				i.setName(new Name("/ndnpoker/" + LocalState.getGameState().id + "/control/" + LocalState.id + "/requestdraw"));
				LocalState.getFace().expressInterest(i, this);
			}catch(Exception e){
				e.printStackTrace();
			}

		}
		
	}

	@Override
	public void onRegisterFailed(Name arg0) {
		System.out.println("Register Failed");
	}

	@Override
	public void onInterest(Name arg0, Interest arg1, Face arg2, long arg3, InterestFilter arg4) {
		
		Name name = arg1.getName();
		String command = name.get(name.size()-1).toEscapedString();
		System.out.println("Recieved Interest : " + arg1.getName());
		
		switch(command){
		case "requestinfo":
			Data data = new Data();
			MetaInfo meta = new MetaInfo();
			meta.setFreshnessPeriod(1000);
			try {
				data.setMetaInfo(meta);
				data.setName(arg1.getName());
				data.setContent(new Blob(LocalState.marshalPlayerState()));
				System.out.println("Sending Player Info");
				arg2.putData(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case "shuffle":
			String order = name.get(name.size()-2).toEscapedString();
			if(LocalState.isGameOwner()){
				try {

					data = new Data();
					data.setName(arg1.getName());
					
					Blob blob;
//					if(order.equals("0"))
//						blob = Deck.marshal(new Deck(LocalState.getDeck().subList(0, 26)));
//					else
						blob = Deck.marshal(new Deck(LocalState.getDeck()));
					System.out.println(blob.size() + " : " + Face.getMaxNdnPacketSize());
					data.setContent(blob);
					System.out.println("Sending Deck to " + data.getName());
					arg2.putData(data);
				} catch (JAXBException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				pendingInterest = arg1;
				LocalState.chainInterest(command, 1);
			}
			break;
		case "draw":
			String index = name.get(name.size()-2).toEscapedString();
			long playerId = Long.parseLong(name.get(name.size()-3).toEscapedString());
			System.out.println(LocalState.id + " " +  playerId);
			if(LocalState.id == playerId){
				try{
					data = new Data();
					meta = new MetaInfo();
					meta.setFreshnessPeriod(100);
					data.setMetaInfo(meta);
					data.setName(arg1.getName());
					System.out.println("Sending data for card : " + index);
					Blob blob = new Blob(LocalState.getDeck().get(Integer.parseInt(index)));
					data.setContent(blob);
					arg2.putData(data);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				System.out.println("Saving Interest : " + arg1.getName().toString());
				if(Integer.parseInt(index) % 2 == 0)
					pendingInterest = new Interest(arg1);
				else
					pendingInterest2 = new Interest(arg1);
				LocalState.chainInterest2(name, 1);
			}
			break;
		case "forcerequestdeck":
			try{
				Interest i = new Interest();
				i.setName(new Name("/ndnpoker/" + LocalState.getGameState().id + "/control/refreshdeck"));
				arg2.expressInterest(i, this);
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void shuffleAndPass(){
//		deck = (List<byte[]>)SecUtil.shuffleDeck(deck);
//		Player nextPlayer = gameState.getNext(id);
//		Interest i = new Interest();
//		i.setName(new Name("ndnpoker/" + gameState.id + "/protocol/" + nextPlayer.id + "/shuffle"));
//		try {
//			face.expressInterest(i, proto);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
