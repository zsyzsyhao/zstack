package org.zstack.sdk;

public class StatementEffect  {

    public StatementEffect Allow;
    public void setAllow(StatementEffect Allow) {
        this.Allow = Allow;
    }
    public StatementEffect getAllow() {
        return this.Allow;
    }

    public StatementEffect Deny;
    public void setDeny(StatementEffect Deny) {
        this.Deny = Deny;
    }
    public StatementEffect getDeny() {
        return this.Deny;
    }

    public StatementEffect[] ENUM$VALUES;
    public void setENUM$VALUES(StatementEffect[] ENUM$VALUES) {
        this.ENUM$VALUES = ENUM$VALUES;
    }
    public StatementEffect[] getENUM$VALUES() {
        return this.ENUM$VALUES;
    }

}
