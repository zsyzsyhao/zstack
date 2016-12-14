package org.zstack.utils;



import java.security.NoSuchAlgorithmException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.nio.charset.Charset;
import org.zstack.utils.logging.CLogger;


/**
 * Created by mingjian.deng on 16/11/1.
 */
public class EncryptRSA {
	private static byte[] key1;
	private static Key key2;
	private static final String KEY_ALGORITHM = "AES";
	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
	private static final CLogger logger = Utils.getLogger(EncryptRSA.class);
	private final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	static{
		if (key1 == null && key2 == null){
			initSecretKey();
		}
	}

	public static void initSecretKey() {
		KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.debug(e.getMessage());
			e.printStackTrace();
		}
		//init keygenerator's size
		kg.init(128);
		//generator a key
		SecretKey  secretKey = kg.generateKey();
		key1 = secretKey.getEncoded();
		key2 = toKey(key1);
	}


	private static Key toKey(byte[] key){
		return new SecretKeySpec(key, KEY_ALGORITHM);
	}


	public  byte[] encrypt(byte[] data,Key key) throws Exception{
		return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
	}


	public  byte[] encrypt(byte[] data,byte[] key) throws Exception{
		return encrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
	}


	public  byte[] encrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{
		Key k = toKey(key);
		return encrypt(data, k, cipherAlgorithm);
	}

	public  byte[] encrypt(byte[] data,Key key,String cipherAlgorithm) throws Exception{
		//init
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);
	}

	public  byte[] decrypt(byte[] data,byte[] key) throws Exception{
		return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
	}

	public  byte[] decrypt(byte[] data,Key key) throws Exception{
		return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);
	}

	public  byte[] decrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception{
		Key k = toKey(key);
		return decrypt(data, k, cipherAlgorithm);
	}

	public  byte[] decrypt(byte[] data,Key key,String cipherAlgorithm) throws Exception{
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(data);
	}

	private String  showByteArray(byte[] data){
		if(null == data){
			return null;
		}
		StringBuilder sb = new StringBuilder("{");
		for(byte b:data){
			sb.append(b).append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("}");
		return sb.toString();
	}

	private String decodeUTF8(byte[] bytes) {
		return new String(bytes, UTF8_CHARSET);
	}

	private byte[] encodeUTF8(String string) {
		return string.getBytes(UTF8_CHARSET);
	}


	public String encrypt1(String password) throws Exception{

		System.out.println("key: "+showByteArray(key1));
		System.out.println("加密前数据 string: "+password);
		System.out.println("加密前数据： byte[]: "+showByteArray(password.getBytes()));

		System.out.println("");

		byte[] encryptData = encrypt(password.getBytes(),key2);
		System.out.println("加密后数据： "+showByteArray(encryptData));
		//System.out.println("加密后数据： "+ Hex.encode(encryptData));

		System.out.println("加密后的数据是： "+decodeUTF8(encryptData));
		return decodeUTF8(encryptData);

	}

	public Object decrypt1(String password) throws Exception{

		byte[] srcBytes = encodeUTF8(password);
		byte[] desBytes = decrypt(srcBytes, key2);

		System.out.println("解密后数据: byte[]:"+showByteArray(desBytes));
		System.out.println("解密后数据: string:"+new String(desBytes));

		return decodeUTF8(desBytes);
	}



}

