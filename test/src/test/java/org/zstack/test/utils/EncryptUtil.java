package org.zstack.test.utils;

import org.springframework.stereotype.Component;
import org.zstack.header.vo.DECRYPT;
import org.zstack.header.vo.ENCRYPT;

/**
 * Created by hhjuliet on 12/9/16.
 */
public class EncryptUtil {
	private String password;

	@DECRYPT
	public String getPassword() {
		return password;
	}

	public String getPassword(boolean encrypt){
		if(encrypt)
			return getPassword();
		else
			return password;
	}

	@ENCRYPT
	public void setPassword(String password) {
		this.password = password;
	}
}
