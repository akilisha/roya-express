package com.akilisha.oss.payman.model;

public class Device {
    private String deviceId;
    private String userId; // Associated user
    private String deviceName;
    private long lastRegistered;

    public Device(String deviceId, String userId, String deviceName, long lastRegistered) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.deviceName = deviceName;
        this.lastRegistered = lastRegistered;
    }

    // Getters and Setters
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public long getLastRegistered() {
        return lastRegistered;
    }

    public void setLastRegistered(long lastRegistered) {
        this.lastRegistered = lastRegistered;
    }
}
