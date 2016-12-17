package org.zstack.sdk;

import java.util.HashMap;

public class SourceClassMap {
    final static HashMap<String, String> srcToDstMapping = new HashMap() {
        {
			put("org.zstack.header.image.ImageInventory", "org.zstack.sdk.ImageInventory");
			put("org.zstack.header.identity.PolicyInventory", "org.zstack.sdk.PolicyInventory");
			put("org.zstack.header.identity.QuotaInventory", "org.zstack.sdk.QuotaInventory");
			put("org.zstack.header.console.ConsoleInventory", "org.zstack.sdk.ConsoleInventory");
			put("org.zstack.header.identity.SharedResourceInventory", "org.zstack.sdk.SharedResourceInventory");
			put("org.zstack.header.image.APICreateRootVolumeTemplateFromVolumeSnapshotEvent$Failure", "org.zstack.sdk.Failure");
			put("org.zstack.header.identity.AccountResourceRefInventory", "org.zstack.sdk.AccountResourceRefInventory");
			put("org.zstack.header.identity.UserInventory", "org.zstack.sdk.UserInventory");
			put("org.zstack.header.configuration.DiskOfferingInventory", "org.zstack.sdk.DiskOfferingInventory");
			put("org.zstack.header.vm.VmInstanceInventory", "org.zstack.sdk.VmInstanceInventory");
			put("org.zstack.header.configuration.InstanceOfferingInventory", "org.zstack.sdk.InstanceOfferingInventory");
			put("org.zstack.header.zone.ZoneInventory", "org.zstack.sdk.ZoneInventory");
			put("org.zstack.header.identity.Quota$QuotaUsage", "org.zstack.sdk.QuotaUsage");
			put("org.zstack.header.host.HostInventory", "org.zstack.sdk.HostInventory");
			put("org.zstack.header.storage.backup.BackupStorageInventory", "org.zstack.sdk.BackupStorageInventory");
			put("org.zstack.header.cluster.ClusterInventory", "org.zstack.sdk.ClusterInventory");
			put("org.zstack.header.identity.AccountInventory", "org.zstack.sdk.AccountInventory");
			put("org.zstack.header.console.ConsoleProxyAgentInventory", "org.zstack.sdk.ConsoleProxyAgentInventory");
			put("org.zstack.header.identity.UserGroupInventory", "org.zstack.sdk.UserGroupInventory");
			put("org.zstack.header.image.ImageBackupStorageRefInventory", "org.zstack.sdk.ImageBackupStorageRefInventory");
			put("org.zstack.header.vm.VmNicInventory", "org.zstack.sdk.VmNicInventory");
			put("org.zstack.header.volume.VolumeInventory", "org.zstack.sdk.VolumeInventory");
			put("org.zstack.header.errorcode.ErrorCode", "org.zstack.sdk.ErrorCode");
			put("org.zstack.header.identity.PolicyInventory$Statement", "org.zstack.sdk.PolicyStatement");
			put("org.zstack.header.identity.AccountConstant$StatementEffect", "org.zstack.sdk.PolicyStatementEffect");
        }
    };

    final static HashMap<String, String> dstToSrcMapping = new HashMap() {
        {
			put("org.zstack.sdk.ImageInventory", "org.zstack.header.image.ImageInventory");
			put("org.zstack.sdk.PolicyInventory", "org.zstack.header.identity.PolicyInventory");
			put("org.zstack.sdk.QuotaInventory", "org.zstack.header.identity.QuotaInventory");
			put("org.zstack.sdk.ConsoleInventory", "org.zstack.header.console.ConsoleInventory");
			put("org.zstack.sdk.SharedResourceInventory", "org.zstack.header.identity.SharedResourceInventory");
			put("org.zstack.sdk.Failure", "org.zstack.header.image.APICreateRootVolumeTemplateFromVolumeSnapshotEvent$Failure");
			put("org.zstack.sdk.AccountResourceRefInventory", "org.zstack.header.identity.AccountResourceRefInventory");
			put("org.zstack.sdk.UserInventory", "org.zstack.header.identity.UserInventory");
			put("org.zstack.sdk.DiskOfferingInventory", "org.zstack.header.configuration.DiskOfferingInventory");
			put("org.zstack.sdk.VmInstanceInventory", "org.zstack.header.vm.VmInstanceInventory");
			put("org.zstack.sdk.InstanceOfferingInventory", "org.zstack.header.configuration.InstanceOfferingInventory");
			put("org.zstack.sdk.ZoneInventory", "org.zstack.header.zone.ZoneInventory");
			put("org.zstack.sdk.QuotaUsage", "org.zstack.header.identity.Quota$QuotaUsage");
			put("org.zstack.sdk.HostInventory", "org.zstack.header.host.HostInventory");
			put("org.zstack.sdk.BackupStorageInventory", "org.zstack.header.storage.backup.BackupStorageInventory");
			put("org.zstack.sdk.ClusterInventory", "org.zstack.header.cluster.ClusterInventory");
			put("org.zstack.sdk.AccountInventory", "org.zstack.header.identity.AccountInventory");
			put("org.zstack.sdk.ConsoleProxyAgentInventory", "org.zstack.header.console.ConsoleProxyAgentInventory");
			put("org.zstack.sdk.UserGroupInventory", "org.zstack.header.identity.UserGroupInventory");
			put("org.zstack.sdk.ImageBackupStorageRefInventory", "org.zstack.header.image.ImageBackupStorageRefInventory");
			put("org.zstack.sdk.VmNicInventory", "org.zstack.header.vm.VmNicInventory");
			put("org.zstack.sdk.VolumeInventory", "org.zstack.header.volume.VolumeInventory");
			put("org.zstack.sdk.ErrorCode", "org.zstack.header.errorcode.ErrorCode");
			put("org.zstack.sdk.PolicyStatement", "org.zstack.header.identity.PolicyInventory$Statement");
			put("org.zstack.sdk.PolicyStatementEffect", "org.zstack.header.identity.AccountConstant$StatementEffect");
        }
    };
}
