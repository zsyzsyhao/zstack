package org.zstack.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.zstack.core.cloudbus.ResourceDestinationMaker;
import org.zstack.core.db.DatabaseFacade;
import org.zstack.core.db.SimpleQuery;
import org.zstack.core.db.UpdateQuery;
import org.zstack.header.exception.CloudRuntimeException;
import org.zstack.header.message.APIEvent;
import org.zstack.header.message.APIMessage;
import org.zstack.utils.gson.JSONObjectUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.zstack.utils.CollectionDSL.e;
import static org.zstack.utils.CollectionDSL.map;

/**
 * Created by xing5 on 2016/12/8.
 */
public class MysqlAsyncRestStore implements AsyncRestApiStore {
    @Autowired
    private DatabaseFacade dbf;
    @Autowired
    private ResourceDestinationMaker destinationMaker;

    private ConcurrentHashMap<String, APIEvent> results = new ConcurrentHashMap<>();

    @Override
    public String save(APIMessage msg) {
        AsyncRestVO vo = new AsyncRestVO();
        vo.setUuid(msg.getId());
        vo.setApiMessage(JSONObjectUtil.toJsonString(map(e(msg.getClass().getName(), msg))));
        vo.setState(AsyncRestState.processing);
        dbf.persist(vo);

        return vo.getUuid();
    }

    @Override
    public void complete(APIEvent evt) {
        if (destinationMaker.isManagedByUs(evt.getApiId())) {
            UpdateQuery q = UpdateQuery.New();
            q.entity(AsyncRestVO.class);
            q.condAnd(AsyncRestVO_.uuid, SimpleQuery.Op.EQ, evt.getApiId());
            q.set(AsyncRestVO_.result, JSONObjectUtil.toJsonString(evt));
            q.set(AsyncRestVO_.state, AsyncRestState.done);
            q.update();
        }

        results.put(evt.getApiId(), evt);
    }

    @Override
    public AsyncRestQueryResult query(String uuid) {
        AsyncRestQueryResult result = new AsyncRestQueryResult();
        result.setUuid(uuid);

        APIEvent evt = results.get(uuid);
        if (evt != null) {
            result.setState(AsyncRestState.done);
            result.setResult(evt);
            return result;
        }

        AsyncRestVO vo = dbf.findByUuid(uuid, AsyncRestVO.class);
        if (vo == null) {
            result.setState(AsyncRestState.expired);
            return result;
        }

        if (vo.getState() != AsyncRestState.done) {
            result.setState(vo.getState());
            return result;
        }

        try {
            Map m = JSONObjectUtil.toObject(vo.getResult(), LinkedHashMap.class);
            String apiEventName = (String) m.keySet().iterator().next();
            Class<APIEvent> apiEventClass = (Class<APIEvent>) Class.forName(apiEventName);
            evt = JSONObjectUtil.rehashObject(m.get(apiEventName), apiEventClass);
            result.setState(AsyncRestState.done);
            result.setResult(evt);

            results.put(uuid, evt);
        } catch (Exception e) {
            throw new CloudRuntimeException(e);
        }

        return result;
    }
}
