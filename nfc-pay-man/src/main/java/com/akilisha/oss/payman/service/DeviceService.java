package com.akilisha.oss.payman.service;

import com.akilisha.oss.payman.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    // In a real application, this would interact with a persistent database
    private final Map<String, Device> registeredDevices = new ConcurrentHashMap<>();

    public void registerDevice(Device device) {
        registeredDevices.put(device.getDeviceId(), device);
        LOGGER.info("DeviceService: Device registered/updated - {}", device.getDeviceId());
    }

    public Device getDevice(String deviceId) {
        return registeredDevices.get(deviceId);
    }

    public void unregisterDevice(String deviceId) {
        registeredDevices.remove(deviceId);
        LOGGER.info("DeviceService: Device unregistered - {}", deviceId);
    }
}
