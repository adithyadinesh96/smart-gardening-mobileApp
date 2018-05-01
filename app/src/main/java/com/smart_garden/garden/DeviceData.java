package com.smart_garden.garden;

public class DeviceData {
    private String deviceId;
    private String deviceName;
    private int switchState;
    private int isInstalled;
    private String url;

    public DeviceData(String deviceId,String deviceName, int switchState, int isInstalled, String url) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.switchState = switchState;
        this.isInstalled = isInstalled;
        this.url = url;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getSwitchState() {
        return switchState;
    }

    public int getIsInstalled() {
        return isInstalled;
    }

    public String getUrl() {
        return url;
    }
}
