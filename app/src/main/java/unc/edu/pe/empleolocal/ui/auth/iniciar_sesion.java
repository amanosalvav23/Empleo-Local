package unc.edu.pe.empleolocal.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import unc.edu.pe.empleolocal.R;
import unc.edu.pe.empleolocal.databinding.ActivityIniciarSesionBinding;
import unc.edu.pe.empleolocal.ui.main.MainActivity;
import unc.edu.pe.empleolocal.ui.registro.RegistroPaso1Activity;
import unc.edu.pe.empleolocal.utils.ViewUtils;

public class iniciar_sesion extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private ActivityIniciarSesionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityIniciarSesionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(iniciar_sesion.this, RegistroPaso1Activity.class);
            startActivity(intent);
        });

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                ViewUtils.showSnackbar(this, "Por favor, complete todos los campos", ViewUtils.MsgType.WARNING);
                return;
            }

            authViewModel.login(email, password);
        });

        binding.tvForgotPassword.setOnClickListener(v -> showResetPasswordDialog());

        setupObservers();
    }

    private void showResetPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
        TextInputEditText etResetEmail = dialogView.findViewById(R.id.et_reset_email);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSend = dialogView.findViewById(R.id.btn_send);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Hacer el fondo del diálogo transparente para que se vea el borde redondeado de la card
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String email = etResetEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etResetEmail.setError("Ingrese su correo");
                return;
            }
            authViewModel.resetPassword(email);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupObservers() {
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                Intent intent = new Intent(iniciar_sesion.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                ViewUtils.showSnackbar(this, error, ViewUtils.MsgType.ERROR);
            }
        });

        authViewModel.getSuccessMessage().observe(this, message -> {
            if (message != null) {
                ViewUtils.showSnackbar(this, message, ViewUtils.MsgType.SUCCESS);
            }
        });

        authViewModel.getIsLoading().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });
    }
}
