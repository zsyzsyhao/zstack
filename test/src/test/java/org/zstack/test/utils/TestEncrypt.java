package org.zstack.test.utils;

import junit.framework.Assert;
import org.junit.Test;
import org.zstack.header.vo.DECRYPT;
import org.zstack.header.vo.ENCRYPT;

/**
 * Created by mingjian.deng on 16/11/2.
 */
public class TestEncrypt {
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

    @Test
    public void test(){
        //EncryptUtil testEncrypt = new EncryptUtil();
        setPassword("pwd");
        Assert.assertNotNull(getPassword());
        //Assert.assertTrue("pwd" == getPassword(true));
        //Assert.assertFalse("pwd" == getPassword(false));
    }
}
