package org.zstack.core.keystore;

import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.zstack.core.Platform;
import org.zstack.core.cloudbus.CloudBus;
import org.zstack.core.cloudbus.MessageSafe;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.core.db.SimpleQuery;
import org.zstack.core.thread.AsyncThread;
import org.zstack.core.thread.ThreadFacade;
import org.zstack.header.AbstractService;
import org.zstack.header.core.keystore.KeystoreInventory;
import org.zstack.header.core.keystore.KeystoreVO;
import org.zstack.header.core.keystore.KeystoreVO_;
import org.zstack.header.exception.CloudRuntimeException;
import org.zstack.header.identity.AccountType;
import org.zstack.header.identity.AccountVO;
import org.zstack.header.image.APIGetCandidateBackupStorageForCreatingImageReply;
import org.zstack.header.managementnode.ManagementNodeReadyExtensionPoint;
import org.zstack.header.message.APICreateMessage;
import org.zstack.header.message.APIMessage;
import org.zstack.header.message.Message;
import org.zstack.header.rest.RESTFacade;
import org.zstack.header.storage.backup.BackupStorageInventory;
import org.zstack.header.storage.backup.BackupStorageState;
import org.zstack.header.storage.backup.BackupStorageStatus;
import org.zstack.header.storage.backup.BackupStorageVO;
import org.zstack.utils.Utils;
import org.zstack.utils.logging.CLogger;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miao on 16-8-15.
 */
public class KeystoreManagerImpl extends AbstractService implements KeystoreManager, ManagementNodeReadyExtensionPoint {

    private static final CLogger logger = Utils.getLogger(KeystoreManagerImpl.class);

    @Autowired
    private CloudBus bus;
    @Autowired
    private ThreadFacade thdf;
    @Autowired
    private RESTFacade restf;
    @Autowired
    private DatabaseFacade dbf;

    @Override
    @MessageSafe
    public void handleMessage(Message msg) {
        if (msg instanceof APIMessage) {
            handleApiMessage((APIMessage) msg);
        } else {
            bus.dealWithUnknownMessage(msg);
        }
    }

    private void handleApiMessage(APIMessage msg) {
        if (msg instanceof APICreateIfNotExistKeystoreMsg) {
            handle((APICreateIfNotExistKeystoreMsg) msg);
        } else if (msg instanceof APICreateKeystoreMsg) {
            handle((APICreateKeystoreMsg) msg);
        } else if (msg instanceof APIDeleteKeystoreMsg) {
            handle((APIDeleteKeystoreMsg) msg);
        } else {
            bus.dealWithUnknownMessage(msg);
        }
    }

    private void handle(APICreateKeystoreMsg msg) {
        APICreateKeystoreReply reply = new APICreateKeystoreReply();

        KeystoreInventory ksinv = new KeystoreInventory();
        ksinv.setResourceUuid(msg.getResourceUuid());
        ksinv.setResourceType(msg.getResourceType());
        ksinv.setType(msg.getType());
        ksinv.setContent(msg.getContent());
        reply.setInventory(createOrUpdate(ksinv));

        bus.reply(msg, reply);
    }

    private void handle(APICreateIfNotExistKeystoreMsg msg) {
        APICreateKeystoreReply reply = new APICreateKeystoreReply();

        KeystoreInventory ksinv = new KeystoreInventory();
        ksinv.setResourceUuid(msg.getResourceUuid());
        ksinv.setResourceType(msg.getResourceType());
        ksinv.setType(msg.getType());
        ksinv.setContent(msg.getContent());
        reply.setInventory(createIfNotExist(ksinv));

        bus.reply(msg, reply);
    }

    private void handle(APIDeleteKeystoreMsg msg) {
        APIDeleteKeystoreEvent evt = new APIDeleteKeystoreEvent(msg.getId());
        delete(msg.getUuid());
        bus.publish(evt);
    }

    @Override
    public String getId() {
        return bus.makeLocalServiceId(KeystoreConstant.SERVICE_ID);
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @AsyncThread
    @Override
    public void managementNodeReady() {
        logger.debug(String.format("Management node[uuid:%s] joins, start KeystoreManager...",
                Platform.getManagementServerId()));
    }


    @Transactional
    public KeystoreInventory query(KeystoreInventory ksinv) {
        String sql = "select ksvo from KeystoreVO ksvo where ksvo.resourceUuid = :resourceUuid " +
                " and ksvo.resourceType = :resourceType and ksvo.type = :keystoreType";
        TypedQuery<KeystoreVO> q = dbf.getEntityManager().createQuery(sql, KeystoreVO.class);
        q.setParameter("resourceUuid", ksinv.getResourceUuid());
        q.setParameter("resourceType", ksinv.getResourceType());
        q.setParameter("keystoreType", ksinv.getType());
        List<KeystoreVO> keystoreVOList = q.getResultList();
        if (keystoreVOList.size() > 1) {
            throw new CloudRuntimeException("the size of Keystore Query Result should not be larger than one.");
        }
        if (keystoreVOList.size() == 1) {
            return KeystoreInventory.valueOf(q.getResultList().get(0));
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public KeystoreInventory createIfNotExist(KeystoreInventory ksinv) {
        KeystoreInventory ksinvR;
        SimpleQuery<KeystoreVO> q = dbf.createQuery(KeystoreVO.class);
        q.add(KeystoreVO_.resourceUuid, SimpleQuery.Op.EQ, ksinv.getResourceUuid());
        q.add(KeystoreVO_.resourceType, SimpleQuery.Op.EQ, ksinv.getResourceType());
        q.add(KeystoreVO_.type, SimpleQuery.Op.EQ, ksinv.getType());
        ksinvR = KeystoreInventory.valueOf(q.find());

        if (ksinvR == null) {
            return createOrUpdate(ksinv);
        } else {
            return ksinvR;
        }
    }

    @Transactional
    private KeystoreVO create(KeystoreInventory ksinv) {
        KeystoreVO vo = new KeystoreVO();
        vo.setUuid(Platform.getUuid());
        vo.setResourceUuid(ksinv.getResourceUuid());
        vo.setResourceType(ksinv.getResourceType());
        vo.setType(ksinv.getType());
        vo.setContent(ksinv.getContent());
        dbf.persistAndRefresh(vo);
        return vo;
    }

    @Transactional
    @Override
    public KeystoreInventory createOrUpdate(KeystoreInventory ksinv) {
        KeystoreVO voR = create(ksinv);
        if (voR != null) {
            return KeystoreInventory.valueOf(voR);
        } else {
            throw new CloudRuntimeException("failed to create or update keystore");
        }
    }

    @Transactional
    @Override
    public void delete(String uuid) {
        dbf.removeByPrimaryKey(uuid, KeystoreVO.class);
    }

    @Transactional
    @Override
    public void delete(KeystoreInventory ksinv) {
    }


}
