package unc.edu.pe.empleolocal.ui.guardadas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import unc.edu.pe.empleolocal.databinding.ActivityEmpleosGuardadosBinding;
import unc.edu.pe.empleolocal.utils.ViewUtils;

public class EmpleosGuardadosActivity extends AppCompatActivity {

    private ActivityEmpleosGuardadosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEmpleosGuardadosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRecyclerView();
        setupListeners();
    }

    private void setupRecyclerView() {
        binding.rvSavedJobs.setLayoutManager(new LinearLayoutManager(this));
        // Aquí conectarías el adaptador real cuando tengas la lógica de persistencia
    }

    private void setupListeners() {
        binding.tvSyncNow.setOnClickListener(v -> {
            ViewUtils.showSnackbar(this, "Sincronizando con la nube...", ViewUtils.MsgType.INFO);
        });
    }
}
