package org.zstack.sdk;

import java.util.HashMap;

public class SourceClassMap {
    final static HashMap<String, String> srcToDstMapping = new HashMap() {
        {
			put("org.zstack.header.zone.ZoneInventory", "org.zstack.sdk.ZoneInventory");
			put("org.zstack.header.vm.VmInstanceInventory", "org.zstack.sdk.VmInstanceInventory");
			put("org.zstack.header.vm.VmNicInventory", "org.zstack.sdk.VmNicInventory");
			put("org.zstack.header.volume.VolumeInventory", "org.zstack.sdk.VolumeInventory");
        }
    };

    final static HashMap<String, String> dstToSrcMapping = new HashMap() {
        {
			put("org.zstack.sdk.ZoneInventory", "org.zstack.header.zone.ZoneInventory");
			put("org.zstack.sdk.VmInstanceInventory", "org.zstack.header.vm.VmInstanceInventory");
			put("org.zstack.sdk.VmNicInventory", "org.zstack.header.vm.VmNicInventory");
			put("org.zstack.sdk.VolumeInventory", "org.zstack.header.volume.VolumeInventory");
        }
    };
}
