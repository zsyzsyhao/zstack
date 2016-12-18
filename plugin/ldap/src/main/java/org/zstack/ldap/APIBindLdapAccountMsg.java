package org.zstack.ldap;

import org.springframework.http.HttpMethod;
import org.zstack.header.message.APIMessage;
import org.zstack.header.message.APIParam;
import org.zstack.header.rest.RestRequest;

@RestRequest(
        path = "/ladp/account-refs",
        method = HttpMethod.POST,
        responseClass = APIBindLdapAccountEvent.class,
        parameterName = "params"
)
public class APIBindLdapAccountMsg extends APIMessage {
    @APIParam(maxLength = 255)
    private String ldapUid;

    @APIParam(maxLength = 32)
    private String accountUuid;

    public String getLdapUid() {
        return ldapUid;
    }

    public void setLdapUid(String ldapUid) {
        this.ldapUid = ldapUid;
    }

    public String getAccountUuid() {
        return accountUuid;
    }

    public void setAccountUuid(String accountUuid) {
        this.accountUuid = accountUuid;
    }
}
