package org.zstack.test.core.keystore;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.componentloader.ComponentLoader;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.core.keystore.KeystoreManager;
import org.zstack.header.core.keystore.KeystoreInventory;
import org.zstack.header.core.keystore.KeystoreVO;
import org.zstack.test.Api;
import org.zstack.test.ApiSenderException;
import org.zstack.test.DBUtil;
import org.zstack.test.deployer.Deployer;
import org.zstack.utils.Utils;
import org.zstack.utils.logging.CLogger;

public class TestKeystore {
    CLogger logger = Utils.getLogger(TestKeystore.class);
    Deployer deployer;
    Api api;
    ComponentLoader loader;
    CloudBus bus;
    DatabaseFacade dbf;
    KeystoreManager ks;

    @Before
    public void setUp() throws Exception {
        DBUtil.reDeployDB();
        deployer = new Deployer("deployerXml/keystore/TestKeystore.xml");
        deployer.addSpringConfig("KeystoreManager.xml");
        deployer.build();
        api = deployer.getApi();
        loader = deployer.getComponentLoader();
        bus = loader.getComponent(CloudBus.class);
        dbf = loader.getComponent(DatabaseFacade.class);
        ks = loader.getComponent(KeystoreManager.class);


    }

    @Test
    public void test() throws ApiSenderException {
        KeystoreInventory ksinv1 = new KeystoreInventory();
        ksinv1.setResourceUuid("001");
        ksinv1.setResourceType("xxxVO");
        ksinv1.setType("Password");
        ksinv1.setContent("miao");
        logger.debug(ksinv1.getResourceUuid());
        KeystoreInventory ksinv2 = ks.createIfNotExist(ksinv1);

        KeystoreVO ksvo1 = dbf.findByUuid(ksinv2.getUuid(), KeystoreVO.class);
        Assert.assertNotNull(ksvo1);
    }

}
