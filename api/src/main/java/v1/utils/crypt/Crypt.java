package v1.utils.crypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Crypt {

	public static String base64encode(String text) {
		try {
			BASE64Encoder enc = new BASE64Encoder();
			String rez = enc.encode(text.getBytes("UTF-8"));
			return rez;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static String base64decode(String text) {
		try {
			BASE64Decoder dec = new BASE64Decoder();
			return new String(dec.decodeBuffer(text), "UTF-8");
		} catch (IOException e) {
			return null;
		}
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] sha1hash = new byte[40];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		sha1hash = md.digest();
		return convertToHex(sha1hash);
	}

	public static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md;
		md = MessageDigest.getInstance("MD5");
		md.update(text.getBytes());
		byte byteData[] = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public static String generateHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		UUID newUUID = UUID.randomUUID();
		String pwd = MD5(newUUID.toString());
		pwd = pwd.substring(0, 25);
		return pwd;
	}

}
