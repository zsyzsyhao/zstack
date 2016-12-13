package org.zstack.test.utils;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.componentloader.ComponentLoader;
import org.zstack.header.vo.DECRYPT;
import org.zstack.header.vo.ENCRYPT;
import org.zstack.test.BeanConstructor;
import org.zstack.utils.EncryptRSA;

/**
 * Created by mingjian.deng on 16/11/2.
 */
public class TestEncrypt {
    private String password;
    ComponentLoader loader;
    EncryptRSA rsa;

    @Before
    public void setUp() throws Exception {
        BeanConstructor con = new BeanConstructor();
        loader = con.build();
        rsa = loader.getComponent(EncryptRSA.class);
    }

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

        //bus = loader.getComponent(CloudBus.class);
        //EncryptUtil testEncrypt = new EncryptUtil();
        setPassword("pwd");
        //Assert.assertNotNull(getPassword());
        //Assert.assertTrue("pwd" == getPassword(true));
        //Assert.assertFalse("pwd" == getPassword(false));
    }
}
