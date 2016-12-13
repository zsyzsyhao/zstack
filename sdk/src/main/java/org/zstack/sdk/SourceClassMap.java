package org.zstack.sdk;

import java.util.HashMap;

public class SourceClassMap {
    final static HashMap<String, String> srcToDstMapping = new HashMap() {
        {
			put("org.zstack.header.identity.UserInventory", "org.zstack.sdk.UserInventory");
			put("org.zstack.header.identity.PolicyInventory", "org.zstack.sdk.PolicyInventory");
			put("org.zstack.header.identity.UserGroupInventory", "org.zstack.sdk.UserGroupInventory");
			put("org.zstack.header.identity.AccountResourceRefInventory", "org.zstack.sdk.AccountResourceRefInventory");
			put("org.zstack.header.zone.ZoneInventory", "org.zstack.sdk.ZoneInventory");
			put("org.zstack.header.vm.VmInstanceInventory", "org.zstack.sdk.VmInstanceInventory");
			put("org.zstack.header.cluster.ClusterInventory", "org.zstack.sdk.ClusterInventory");
			put("org.zstack.header.identity.QuotaInventory", "org.zstack.sdk.QuotaInventory");
			put("org.zstack.header.identity.Quota$QuotaUsage", "org.zstack.sdk.QuotaUsage");
			put("org.zstack.header.host.HostInventory", "org.zstack.sdk.HostInventory");
			put("org.zstack.header.identity.AccountInventory", "org.zstack.sdk.AccountInventory");
			put("org.zstack.header.identity.SharedResourceInventory", "org.zstack.sdk.SharedResourceInventory");
			put("org.zstack.header.volume.VolumeInventory", "org.zstack.sdk.VolumeInventory");
			put("org.zstack.header.identity.PolicyInventory$Statement", "org.zstack.sdk.Statement");
			put("org.zstack.header.vm.VmNicInventory", "org.zstack.sdk.VmNicInventory");
			put("org.zstack.header.identity.AccountConstant$StatementEffect", "org.zstack.sdk.StatementEffect");
			put("[Lorg.zstack.header.identity.AccountConstant$StatementEffect;", "org.zstack.sdk.StatementEffect[]");
        }
    };

    final static HashMap<String, String> dstToSrcMapping = new HashMap() {
        {
			put("org.zstack.sdk.UserInventory", "org.zstack.header.identity.UserInventory");
			put("org.zstack.sdk.PolicyInventory", "org.zstack.header.identity.PolicyInventory");
			put("org.zstack.sdk.UserGroupInventory", "org.zstack.header.identity.UserGroupInventory");
			put("org.zstack.sdk.AccountResourceRefInventory", "org.zstack.header.identity.AccountResourceRefInventory");
			put("org.zstack.sdk.ZoneInventory", "org.zstack.header.zone.ZoneInventory");
			put("org.zstack.sdk.VmInstanceInventory", "org.zstack.header.vm.VmInstanceInventory");
			put("org.zstack.sdk.ClusterInventory", "org.zstack.header.cluster.ClusterInventory");
			put("org.zstack.sdk.QuotaInventory", "org.zstack.header.identity.QuotaInventory");
			put("org.zstack.sdk.QuotaUsage", "org.zstack.header.identity.Quota$QuotaUsage");
			put("org.zstack.sdk.HostInventory", "org.zstack.header.host.HostInventory");
			put("org.zstack.sdk.AccountInventory", "org.zstack.header.identity.AccountInventory");
			put("org.zstack.sdk.SharedResourceInventory", "org.zstack.header.identity.SharedResourceInventory");
			put("org.zstack.sdk.VolumeInventory", "org.zstack.header.volume.VolumeInventory");
			put("org.zstack.sdk.Statement", "org.zstack.header.identity.PolicyInventory$Statement");
			put("org.zstack.sdk.VmNicInventory", "org.zstack.header.vm.VmNicInventory");
			put("org.zstack.sdk.StatementEffect", "org.zstack.header.identity.AccountConstant$StatementEffect");
			put("org.zstack.sdk.StatementEffect[]", "[Lorg.zstack.header.identity.AccountConstant$StatementEffect;");
        }
    };
}
