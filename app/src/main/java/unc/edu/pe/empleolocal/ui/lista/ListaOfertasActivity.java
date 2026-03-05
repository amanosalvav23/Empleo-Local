package unc.edu.pe.empleolocal.ui.lista;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import unc.edu.pe.empleolocal.databinding.ActivityListaOfertasBinding;
import unc.edu.pe.empleolocal.ui.main.MainActivity;
import unc.edu.pe.empleolocal.utils.ViewUtils;

public class ListaOfertasActivity extends AppCompatActivity {

    private ActivityListaOfertasBinding binding;
    private MainActivity.RealOfertaAdapter adapter;
    private ListaOfertasViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityListaOfertasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(ListaOfertasViewModel.class);
        setupRecyclerView();
        setupObservers();
    }

    private void setupRecyclerView() {
        binding.rvOfertas.setLayoutManager(new LinearLayoutManager(this));
        // Pasamos 'null' a RealOfertaAdapter ya que aquí no necesitamos las funciones de navegación de MainActivity
        adapter = new MainActivity.RealOfertaAdapter(null);
        binding.rvOfertas.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getOfertas().observe(this, ofertas -> {
            if (ofertas != null) adapter.setOfertas(ofertas);
        });

        viewModel.getApplicationSuccess().observe(this, success -> {
            if (success != null && success) {
                ViewUtils.showSnackbar(this, "¡Postulación exitosa!", ViewUtils.MsgType.SUCCESS);
                viewModel.clearStatus();
            }
        });

        viewModel.getApplicationError().observe(this, error -> {
            if (error != null) {
                ViewUtils.showSnackbar(this, error, ViewUtils.MsgType.ERROR);
                viewModel.clearStatus();
            }
        });
    }
}
