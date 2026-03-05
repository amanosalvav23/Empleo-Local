package unc.edu.pe.empleolocal.ui.registro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import unc.edu.pe.empleolocal.data.model.Usuario;
import unc.edu.pe.empleolocal.databinding.ActivityRegistroPaso1Binding;
import unc.edu.pe.empleolocal.ui.auth.AuthViewModel;

public class RegistroPaso1Activity extends AppCompatActivity {

    private ActivityRegistroPaso1Binding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegistroPaso1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnContinue.setOnClickListener(v -> {
            if (validateFields()) {
                String correo = binding.etCorreo.getText().toString().trim().toLowerCase();
                // Verificamos disponibilidad ANTES de permitir pasar al paso 2
                authViewModel.checkEmailAvailability(correo);
            }
        });

        setupObservers();
    }

    private void setupObservers() {
        authViewModel.getIsEmailAvailable().observe(this, isAvailable -> {
            if (isAvailable != null) {
                if (isAvailable) {
                    // El correo está disponible, pasar al paso 2
                    navigateToStep2();
                }
                // Limpiar el estado para que no se dispare al volver
                authViewModel.resetEmailAvailableState();
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && error.contains("registrado")) {
                binding.tilCorreo.setError(error);
            }
        });

        authViewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnContinue.setEnabled(!isLoading);
            binding.btnContinue.setText(isLoading ? "Verificando..." : "Continuar");
        });
    }

    private void navigateToStep2() {
        String nombre = binding.etNombre.getText().toString().trim();
        String apellido = binding.etApellido.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();
        String correo = binding.etCorreo.getText().toString().trim().toLowerCase();
        String password = binding.etPassword.getText().toString().trim();

        Usuario usuario = new Usuario(null, nombre, apellido, telefono, correo);
        
        Intent intent = new Intent(RegistroPaso1Activity.this, RegistroPaso2Activity.class);
        intent.putExtra("user_data", usuario);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    private boolean validateFields() {
        binding.tilNombre.setError(null);
        binding.tilApellido.setError(null);
        binding.tilTelefono.setError(null);
        binding.tilCorreo.setError(null);
        binding.tilPassword.setError(null);

        String nombre = binding.etNombre.getText().toString().trim();
        String apellido = binding.etApellido.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();
        String correo = binding.etCorreo.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        boolean isValid = true;

        if (nombre.isEmpty()) {
            binding.tilNombre.setError("Ingrese su nombre");
            isValid = false;
        }

        if (apellido.isEmpty()) {
            binding.tilApellido.setError("Ingrese su apellido");
            isValid = false;
        }

        if (telefono.isEmpty()) {
            binding.tilTelefono.setError("Ingrese su teléfono");
            isValid = false;
        } else if (telefono.length() != 9 || !telefono.startsWith("9")) {
            binding.tilTelefono.setError("Debe tener 9 dígitos e iniciar con 9");
            isValid = false;
        }

        if (correo.isEmpty()) {
            binding.tilCorreo.setError("Ingrese su correo");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.setError("Correo no válido");
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Ingrese una contraseña");
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Mínimo 6 caracteres");
            isValid = false;
        }

        return isValid;
    }
}
