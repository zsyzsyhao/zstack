package org.zstack.sdk;

public class VmInstanceInventory  {

    public java.lang.String uuid;

    public java.lang.String name;

    public java.lang.String description;

    public java.lang.String zoneUuid;

    public java.lang.String clusterUuid;

    public java.lang.String imageUuid;

    public java.lang.String hostUuid;

    public java.lang.String lastHostUuid;

    public java.lang.String instanceOfferingUuid;

    public java.lang.String rootVolumeUuid;

    public java.lang.String platform;

    public java.lang.String defaultL3NetworkUuid;

    public java.lang.String type;

    public java.lang.String hypervisorType;

    public java.lang.Long memorySize;

    public java.lang.Integer cpuNum;

    public java.lang.Long cpuSpeed;

    public java.lang.String allocatorStrategy;

    public java.sql.Timestamp createDate;

    public java.sql.Timestamp lastOpDate;

    public java.lang.String state;

    public java.util.List<VmNicInventory> vmNics;

    public java.util.List<VolumeInventory> allVolumes;

}
