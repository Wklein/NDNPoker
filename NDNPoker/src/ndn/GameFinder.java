package ndn;

import java.io.IOException;

import net.named_data.jndn.Data;
import net.named_data.jndn.Exclude;
import net.named_data.jndn.Interest;
import net.named_data.jndn.OnData;
import poker.GameState;
import poker.LocalState;

public class GameFinder implements OnData {

	@Override
	public void onData(Interest arg0, Data arg1) {
		System.out.println(arg1.getContent().toString());
		Exclude ex = arg0.getExclude();
		if(ex == null)
			ex = new Exclude();
		System.out.println(arg1.getName().get(arg1.getName().size()-1).toEscapedString());
		ex.appendComponent(arg1.getName().get(arg1.getName().size()-1));
		System.out.println(ex.toUri());
		arg0.setExclude(ex);
		

		LocalState.getDisplay().listGame(GameState.unmarshalBlob(arg1.getContent()));
//		LocalState.exitLoop();
		
//		try {
//			LocalState.findGames(arg0);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
	}

}
