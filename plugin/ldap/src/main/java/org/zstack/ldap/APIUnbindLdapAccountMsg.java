package org.zstack.ldap;

import org.springframework.http.HttpMethod;
import org.zstack.header.message.APIMessage;
import org.zstack.header.message.APIParam;
import org.zstack.header.rest.RestRequest;

@RestRequest(
        path = "/ldap/account-refs/{uuid}",
        method = HttpMethod.DELETE,
        responseClass = APIUnbindLdapAccountEvent.class
)
public class APIUnbindLdapAccountMsg extends APIMessage {
    @APIParam(maxLength = 32)
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
