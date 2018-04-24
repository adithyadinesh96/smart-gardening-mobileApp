package com.garden.garden;

public class DeviceData {
    private String deviceName;
    private int switchState;
    private int isInstalled;
    private String url;

    public DeviceData(String deviceName, int switchState, int isInstalled, String url) {
        this.deviceName = deviceName;
        this.switchState = switchState;
        this.isInstalled = isInstalled;
        this.url = url;
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
