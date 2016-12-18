package org.zstack.header.network.l3;

import org.zstack.header.message.APIReply;
import org.zstack.header.rest.RestResponse;

import java.util.List;

/**
 * Created by frank on 6/15/2015.
 */
@RestResponse(allTo = "inventories")
public class APIGetFreeIpReply extends APIReply {
    private List<FreeIpInventory> inventories;

    public List<FreeIpInventory> getInventories() {
        return inventories;
    }

    public void setInventories(List<FreeIpInventory> inventories) {
        this.inventories = inventories;
    }
}
