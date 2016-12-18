package org.zstack.ldap;

import org.springframework.http.HttpMethod;
import org.zstack.header.message.APIMessage;
import org.zstack.header.rest.RestRequest;

/**
 * Created by miao on 16-9-22.
 */
@RestRequest(
        path = "/ladp/bindings/actions",
        isAction = true,
        method = HttpMethod.PUT,
        responseClass = APICleanInvalidLdapBindingsEvent.class
)
public class APICleanInvalidLdapBindingsMsg extends APIMessage {
}
