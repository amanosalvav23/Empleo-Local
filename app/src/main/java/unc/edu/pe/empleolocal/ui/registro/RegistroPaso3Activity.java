package unc.edu.pe.empleolocal.ui.registro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import unc.edu.pe.empleolocal.data.model.Usuario;
import unc.edu.pe.empleolocal.databinding.ActivityRegistroPaso3Binding;
import unc.edu.pe.empleolocal.ui.auth.AuthViewModel;
import unc.edu.pe.empleolocal.ui.main.MainActivity;
import unc.edu.pe.empleolocal.utils.NotificationHelper;
import unc.edu.pe.empleolocal.utils.ViewUtils;

public class RegistroPaso3Activity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private Usuario user;
    private String password;
    private ActivityRegistroPaso3Binding binding;

    // Variables para el archivo CV
    private Uri selectedCvUri = null;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB en bytes

    // Launcher para seleccionar el archivo (PDF o DOCX)
    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    validarArchivoSeleccionado(result.getData().getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegistroPaso3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        user = (Usuario) getIntent().getSerializableExtra("user_data");
        password = getIntent().getStringExtra("password");

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        setupObservers();
        authViewModel.fetchSectors();

        // ACCIÓN: Clic en la tarjeta de subida (Imagen/Botón recomendado)
        binding.cardUpload.setOnClickListener(v -> verificarPermisosYAbrirSelector());

        // ACCIÓN: Finalizar registro
        binding.btnFinish.setOnClickListener(v -> {
            if (validarFormulario()) {
                checkNotificationPermissionAndRegister();
            }
        });
    }

    private void verificarPermisosYAbrirSelector() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ no necesita permiso READ_EXTERNAL_STORAGE para el selector de documentos
            abrirSelectorArchivos();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                abrirSelectorArchivos();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
            }
        }
    }

    private void abrirSelectorArchivos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }

    private void validarArchivoSeleccionado(Uri uri) {
        if (uri == null) return;

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

            String fileName = cursor.getString(nameIndex);
            long fileSize = cursor.getLong(sizeIndex);
            cursor.close();

            // Validar extensión
            if (!fileName.toLowerCase().endsWith(".pdf") && !fileName.toLowerCase().endsWith(".docx")) {
                ViewUtils.showSnackbar(this, "Solo se permiten archivos .pdf o .docx", ViewUtils.MsgType.ERROR);
                return;
            }

            // Validar tamaño (5MB)
            if (fileSize > MAX_FILE_SIZE) {
                ViewUtils.showSnackbar(this, "El archivo supera el límite de 5MB", ViewUtils.MsgType.ERROR);
                return;
            }

            // Éxito: Guardar URI y actualizar interfaz
            selectedCvUri = uri;
            binding.uploadCvTitle.setText("¡Archivo listo!"); // Asegúrate que este ID exista en tu XML
            binding.uploadCvHint.setText(fileName);
            ViewUtils.showSnackbar(this, "Archivo cargado correctamente", ViewUtils.MsgType.SUCCESS);
        }
    }

    private boolean validarFormulario() {
        List<String> selectedSectors = new ArrayList<>();
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedSectors.add(chip.getText().toString());
            }
        }

        if (selectedSectors.isEmpty()) {
            ViewUtils.showSnackbar(this, "Selecciona al menos un sector de experiencia", ViewUtils.MsgType.WARNING);
            return false;
        }

        if (selectedCvUri == null) {
            ViewUtils.showSnackbar(this, "Es obligatorio subir tu CV", ViewUtils.MsgType.WARNING);
            return false;
        }

        user.setSectores(selectedSectors);
        return true;
    }

    private void setupObservers() {
        authViewModel.getSectorsLiveData().observe(this, sectors -> {
            binding.chipGroup.removeAllViews();
            for (String sector : sectors) {
                Chip chip = new Chip(this);
                chip.setText(sector);
                chip.setCheckable(true);
                chip.setChipBackgroundColorResource(R.color.selector_chip_background);
                chip.setTextColor(ContextCompat.getColorStateList(this, R.color.selector_chip_text));
                chip.setCheckedIconVisible(true);
                chip.setCheckedIcon(ContextCompat.getDrawable(this, R.drawable.ic_check));
                binding.chipGroup.addView(chip);
            }
        });

        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                NotificationHelper.showWelcomeNotification(this, user.getNombre());
                ViewUtils.showSnackbar(this, "¡Bienvenido! Registro completado", ViewUtils.MsgType.SUCCESS);
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) ViewUtils.showSnackbar(this, error, ViewUtils.MsgType.ERROR);
        });

        authViewModel.getIsLoading().observe(this, loading -> {
            binding.btnFinish.setEnabled(!loading);
            binding.btnFinish.setText(loading ? "Procesando..." : "Finalizar Registro");
        });
    }

    private void checkNotificationPermissionAndRegister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            } else {
                authViewModel.registerWithCv(user, password, selectedCvUri);
            }
        } else {
            authViewModel.registerWithCv(user, password, selectedCvUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 101) authViewModel.registerWithCv(user, password, selectedCvUri);
            if (requestCode == 102) abrirSelectorArchivos();
        }
    }
}