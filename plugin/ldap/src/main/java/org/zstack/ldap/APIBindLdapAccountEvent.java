package org.zstack.ldap;

import org.zstack.header.message.APIEvent;
import org.zstack.header.rest.RestResponse;

@RestResponse(allTo = "inventory")
public class APIBindLdapAccountEvent extends APIEvent {
    private LdapAccountRefInventory inventory;

    public APIBindLdapAccountEvent(String apiId) {
        super(apiId);
    }

    public APIBindLdapAccountEvent() {
        super(null);
    }

    public LdapAccountRefInventory getInventory() {
        return inventory;
    }

    public void setInventory(LdapAccountRefInventory inventory) {
        this.inventory = inventory;
    }
}
