package com.project.dosmart.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PinCodeService {

    private static final String APP_DIR = ".dosmart";
    private static final String PIN_FILE_NAME = "pin.json";
    private final String PIN_FILE_PATH;
    private final ObjectMapper _objectMapper;
    private int _failedAttempts = 0;
    private LocalDateTime _lockoutTime = null;

    public PinCodeService() {
        _objectMapper = new ObjectMapper();
        String userHome = System.getProperty("user.home");
        Path appDirPath = Paths.get(userHome, APP_DIR);
        PIN_FILE_PATH = Paths.get(userHome, APP_DIR, PIN_FILE_NAME).toString();

        try {
            Files.createDirectories(appDirPath);
        } catch (IOException ignored) {
        }

        loadLockoutState();
    }

    public boolean hasPinCode() {
        File file = new File(PIN_FILE_PATH);
        return file.exists();
    }

    public boolean setPinCode(String pinCode) {
        if (!isValidPin(pinCode)) {
            return false;
        }

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("hash", hashPin(pinCode));
            data.put("failedAttempts", 0);
            _objectMapper.writeValue(new File(PIN_FILE_PATH), data);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public boolean verifyPinCode(String pinCode) {
        if (isLockedOut()) {
            return false;
        }

        if (!isValidPin(pinCode)) {
            return false;
        }

        try {
            File file = new File(PIN_FILE_PATH);

            if (!file.exists()) {
                return false;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = _objectMapper.readValue(file, Map.class);
            String storedHash = (String) data.get("hash");

            if (hashPin(pinCode).equals(storedHash)) {
                resetFailedAttempts();
                return true;
            } else {
                incrementFailedAttempts();
                return false;
            }
        } catch (IOException ignored) {
            return false;
        }
    }

    public void removePinCode() {
        File file = new File(PIN_FILE_PATH);

        if (file.exists()) {
            file.delete();
        }

        resetFailedAttempts();
        _lockoutTime = null;
    }

    public boolean isLockedOut() {
        if (_lockoutTime == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(_lockoutTime)) {
            _lockoutTime = null;
            resetFailedAttempts();
            return false;
        }

        return true;
    }

    public long getRemainingLockoutSeconds() {
        if (_lockoutTime == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(_lockoutTime)) {
            return 0;
        }

        return java.time.Duration.between(now, _lockoutTime).getSeconds();
    }

    public int getFailedAttempts() {
        return _failedAttempts;
    }

    public int getRemainingAttempts() {
        return Math.max(0, 3 - _failedAttempts);
    }

    private boolean isValidPin(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }

    private String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException ignored) {
            throw new RuntimeException(ignored);
        }
    }

    private void incrementFailedAttempts() {
        _failedAttempts++;

        if (_failedAttempts >= 3) {
            _lockoutTime = LocalDateTime.now().plusMinutes(1);
            saveLockoutState();
        }
    }

    private void resetFailedAttempts() {
        _failedAttempts = 0;
        _lockoutTime = null;
        saveLockoutState();
    }

    private void saveLockoutState() {
        try {
            Map<String, Object> lockoutData = new HashMap<>();
            lockoutData.put("failedAttempts", _failedAttempts);

            if (_lockoutTime != null) {
                lockoutData.put("lockoutTime", _lockoutTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            File lockoutFile = new File(PIN_FILE_PATH + ".lockout");
            _objectMapper.writeValue(lockoutFile, lockoutData);
        } catch (IOException ignored) {
        }
    }

    private void loadLockoutState() {
        try {
            File lockoutFile = new File(PIN_FILE_PATH + ".lockout");

            if (lockoutFile.exists()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> lockoutData = _objectMapper.readValue(lockoutFile, Map.class);
                _failedAttempts = (Integer) lockoutData.getOrDefault("failedAttempts", 0);
                String lockoutTimeStr = (String) lockoutData.get("lockoutTime");

                if (lockoutTimeStr != null) {
                    _lockoutTime = LocalDateTime.parse(lockoutTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            }
        } catch (IOException ignored) {
        }
    }
}