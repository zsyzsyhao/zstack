package org.zstack.sdk;

public class CreateRootVolumeTemplateFromVolumeSnapshotResult {
    public ImageInventory inventory;
    public void setInventory(ImageInventory inventory) {
        this.inventory = inventory;
    }
    public ImageInventory getInventory() {
        return this.inventory;
    }

    public java.util.List<Failure> failures;
    public void setFailures(java.util.List<Failure> failures) {
        this.failures = failures;
    }
    public java.util.List<Failure> getFailures() {
        return this.failures;
    }

}
