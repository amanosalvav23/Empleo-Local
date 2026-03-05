package unc.edu.pe.empleolocal.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import unc.edu.pe.empleolocal.databinding.ActivityBienvenidaBinding;
import unc.edu.pe.empleolocal.ui.registro.RegistroPaso1Activity;

public class BienvenidaActivity extends AppCompatActivity {

    private ActivityBienvenidaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBienvenidaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(BienvenidaActivity.this, iniciar_sesion.class);
            startActivity(intent);
        });

        binding.btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(BienvenidaActivity.this, RegistroPaso1Activity.class);
            startActivity(intent);
        });
    }
}
