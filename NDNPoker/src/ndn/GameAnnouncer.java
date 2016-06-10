package ndn;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.MetaInfo;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.util.Blob;
import poker.LocalState;

public class GameAnnouncer implements OnRegisterFailed, OnInterestCallback {
	@Override
	public void onRegisterFailed(Name name) {
		System.out.println("Register Failed");
	}

	@Override
	public void onInterest(Name name, Interest interest, Face face, long arg3, InterestFilter filter) {
		if(interest.getExclude().matches(new Name.Component(String.valueOf(LocalState.getGameState().id)))) return;
		System.out.println("Recieved Interest " + interest.getName().toString());
		Data data = new Data();
		MetaInfo meta = new MetaInfo();
		meta.setFreshnessPeriod(1000);
		try {
			data.setMetaInfo(meta);
			data.setName(interest.getName().append( String.valueOf(LocalState.getGameState().id) ));
			data.setContent(new Blob(LocalState.marshalGameState()));
			System.out.println("Sending State");
			face.putData(data);
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}
		
	}
}
