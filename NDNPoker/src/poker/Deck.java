package poker;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import net.named_data.jndn.util.Blob;

@XmlRootElement
public class Deck {
	public List<byte[]> cards;
	
	public Deck(){
		cards = new ArrayList<byte[]>();
	}
	
	public Deck(List<byte[]> c){
		cards = c;
	}
	
	public static Deck unmarshal(Blob blob) throws JAXBException{
		JAXBContext jc = JAXBContext.newInstance(Deck.class);
		Unmarshaller u = jc.createUnmarshaller();
		Deck deck = (Deck)u.unmarshal(new StringReader(blob.toString())); 
		return deck;
	}
	
	public static Blob marshal(Deck deck) throws JAXBException{
		JAXBContext jc = JAXBContext.newInstance(Deck.class);
		Marshaller m = jc.createMarshaller();
		StringWriter writer = new StringWriter();
		m.marshal(deck, writer);
		return new Blob(writer.toString());
	}
}
