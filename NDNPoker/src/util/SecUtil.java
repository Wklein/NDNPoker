package util;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import net.named_data.jndn.util.Blob;

public class SecUtil {
	
	/*
	 * Shuffles a list in place.  This method is equivalent to removing a pseudorandom element from the original list
	 * and placing it at the front of a new list, repeating the process until all elements have been moved.
	 */
	public static <T> List<?> shuffleDeck(List<T> list){
		int length = list.size();
		SecureRandom rand = new SecureRandom();
		while(length > 0){
			int index = rand.nextInt(length);
			T tmp = list.get(index);
			list.set(index, list.get(length-1));
			list.set(length-1, tmp);
			length--;
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Long> drawDeck(){
		List<Long> deck = new ArrayList<Long>();
		SecureRandom rand = new SecureRandom();
		for(int i = 0; i < 52; i++){
			long val = Math.abs(rand.nextLong()) >> 6;
			val *= 52;
			deck.add(val + i);
//			System.out.println(val + i);
		}
		return deck;
	}
	
	public static List<byte[]> getPlainDeck(){
		try{
			List<Long> plainDeck = SecUtil.drawDeck();
			List<byte[]> pDeck = new ArrayList<byte[]>();
			for(int i = 0; i < plainDeck.size(); i++){
				Long tmp = plainDeck.get(i);
				pDeck.add(longToBytes8(tmp).clone());
			}
//			System.out.println("Card Length " + pDeck.get(0).length);
			return (List<byte[]>)shuffleDeck(pDeck);
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<byte[]> getEncryptedDeck(SecretKey key){
		try{
			List<Long> plainDeck = SecUtil.drawDeck();
			List<byte[]> eDeck = new ArrayList<byte[]>();
			Cipher c = Cipher.getInstance("DES");
			c.init(Cipher.ENCRYPT_MODE, key);
			for(int i = 0; i < plainDeck.size(); i++){
				Long tmp = plainDeck.get(i);
				eDeck.add(c.doFinal(String.valueOf(tmp).getBytes()));
			}
//			System.out.println("Card Length " + eDeck.get(0).length);
			return eDeck;
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<byte[]> reEncryptDeck(List<byte[]> deck, SecretKey key){
		try{
			Cipher c = Cipher.getInstance("DES");
			c.init(Cipher.ENCRYPT_MODE, key);
			for(int i = 0; i < deck.size(); i++){
//				String tmp = new String(deck.get(i));
//				System.out.println(tmp + " " + tmp.getBytes().length);
				deck.set(i, c.doFinal(deck.get(i)));
			}
			return deck;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] encryptSymKey(SecretKey key, KeyPair keyPair){
		try{
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
			byte[] encBytes = c.doFinal(key.getEncoded());
			return encBytes;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] decryptCard(SecretKey key, byte[] encCard){
		try{
			Cipher c = Cipher.getInstance("DES");
			c.init(Cipher.DECRYPT_MODE, key);
			byte[] decCard = c.doFinal(encCard);
			return decCard;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	private static ByteBuffer buffer = ByteBuffer.allocate(128);    

	private static ByteBuffer buffer2 = ByteBuffer.allocate(Long.BYTES);  

    public static byte[] longToBytes(long x) {
    	
        buffer.putLong(0, x);
//        buffer.putLong(8, 0);
//        for(byte b: buffer.array())
//        	System.out.print(b + " ");
//        System.out.println();
        return buffer.array();
    }
    
public static byte[] longToBytes8(long x) {
    	buffer2.clear();
        buffer2.putLong(0, x);
//        for(byte b: buffer2.array())
//        	System.out.print(b + " ");
//        System.out.println(" test");
        return buffer2.array();
    }

    public static long bytesToLong(byte[] bytes) {
    	buffer.clear();
//    	 for(byte b: bytes)
//         	System.out.print(b + " ");
//         System.out.println();
        buffer.put(bytes, 0, 16);
        buffer.flip();
//        for(byte b: buffer.array())
//        	System.out.print(b + " ");
        return buffer.getLong();
    }
    
    public static long bytesToLong8(byte[] bytes) {
    	buffer2.clear();
//    	 for(byte b: bytes)
//         	System.out.print(b + " ");
//         System.out.println();
        buffer2.put(bytes, 0, 8);
        buffer2.flip();
//        for(byte b: buffer.array())
//        	System.out.print(b + " ");
        return buffer2.getLong();
    }
	
	public static void main(String[] args) throws Exception{
		asymEncryptionTest();
		
	}
	
	private static void asymEncryptionTest() throws Exception{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		KeyPair key = keyGen.generateKeyPair();
		Cipher enc = Cipher.getInstance("RSA/ECB/NoPadding");
		enc.init(Cipher.ENCRYPT_MODE, key.getPublic());
		
		Cipher dec = Cipher.getInstance("RSA/ECB/NoPadding");
		dec.init(Cipher.DECRYPT_MODE, key.getPrivate());
		
		KeyPair key2 = keyGen.generateKeyPair();
		Cipher enc2 = Cipher.getInstance("RSA/ECB/NoPadding");
		enc2.init(Cipher.ENCRYPT_MODE, key2.getPublic());
		
		Cipher dec2 = Cipher.getInstance("RSA/ECB/NoPadding");
		dec2.init(Cipher.DECRYPT_MODE, key2.getPrivate());
		
		List<Long> deck = drawDeck();
//		for(Long i: deck){
//			System.out.println(i);
//		}
		
		Long l = deck.get(0);
		System.out.println("\n" + l);
		byte[] bytes = longToBytes(l);
		System.out.println(bytes);
		System.out.println(bytes.length);
		
		bytes = enc.doFinal(bytes);
		System.out.println(bytes.length);
		bytesToLong(bytes);
		bytes = enc2.doFinal(bytes);
		System.out.println(bytes.length);
		bytesToLong(bytes);
		Blob blob = new Blob(bytes);
		
		bytes = blob.getImmutableArray();
		System.out.println(bytes.length);
		bytesToLong(bytes);
		bytes = dec.doFinal(bytes);
		System.out.println(bytes.length);
		bytesToLong(bytes);
		bytes = dec2.doFinal(bytes);
		System.out.println(bytes.length);
		bytesToLong(bytes);
		System.out.println();
		l = bytesToLong(bytes);
		System.out.println(l);
	}
	
	private static void symEncryptionTest() throws Exception{
//		IvParameterSpec spec = new IvParameterSpec(longToBytes8(78234652837l));
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		SecretKey key = keyGen.generateKey();
		Cipher enc = Cipher.getInstance("AES/None/NoPadding");
		enc.init(Cipher.ENCRYPT_MODE, key);
		
		Cipher dec = Cipher.getInstance("AES/None/NoPadding");
		dec.init(Cipher.DECRYPT_MODE, key);
		
		SecretKey key2 = keyGen.generateKey();
		Cipher enc2 = Cipher.getInstance("AES/None/NoPadding");
		enc2.init(Cipher.ENCRYPT_MODE, key2);
		
		Cipher dec2 = Cipher.getInstance("AES/None/NoPadding");
		dec2.init(Cipher.DECRYPT_MODE, key2);
		
		List<Long> deck = drawDeck();
//		for(Long i: deck){
//			System.out.println(i);
//		}
		
		Long l = deck.get(0);
		System.out.println("\n" + l);
		byte[] bytes = longToBytes(l);
		System.out.println(bytes);
		System.out.println(bytes.length);
		
		bytes = enc.doFinal(bytes);
		System.out.println(bytes.length);
		bytes = enc2.doFinal(bytes);
		System.out.println(bytes.length);
		Blob blob = new Blob(bytes);
		
		bytes = blob.getImmutableArray();
		System.out.println(bytes.length);
		bytes = dec2.doFinal(bytes);
		System.out.println(bytes.length);
		bytes = dec.doFinal(bytes);
		System.out.println(bytes.length);
		
		l = bytesToLong(bytes);
		System.out.println(l);
	}
}
