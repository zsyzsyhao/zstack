package org.zstack.test.compute.zone;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zstack.core.componentloader.ComponentLoader;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.header.exception.CloudRuntimeException;
import org.zstack.header.rest.RESTFacade;
import org.zstack.sdk.*;
import org.zstack.test.Api;
import org.zstack.test.ApiSenderException;
import org.zstack.test.DBUtil;
import org.zstack.test.WebBeanConstructor;
import org.zstack.utils.Utils;
import org.zstack.utils.gson.JSONObjectUtil;
import org.zstack.utils.logging.CLogger;

public class TestCreateZoneRest1 {
    CLogger logger = Utils.getLogger(TestCreateZoneRest1.class);

    Api api;
    ComponentLoader loader;
    DatabaseFacade dbf;
    RESTFacade restf;

    @Before
    public void setUp() throws Exception {
        DBUtil.reDeployDB();
        WebBeanConstructor con = new WebBeanConstructor();
        /* This loads spring application context */
        loader = con.addXml("PortalForUnitTest.xml").addXml("ZoneManager.xml")
                .addXml("AccountManager.xml").addXml("rest.xml").build();
        dbf = loader.getComponent(DatabaseFacade.class);
        restf = loader.getComponent(RESTFacade.class);
        api = new Api();
        api.startServer();
    }

    @Test
    public void test() throws ApiSenderException, InterruptedException {
        api.loginAsAdmin();

        ZSClient.configure(
                new ZSConfig.Builder()
                .setHostname("127.0.0.1")
                .setPort(8989)
                .build()
        );

        CreateZoneAction action = new CreateZoneAction();
        action.sessionId = api.getAdminSession().getUuid();
        action.name = "zone1";
        CreateZoneAction.Result res = action.call();
        if (res.error != null) {
            throw new CloudRuntimeException(JSONObjectUtil.toJsonString(res.error));
        }

        logger.debug(JSONObjectUtil.toJsonString(res.value));

        QueryZoneAction qaction = new QueryZoneAction();
        qaction.sessionId = api.getAdminSession().getUuid();
        qaction.conditions.add("name=zone1");
        QueryZoneAction.Result r = qaction.call();
        Assert.assertNull(r.error);

        QueryZoneResult qr = r.value;
        Assert.assertEquals(1, qr.inventories.size());
        logger.debug(JSONObjectUtil.toJsonString(r));
    }
}
