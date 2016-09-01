package org.zstack.core.keystore;

import org.zstack.header.Component;
import org.zstack.header.core.keystore.KeystoreInventory;

/**
 * Created by miao on 16-8-15.
 */
public interface KeystoreManager extends Component {
    public KeystoreInventory createIfNotExist(KeystoreInventory ksinv);

    public KeystoreInventory createOrUpdate(KeystoreInventory ksinv);

    public void delete(String uuid);

    public void delete(KeystoreInventory ksinv);
}
