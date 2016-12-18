package org.zstack.ldap;

import org.springframework.http.HttpMethod;
import org.zstack.header.identity.APISessionMessage;
import org.zstack.header.identity.SuppressCredentialCheck;
import org.zstack.header.message.APIParam;
import org.zstack.header.rest.RestRequest;

@SuppressCredentialCheck
@RestRequest(
        path = "/ldap/uids/{uid}/actions",
        method = HttpMethod.PUT,
        responseClass = APILogInByLdapReply.class,
        isAction = true
)
public class APILogInByLdapMsg extends APISessionMessage {
    @APIParam
    private String uid;
    @APIParam
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
