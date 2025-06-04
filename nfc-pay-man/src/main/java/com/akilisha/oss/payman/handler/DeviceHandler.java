package com.akilisha.oss.payman.handler;

import com.akilisha.oss.payman.model.Device;
import com.akilisha.oss.payman.service.DeviceService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

// This class needs to be listed in META-INF/services/com.akilisha.oss.payman.handler.BaseHandler
public class DeviceHandler extends BaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceHandler.class);
    private final Gson gson = new Gson();
    private DeviceService deviceService; // Injected dependency

    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public String getPath() {
        return "/device";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Register or Update Device
        String payload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Device device = gson.fromJson(payload, Device.class);

        if (device == null || device.getDeviceId() == null || device.getUserId() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new StatusResponse(false, "Missing deviceId or userId.")));
            return;
        }

        deviceService.registerDevice(device);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(new StatusResponse(true, "Device registered/updated successfully.")));
        LOGGER.info("Device registered/updated: {}", device.getDeviceId());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Update Device (same as POST for simplicity here)
        doPost(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Unregister Device
        String deviceId = req.getParameter("deviceId");
        if (deviceId == null || deviceId.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new StatusResponse(false, "Missing deviceId.")));
            return;
        }

        deviceService.unregisterDevice(deviceId);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().write(gson.toJson(new StatusResponse(true, "Device unregistered successfully.")));
        LOGGER.info("Device unregistered: {}", deviceId);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Get Device Info (for a specific deviceId or all for a userId)
        String deviceId = req.getParameter("deviceId");
        String userId = req.getParameter("userId");

        if (deviceId != null && !deviceId.isEmpty()) {
            Device device = deviceService.getDevice(deviceId);
            if (device != null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write(gson.toJson(device));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.setContentType("application/json");
                resp.getWriter().write(gson.toJson(new StatusResponse(false, "Device not found.")));
            }
        } else if (userId != null && !userId.isEmpty()) {
            // In a real app, you'd have a method like getDevicesByUserId
            resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new StatusResponse(false, "Getting devices by userId not implemented for demo.")));
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(new StatusResponse(false, "Missing deviceId or userId parameter.")));
        }
    }

    private static class StatusResponse {
        boolean success;
        String message;

        public StatusResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
