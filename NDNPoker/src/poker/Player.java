package poker;

import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.named_data.jndn.util.Blob;

@XmlRootElement
public class Player {
	public long id;
	public int turn;
	public String name;
	
	public int currentCash;
	public int currentBet;
	@XmlJavaTypeAdapter(PublicKeyAdapter.class)
	public PublicKey pubKey;
	public byte[] encKey;
	
	public Player(){}
	
	public Player(long i, String n, int cash, PublicKey key, int turn, byte[] eKey){
		id = i;
		name = n;
		currentCash = cash;
		this.turn = turn;
		pubKey = key;
		encKey = eKey;
	}
	
	public void print(){
		System.out.println("id : " + id);
		System.out.println("turn : " + turn);
		System.out.println("name : " + name);
		System.out.println("currentCash : " + currentCash);
		System.out.println("Key : " + pubKey.toString());
	}
	
	private static class PublicKeyAdapter extends XmlAdapter< byte[], PublicKey>{

		@Override
		public byte[] marshal(PublicKey v) throws Exception {
			return v.getEncoded();
		}

		@Override
		public PublicKey unmarshal(byte[] v) throws Exception {
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(v));
		}
	}
	
	public static Player unmarshalBlob(Blob blob){
		try{
			JAXBContext jc = JAXBContext.newInstance( Player.class );
			Unmarshaller u = jc.createUnmarshaller();
			Object element = u.unmarshal(new StringReader(blob.toString()));
			if(element instanceof Player)
				return (Player)element;
			else
				return null;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
		
}
