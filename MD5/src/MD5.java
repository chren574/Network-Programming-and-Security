/**
 * 
 */
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
/**
 * @author Christoffer
 *
 */
public class MD5 {

	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		
		// username:passwd
		// easily:broken
		String plaintext = "easily:broken";
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(plaintext.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 ){
		  hashtext = "0"+hashtext;
		}
		
		System.out.println("HASH: + " + hashtext);
		System.out.println("FACI: + " + "0088eb08f098c52321e34bb4248b383e");
		System.out.println("EQUEL? " + hashtext.equals("0088eb08f098c52321e34bb4248b383e"));
	}

}
