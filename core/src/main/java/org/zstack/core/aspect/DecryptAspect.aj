package org.zstack.core.aspect;

import org.apache.commons.codec.binary.Base64;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.zstack.core.thread.SyncThreadSignature;
import org.zstack.utils.Utils;
import org.zstack.utils.logging.CLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.zstack.utils.EncryptRSA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;


public aspect DecryptAspect {
	private static final CLogger logger = Utils.getLogger(DecryptAspect.class);

	Object around(): execution(@org.zstack.header.vo.DECRYPT * *(..)){
		Object value = proceed();
		if (value != null){
			try{
				logger.debug(String.format("decrypted password is: %s", value));
				value = (String) decrypt((String) value);
			}catch(Exception e){
				logger.debug(String.format("decrypt aspectj is error..."));
				logger.debug(e.getMessage());
				e.printStackTrace();
			}

		}

		return value;
	}

	private Object decrypt(String password) throws NoSuchAlgorithmException,
			IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
			NoSuchPaddingException, IOException, ClassNotFoundException {
		EncryptRSA rsa = new EncryptRSA();
		RSAPrivateKey privateKey = rsa.getPrivateKey();
		byte[] srcBytes = password.getBytes("utf-8");
		byte[] desBytes = rsa.decrypt(privateKey, Base64.decodeBase64(srcBytes));
		return new String(desBytes, "utf-8");
	}


}