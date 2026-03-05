package unc.edu.pe.empleolocal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import unc.edu.pe.empleolocal.data.repository.FirebaseRepository;
import unc.edu.pe.empleolocal.databinding.ActivityFiltrarOfertasBinding;

public class filtrar_ofertas extends AppCompatActivity {
    private ActivityFiltrarOfertasBinding binding;
    private FirebaseRepository repository;
    private List<String> selectedSectors = new ArrayList<>();
    private boolean isUpdatingUI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFiltrarOfertasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new FirebaseRepository();

        setupUI();
        loadSectorsAndFilters(); // Jalar sectores disponibles y datos del usuario desde Firebase

        binding.btnApply.setOnClickListener(v -> applyFilters());
        
        binding.btnClearFilters.setOnClickListener(v -> {
            // "Restablecer" regresa a los sectores que están guardados en Firebase
            loadCurrentFiltersFromFirebase();
            Toast.makeText(this, "Sectores restablecidos a los de tu perfil", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupUI() {
        // Listener del slider para actualizar el texto del radio en km
        binding.sliderRadius.addOnChangeListener((slider, value, fromUser) -> 
            binding.tvRadiusVal.setText((int) value + " km"));
        
        binding.sliderSalary.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            binding.tvMinSalary.setText("S/. " + (int) (float) values.get(0));
            binding.tvMaxSalary.setText("S/. " + (int) (float) values.get(1));
        });
    }

    private void loadSectorsAndFilters() {
        // 1. Cargar todos los sectores posibles de la colección "sectores" para crear los chips
        repository.getSectors().addOnSuccessListener(querySnapshot -> {
            binding.cgSectors.removeAllViews();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String name = doc.getString("nombre");
                if (name == null) name = doc.getId(); // Fallback al ID del documento si no hay campo 'nombre'
                
                final String sectorNameFinal = name;
                Chip chip = new Chip(this);
                chip.setText(sectorNameFinal);
                chip.setCheckable(true);
                chip.setTag(sectorNameFinal);
                
                chip.setOnCheckedChangeListener((bv, isChecked) -> {
                    if (isUpdatingUI) return;
                    if (isChecked) {
                        if (!selectedSectors.contains(sectorNameFinal)) selectedSectors.add(sectorNameFinal);
                    } else {
                        selectedSectors.remove(sectorNameFinal);
                    }
                });
                
                binding.cgSectors.addView(chip);
            }
            // 2. Una vez creados los chips, jalar los datos del usuario para marcarlos
            loadCurrentFiltersFromFirebase();
        }).addOnFailureListener(e -> Log.e("FiltrarOfertas", "Error al cargar sectores", e));
    }

    private void loadCurrentFiltersFromFirebase() {
        isUpdatingUI = true;
        String uid = repository.getCurrentUserUid();
        if (uid == null) {
            isUpdatingUI = false;
            return;
        }

        repository.getUserProfile(uid).addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // --- JALAR RADIO (KM) DE FIREBASE ---
                Object radObj = doc.get("radioBusqueda");
                int radius = 5; // Valor por defecto
                if (radObj instanceof Number) {
                    radius = ((Number) radObj).intValue();
                }
                
                // Actualizar Slider y Texto en la pantalla
                float sliderValue = (float) Math.max(1, Math.min(20, radius));
                binding.sliderRadius.setValue(sliderValue);
                binding.tvRadiusVal.setText((int) sliderValue + " km");

                // --- JALAR SECTORES DEL USUARIO DE FIREBASE ---
                List<String> userSectors = (List<String>) doc.get("sectores");
                selectedSectors.clear();
                if (userSectors != null) {
                    selectedSectors.addAll(userSectors);
                }

                // --- MARCAR LOS CHIPS CORRESPONDIENTES ---
                for (int i = 0; i < binding.cgSectors.getChildCount(); i++) {
                    Chip chip = (Chip) binding.cgSectors.getChildAt(i);
                    String chipSector = (String) chip.getTag();
                    chip.setChecked(selectedSectors.contains(chipSector));
                }
            }
            isUpdatingUI = false;
        }).addOnFailureListener(e -> {
            isUpdatingUI = false;
            Log.e("FiltrarOfertas", "Error al cargar perfil del usuario", e);
        });
    }

    private void applyFilters() {
        String uid = repository.getCurrentUserUid();
        if (uid == null) return;

        // 1. ACTUALIZAR KM EN FIREBASE (Se guarda permanentemente)
        int radius = (int) binding.sliderRadius.getValue();
        Map<String, Object> updates = new HashMap<>();
        updates.put("radioBusqueda", radius);

        repository.updateUserProfile(uid, updates).addOnSuccessListener(aVoid -> {
            // 2. ENVIAR SECTORES AL MAPA (Se envían los que están marcados ahora, sean o no los de la BD)
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("selectedSectors", new ArrayList<>(selectedSectors));
            setResult(RESULT_OK, resultIntent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al guardar el radio en Firebase", Toast.LENGTH_SHORT).show();
        });
    }
}
