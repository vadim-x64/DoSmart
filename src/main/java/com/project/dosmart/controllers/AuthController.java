package com.project.dosmart.controllers;

import com.project.dosmart.services.PinCodeService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AuthController {

    @FXML
    private PasswordField _pinField;
    @FXML
    private TextField _visiblePinField;
    @FXML
    private Button _unlockButton;
    @FXML
    private Button _toggleVisibilityButton;
    @FXML
    private Label _messageLabel;
    @FXML
    private Label _attemptsLabel;

    private PinCodeService _pinCodeService;
    private Stage _stage;
    private boolean _authenticated = false;
    private Timeline _lockoutTimer;
    private boolean _pinVisible = false;

    public void setPinCodeService(PinCodeService pinCodeService) {
        _pinCodeService = pinCodeService;

        if (_pinCodeService != null) {
            checkLockoutStatus();
            updateAttemptsLabel();
        }
    }

    public void setStage(Stage stage) {
        _stage = stage;
    }

    public boolean isAuthenticated() {
        return _authenticated;
    }

    @FXML
    private void initialize() {
        _unlockButton.setOnAction(e -> verifyPin());
        _toggleVisibilityButton.setOnAction(e -> togglePinVisibility());
        _pinField.setOnAction(e -> verifyPin());
        _visiblePinField.setOnAction(e -> verifyPin());
        _visiblePinField.setVisible(false);
        _visiblePinField.setManaged(false);

        _pinField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                _pinField.setText(oldText);
                return;
            }

            if (newText.length() > 4) {
                _pinField.setText(newText.substring(0, 4));
                return;
            }

            _visiblePinField.setText(newText);
        });

        _visiblePinField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                _visiblePinField.setText(oldText);
                return;
            }

            if (newText.length() > 4) {
                _visiblePinField.setText(newText.substring(0, 4));
                return;
            }

            _pinField.setText(newText);
        });
    }

    private void togglePinVisibility() {
        _pinVisible = !_pinVisible;

        if (_pinVisible) {
            _pinField.setVisible(false);
            _pinField.setManaged(false);
            _visiblePinField.setVisible(true);
            _visiblePinField.setManaged(true);
            _toggleVisibilityButton.setText("🔓");
            _visiblePinField.requestFocus();
        } else {
            _visiblePinField.setVisible(false);
            _visiblePinField.setManaged(false);
            _pinField.setVisible(true);
            _pinField.setManaged(true);
            _toggleVisibilityButton.setText("🔒");
            _pinField.requestFocus();
        }
    }

    private void verifyPin() {
        String pin = _pinVisible ? _visiblePinField.getText().trim() : _pinField.getText().trim();

        if (pin.length() < 4) {
            _messageLabel.setText("Пін-код має містити 4 цифри");
            _messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (_pinCodeService.verifyPinCode(pin)) {
            _authenticated = true;
            _stage.close();
        } else {
            if (_pinCodeService.isLockedOut()) {
                clearPinFields();
                startLockoutTimer();
            } else {
                _messageLabel.setText("Невірний пін-код");
                _messageLabel.setStyle("-fx-text-fill: red;");
                clearPinFields();
                updateAttemptsLabel();
            }
        }
    }

    private void clearPinFields() {
        _pinField.clear();
        _visiblePinField.clear();
    }

    private void updateAttemptsLabel() {
        if (_pinCodeService != null && !_pinCodeService.isLockedOut()) {
            int remaining = _pinCodeService.getRemainingAttempts();
            _attemptsLabel.setText("Залишилось спроб: " + remaining);
            _attemptsLabel.setStyle("-fx-text-fill: #666666;");
        }
    }

    private void checkLockoutStatus() {
        if (_pinCodeService != null && _pinCodeService.isLockedOut()) {
            startLockoutTimer();
        }
    }

    private void startLockoutTimer() {
        _pinField.setDisable(true);
        _visiblePinField.setDisable(true);
        _unlockButton.setDisable(true);
        _toggleVisibilityButton.setDisable(true);
        _attemptsLabel.setText("");
        _lockoutTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateLockoutMessage()));
        _lockoutTimer.setCycleCount(Animation.INDEFINITE);
        _lockoutTimer.play();
        updateLockoutMessage();
    }

    private void updateLockoutMessage() {
        long remainingSeconds = _pinCodeService.getRemainingLockoutSeconds();

        if (remainingSeconds <= 0) {
            _lockoutTimer.stop();
            _pinField.setDisable(false);
            _visiblePinField.setDisable(false);
            _unlockButton.setDisable(false);
            _toggleVisibilityButton.setDisable(false);
            _messageLabel.setText("");
            clearPinFields();
            updateAttemptsLabel();
        } else {
            long minutes = remainingSeconds / 60;
            long seconds = remainingSeconds % 60;
            _messageLabel.setText(String.format("Спробуйте знову через %02d:%02d", minutes, seconds));
            _messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}