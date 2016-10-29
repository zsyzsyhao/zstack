package org.zstack.header.vm;

import org.zstack.header.identity.Action;
import org.zstack.header.message.APIMessage;
import org.zstack.header.message.APIParam;

/**
 * Created by root on 10/29/16.
 */
@Action(category = VmInstanceConstant.ACTION_CATEGORY)
public class APISuspendVmInstanceMsg extends APIMessage implements VmInstanceMessage{
    @APIParam(resourceType = VmInstanceVO.class,checkAccount = true,operationTarget = true)
    private String vmInstanceUuid;

    @Override
    public String getVmInstanceUuid(){
        return vmInstanceUuid;
    }

    public void setVmInstanceUuid(String vmInstanceUuid){
        this.vmInstanceUuid = vmInstanceUuid;
    }
}
