package org.zstack.header.vm;

import org.zstack.header.identity.Action;
import org.zstack.header.message.APIMessage;
import org.zstack.header.message.APIParam;

/**
 * Created by root on 11/2/16.
 */
@Action(category = VmInstanceConstant.ACTION_CATEGORY)
public class APIResumeVmInstanceMsg extends APIMessage implements VmInstanceMessage{
    @APIParam
    private String vmInstanceUuid;

    @Override
    public String getVmInstanceUuid(){
        return vmInstanceUuid;
    }

    public String setVmInstanceUuid(String vmInstanceUuid){
        return this.vmInstanceUuid = vmInstanceUuid;
    }
}
