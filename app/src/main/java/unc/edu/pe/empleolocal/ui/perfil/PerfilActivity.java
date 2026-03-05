package unc.edu.pe.empleolocal.ui.perfil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.empleolocal.R;
import unc.edu.pe.empleolocal.data.model.Usuario;
import unc.edu.pe.empleolocal.databinding.ActivityEditarPerfilBinding;
import unc.edu.pe.empleolocal.databinding.ActivityPerfilBinding;
import unc.edu.pe.empleolocal.ui.auth.iniciar_sesion;
import unc.edu.pe.empleolocal.utils.ViewUtils;

public class PerfilActivity extends AppCompatActivity {

    private ActivityPerfilBinding binding;
    private PerfilViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPerfilBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
        setupObservers();
        viewModel.loadUserProfile();
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, iniciar_sesion.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupObservers() {
        viewModel.getUserProfile().observe(this, this::updateUI);
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                ViewUtils.showSnackbar(this, error, ViewUtils.MsgType.ERROR);
            }
        });
    }

    private void updateUI(Usuario usuario) {
        if (usuario == null) return;

        // Nombre e iniciales
        binding.tvUserName.setText(usuario.getNombre() + " " + usuario.getApellido());
        String initials = "";
        if (usuario.getNombre() != null && !usuario.getNombre().isEmpty())
            initials += usuario.getNombre().charAt(0);
        if (usuario.getApellido() != null && !usuario.getApellido().isEmpty())
            initials += usuario.getApellido().charAt(0);
        binding.tvUserInitials.setText(initials.toUpperCase());

        // Ubicación y radio
        binding.tvUserLocation.setText(usuario.getDireccion());
        binding.tvLocationAddress.setText(usuario.getDireccion());
        binding.tvLocationRadio.setText(usuario.getRadioBusqueda() + " km");

        // Sectores
        binding.cgSectores.removeAllViews();
        if (usuario.getSectores() != null) {
            for (String sector : usuario.getSectores()) {
                Chip chip = new Chip(this);
                chip.setText(sector);
                chip.setChipBackgroundColorResource(android.R.color.transparent);
                chip.setChipStrokeWidth(1f);
                chip.setChipStrokeColorResource(R.color.login_blue_text);
                chip.setTextColor(ContextCompat.getColor(this, R.color.login_blue_text));
                binding.cgSectores.addView(chip);
            }
        }

        // Manejo del CV
        if (usuario.getCvUrl() != null && !usuario.getCvUrl().isEmpty()) {
            binding.tvCvName.setText("Currículum Vitae Cargado");
            binding.tvViewCv.setVisibility(View.VISIBLE);
            binding.tvViewCv.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(usuario.getCvUrl()));
                startActivity(intent);
            });
        } else {
            binding.tvCvName.setText("Aún no has subido un CV");
            binding.tvViewCv.setVisibility(View.GONE);
        }
    }

    // =============================================================================
    //                       EditarPerfilActivity (anidada o separada)
    // =============================================================================

    public static class EditarPerfilActivity extends AppCompatActivity {

        private ActivityEditarPerfilBinding binding;
        private PerfilViewModel viewModel;
        private Uri selectedPdfUri;
        private String currentCvUrl;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            binding = ActivityEditarPerfilBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);

            setupObservers();

            // Cargar datos actuales
            viewModel.loadUserProfile();

            // Subir CV
            binding.btnUploadCv.setOnClickListener(v -> selectPdf());

            // Guardar cambios
            binding.btnSaveChanges.setOnClickListener(v -> saveChanges());
        }

        private void setupObservers() {
            viewModel.getUserProfile().observe(this, usuario -> {
                if (usuario == null) return;

                binding.etNombre.setText(usuario.getNombre());
                binding.etApellido.setText(usuario.getApellido());
                binding.etTelefono.setText(usuario.getTelefono());
                binding.etCorreo.setText(usuario.getCorreo());

                currentCvUrl = usuario.getCvUrl();

                if (currentCvUrl != null && !currentCvUrl.isEmpty()) {
                    binding.tvCvName.setText("CV actual cargado");
                    binding.tvCvName.setTextColor(ContextCompat.getColor(this, R.color.login_blue_text));
                } else {
                    binding.tvCvName.setText("No se encontró CV previo");
                }
            });

            viewModel.getUpdateSuccess().observe(this, success -> {
                if (success != null && success) {
                    Toast.makeText(this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            viewModel.getError().observe(this, error -> {
                if (error != null) {
                    ViewUtils.showSnackbar(this, error, ViewUtils.MsgType.ERROR);
                }
            });
        }

        private void selectPdf() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(Intent.createChooser(intent, "Selecciona tu CV (PDF)"), 101);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
                selectedPdfUri = data.getData();
                binding.tvCvName.setText("Nuevo archivo seleccionado");
                binding.tvCvName.setTextColor(ContextCompat.getColor(this, R.color.login_blue_text));
            }
        }

        private void saveChanges() {
            String nombre = binding.etNombre.getText().toString().trim();
            String apellido = binding.etApellido.getText().toString().trim();
            String telefono = binding.etTelefono.getText().toString().trim();

            if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty()) {
                ViewUtils.showSnackbar(this, "Completa los campos obligatorios", ViewUtils.MsgType.WARNING);
                return;
            }

            List<String> sectores = new ArrayList<>();
            for (int i = 0; i < binding.cgSectores.getChildCount(); i++) {
                View v = binding.cgSectores.getChildAt(i);
                if (v instanceof Chip) {
                    Chip chip = (Chip) v;
                    if (chip.isChecked()) {
                        sectores.add(chip.getText().toString());
                    }
                }
            }

            viewModel.updateProfileWithCv(nombre, apellido, telefono, sectores, selectedPdfUri, currentCvUrl);
        }
    }
}