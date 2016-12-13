package org.zstack.test.utils;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.componentloader.ComponentLoader;
import org.zstack.core.encrypt.PasswordEncrypt;
import org.zstack.header.vo.DECRYPT;
import org.zstack.header.vo.ENCRYPT;
import org.zstack.test.BeanConstructor;
import org.zstack.utils.EncryptRSA;

/**
 * Created by mingjian.deng on 16/11/2.
 */
public class TestEncrypt implements PasswordEncrypt {
    private String password;
    ComponentLoader loader;
    EncryptRSA rsa;

    @Before
    public void setUp() throws Exception {
        BeanConstructor con = new BeanConstructor();
        loader = con.build();
        rsa = loader.getComponent(EncryptRSA.class);
    }

    public String getPassword() {
        return password;
    }

    public String getPassword(boolean encrypt){
        if(encrypt)
            return getString();
        else
            return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    @ENCRYPT
    public void setString(String password){
        this.password = password;
    }

    @DECRYPT
    public String getString(){
        return password;
    }

    @Test
    public void test(){

        //bus = loader.getComponent(CloudBus.class);
        //EncryptUtil testEncrypt = new EncryptUtil();
        setString("pwd");
        String decreptPassword = getString();
        Assert.assertNotNull(decreptPassword);
        Assert.assertTrue("pwd".equals(decreptPassword));
        //Assert.assertFalse("pwd" == getPassword(false));
    }
}
