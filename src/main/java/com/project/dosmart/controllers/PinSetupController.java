package com.project.dosmart.controllers;

import com.project.dosmart.services.PinCodeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class PinSetupController {

    @FXML
    private PasswordField _pinField;
    @FXML
    private TextField _visiblePinField;
    @FXML
    private PasswordField _confirmPinField;
    @FXML
    private TextField _visibleConfirmPinField;
    @FXML
    private Button _setupButton;
    @FXML
    private Button _toggleVisibilityButton;
    @FXML
    private Label _messageLabel;
    @FXML
    private Label _confirmLabel;

    private PinCodeService _pinCodeService;
    private Stage _stage;
    private boolean _isRemoving = false;
    private boolean _pinVisible = false;

    public void setPinCodeService(PinCodeService pinCodeService) {
        _pinCodeService = pinCodeService;
    }

    public void setStage(Stage stage) {
        _stage = stage;
    }

    public void setRemovingMode(boolean removing) {
        _isRemoving = removing;
        if (_isRemoving) {
            _confirmPinField.setVisible(false);
            _confirmPinField.setManaged(false);
            _visibleConfirmPinField.setVisible(false);
            _visibleConfirmPinField.setManaged(false);
            _confirmLabel.setVisible(false);
            _confirmLabel.setManaged(false);
            _setupButton.setText("ЗНЯТИ");
        }
    }

    @FXML
    private void initialize() {
        _setupButton.setOnAction(e -> handlePinAction());
        _toggleVisibilityButton.setOnAction(e -> togglePinVisibility());
        _visiblePinField.setVisible(false);
        _visiblePinField.setManaged(false);
        _visibleConfirmPinField.setVisible(false);
        _visibleConfirmPinField.setManaged(false);
        addPinFieldRestrictions(_pinField, _visiblePinField);
        addPinFieldRestrictions(_confirmPinField, _visibleConfirmPinField);
    }

    private void addPinFieldRestrictions(PasswordField passwordField, TextField textField) {
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                passwordField.setText(oldText);
                return;
            }

            if (newText.length() > 4) {
                passwordField.setText(newText.substring(0, 4));
                return;
            }

            textField.setText(newText);
        });

        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                textField.setText(oldText);

                return;
            }

            if (newText.length() > 4) {
                textField.setText(newText.substring(0, 4));

                return;
            }

            passwordField.setText(newText);
        });
    }

    private void togglePinVisibility() {
        _pinVisible = !_pinVisible;

        if (_pinVisible) {
            _pinField.setVisible(false);
            _pinField.setManaged(false);
            _visiblePinField.setVisible(true);
            _visiblePinField.setManaged(true);

            if (!_isRemoving) {
                _confirmPinField.setVisible(false);
                _confirmPinField.setManaged(false);
                _visibleConfirmPinField.setVisible(true);
                _visibleConfirmPinField.setManaged(true);
            }

            _toggleVisibilityButton.setText("🔓");
            _visiblePinField.requestFocus();
        } else {
            _visiblePinField.setVisible(false);
            _visiblePinField.setManaged(false);
            _pinField.setVisible(true);
            _pinField.setManaged(true);

            if (!_isRemoving) {
                _visibleConfirmPinField.setVisible(false);
                _visibleConfirmPinField.setManaged(false);
                _confirmPinField.setVisible(true);
                _confirmPinField.setManaged(true);
            }

            _toggleVisibilityButton.setText("🔒");
            _pinField.requestFocus();
        }
    }

    private void handlePinAction() {
        String pin = _pinVisible ? _visiblePinField.getText().trim() : _pinField.getText().trim();

        if (pin.length() != 4) {
            showError("Пін-код повинен містити 4 цифри");

            return;
        }

        if (_isRemoving) {
            if (_pinCodeService.verifyPinCode(pin)) {
                _pinCodeService.removePinCode();
                _stage.close();
            } else {
                if (_pinCodeService.isLockedOut()) {
                    showError("Додаток заблоковано. Спробуйте пізніше.");
                } else {
                    showError("Невірний пін-код");
                    clearPinFields();
                }
            }
        } else {
            String confirmPin = _pinVisible ? _visibleConfirmPinField.getText().trim() : _confirmPinField.getText().trim();

            if (confirmPin.length() != 4) {
                showError("Підтвердження має містити 4 цифри");

                return;
            }

            if (!pin.equals(confirmPin)) {
                showError("Пін-коди не співпадають");
                clearConfirmFields();

                return;
            }

            if (_pinCodeService.setPinCode(pin)) {
                _stage.close();
            }
        }
    }

    private void clearPinFields() {
        _pinField.clear();
        _visiblePinField.clear();
    }

    private void clearConfirmFields() {
        _confirmPinField.clear();
        _visibleConfirmPinField.clear();
    }

    private void showError(String message) {
        _messageLabel.setText(message);
    }
}