package org.zstack.ldap;

import org.springframework.http.HttpMethod;
import org.zstack.header.query.APIQueryMessage;
import org.zstack.header.query.AutoQuery;
import org.zstack.header.rest.RestRequest;
import org.zstack.header.rest.RestResponse;

@AutoQuery(replyClass = APIQueryLdapAccountReply.class, inventoryClass = LdapAccountRefInventory.class)
@RestRequest(
        path = "/ldap/accounts/refs",
        method = HttpMethod.GET,
        responseClass = APIQueryLdapAccountReply.class
)
public class APIQueryLdapAccountMsg extends APIQueryMessage {
}
