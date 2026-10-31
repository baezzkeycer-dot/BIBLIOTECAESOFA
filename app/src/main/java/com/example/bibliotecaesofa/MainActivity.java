package com.example.bibliotecaesofa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextInputLayout tilUser;
    private TextInputLayout tilPassword;
    private EditText etUser;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        tilUser = findViewById(R.id.tilUser);
        tilPassword = findViewById(R.id.tilPassword);
        etUser = findViewById(R.id.etUser);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.btnRegister).setOnClickListener(v -> showRegisterDialog());

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void attemptLogin() {
        tilUser.setError(null);
        tilPassword.setError(null);

        String user = etUser.getText() == null ? "" : etUser.getText().toString().trim();
        String password = etPassword.getText() == null ? "" : etPassword.getText().toString();

        if (user.isEmpty()) {
            tilUser.setError(getString(R.string.login_empty_fields));
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.login_empty_fields));
            return;
        }

        if (!sessionManager.login(user, password)) {
            tilPassword.setError(getString(R.string.login_invalid));
            return;
        }

        Toast.makeText(this,
                getString(R.string.login_welcome, sessionManager.getCurrentUser()),
                Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, CatalogActivity.class));
        finish();
    }

    private void showRegisterDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_register, null);
        TextInputLayout tilNewUser = view.findViewById(R.id.tilNewUser);
        TextInputLayout tilNewPassword = view.findViewById(R.id.tilNewPassword);
        TextInputLayout tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        EditText etNewUser = view.findViewById(R.id.etNewUser);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.register_title)
                .setView(view)
                .setPositiveButton(R.string.register_action, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(v -> {
                tilNewUser.setError(null);
                tilNewPassword.setError(null);
                tilConfirmPassword.setError(null);

                String user = etNewUser.getText() == null
                        ? "" : etNewUser.getText().toString().trim();
                String password = etNewPassword.getText() == null
                        ? "" : etNewPassword.getText().toString();
                String confirmation = etConfirmPassword.getText() == null
                        ? "" : etConfirmPassword.getText().toString();

                if (user.length() < 3) {
                    tilNewUser.setError(getString(R.string.register_short_user));
                    return;
                }
                if (password.length() < 4) {
                    tilNewPassword.setError(getString(R.string.register_short_password));
                    return;
                }
                if (!password.equals(confirmation)) {
                    tilConfirmPassword.setError(getString(R.string.register_mismatch));
                    return;
                }
                if (!sessionManager.register(user, password)) {
                    tilNewUser.setError(getString(R.string.register_exists));
                    return;
                }

                etUser.setText(user);
                etPassword.setText("");
                Toast.makeText(MainActivity.this,
                        R.string.register_success, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
