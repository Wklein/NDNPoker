package ndn;

import java.io.IOException;

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
import net.named_data.jndn.OnTimeout;
import net.named_data.jndn.util.Blob;
import poker.Deck;
import poker.LocalState;
import poker.Player;

public class GameControl implements OnInterestCallback, OnTimeout, OnRegisterFailed, OnData {



	@Override
	public void onRegisterFailed(Name arg0) {
		System.out.println("Register Failed");
	}

	@Override
	public void onInterest(Name arg0, Interest arg1, Face arg2, long arg3, InterestFilter arg4) {
//		System.out.println("Recieved Control Interest " + arg1.getName());
		Name name = arg1.getName();
		String command = name.get(name.size()-1).toEscapedString();
		String id = name.get(name.size() - 2).toEscapedString();
//		System.out.println(command + " " + id);
		switch(command){
			case "join":
				Interest i = new Interest();
				i.setMustBeFresh(true);
				i.setInterestLifetimeMilliseconds(2000);
				i.setName(new Name("/ndnpoker/" + LocalState.getGameState().id + "/protocol/" + id + "/requestinfo"));
				try {
					System.out.println("Sending Interest " + i.getName());
					arg2.expressInterest(i, this);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case "state":
				try {
					Data d = new Data();
					MetaInfo meta = new MetaInfo();
					meta.setFreshnessPeriod(1000);
					d.setMetaInfo(meta);
					d.setContent(new Blob(LocalState.marshalGameState()));
					d.setName(arg1.getName());
//					System.out.println("Sending State to " + arg1.getName().toString());
					arg2.putData(d);
				} catch (JAXBException | IOException e) {
					e.printStackTrace();
				}
				break;
			case "refreshdeck":
				try {
					Data d = new Data();
					MetaInfo meta = new MetaInfo();
					meta.setFreshnessPeriod(10000);
					d.setMetaInfo(meta);
					d.setContent(new Blob(Deck.marshal(new Deck(LocalState.getDeck()))));
					d.setName(arg1.getName());
//					System.out.println("Sending State to " + arg1.getName().toString());
					arg2.putData(d);
				} catch (JAXBException | IOException e) {
					e.printStackTrace();
				}
				break;
			case "requestdraw":
				long playerId = Long.parseLong(arg1.getName().get(arg1.getName().size() - 2).toEscapedString());
				int playerOrder = -1;
				for(Player p: LocalState.getGameState().currentPlayers){
					System.out.println(playerId + " " + p.id);
					if(playerId == p.id)
						playerOrder = p.turn;
					System.out.println("Order : " + playerOrder);
				}
				byte[] bytes = new byte[2];
				bytes[0] = (byte)(playerOrder * 2);
				bytes[1] = (byte)(playerOrder * 2 + 1);
				Data d = new Data();
				d.setContent(new Blob(bytes));
				d.setName(arg1.getName());
			try {
				arg2.putData(d);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				break;
				
		}	
	}

	@Override
	public void onData(Interest arg0, Data arg1) {
		System.out.println("Recieved Data : " + arg1.getContent().toString());
		Player p = Player.unmarshalBlob(arg1.getContent());
		LocalState.getGameState().addPlayer(p);
		LocalState.getDisplay().displayGameState(LocalState.getGameState());
	}

	@Override
	public void onTimeout(Interest arg0) {
		System.out.println(arg0.getName().toString() + " Timed out");
		
	}

}
