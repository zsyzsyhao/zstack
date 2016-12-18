package org.zstack.ldap;

import org.zstack.header.identity.AccountInventory;
import org.zstack.header.message.APIEvent;
import org.zstack.header.rest.RestResponse;

import java.util.List;

/**
 * Created by miao on 16-9-22.
 */
@RestResponse(allTo = "inventories")
public class APICleanInvalidLdapBindingsEvent extends APIEvent {
    private List<AccountInventory> inventories;

    public APICleanInvalidLdapBindingsEvent(String apiId) {
        super(apiId);
    }

    public APICleanInvalidLdapBindingsEvent() {
        super(null);
    }

    public List<AccountInventory> getInventories() {
        return inventories;
    }

    public void setInventories(List<AccountInventory> inventories) {
        this.inventories = inventories;
    }
}
