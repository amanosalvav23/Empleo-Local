package unc.edu.pe.empleolocal.ui.perfil;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unc.edu.pe.empleolocal.data.model.Usuario;
import unc.edu.pe.empleolocal.data.repository.FirebaseRepository;

public class PerfilViewModel extends ViewModel {

    private final FirebaseRepository repository;

    private final MutableLiveData<Usuario> userProfile = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public PerfilViewModel() {
        this.repository = new FirebaseRepository();
    }

    // ────────────────────────────────────────────────
    // Getters
    // ────────────────────────────────────────────────
    public LiveData<Usuario> getUserProfile() {
        return userProfile;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    // ────────────────────────────────────────────────
    // Cargar perfil
    // ────────────────────────────────────────────────
    public void loadUserProfile() {
        String uid = repository.getCurrentUserUid();
        if (uid == null) {
            error.setValue("Sesión expirada. Por favor, inicie sesión.");
            return;
        }

        isLoading.setValue(true);
        repository.getUserProfile(uid).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    try {
                        Usuario usuario = document.toObject(Usuario.class);
                        if (usuario != null) {
                            userProfile.setValue(usuario);
                        } else {
                            error.setValue("Error al convertir los datos del perfil.");
                        }
                    } catch (Exception e) {
                        error.setValue("Formato de datos incompatible: " + e.getMessage());
                    }
                } else {
                    error.setValue("Perfil no encontrado. Complete su registro.");
                }
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error de conexión";
                error.setValue("Error en el servidor: " + errorMsg);
            }
        });
    }

    // ────────────────────────────────────────────────
    // Actualización básica (sin CV)
    // ────────────────────────────────────────────────
    public void updateProfile(String nombre, String apellido, String telefono, List<String> sectores) {
        String uid = repository.getCurrentUserUid();
        if (uid == null) return;

        isLoading.setValue(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("apellido", apellido);
        updates.put("telefono", telefono);
        updates.put("sectores", sectores);

        repository.updateUserProfile(uid, updates).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                updateSuccess.setValue(true);
                loadUserProfile(); // Recargar datos
            } else {
                error.setValue("No se pudo actualizar el perfil.");
            }
        });
    }

    // ────────────────────────────────────────────────
    // Actualización con posible cambio de CV
    // ────────────────────────────────────────────────
    public void updateProfileWithCv(String nombre, String apellido, String telefono,
                                    List<String> sectores, Uri newFileUri, String oldCvUrl) {
        String uid = repository.getCurrentUserUid();
        if (uid == null) return;

        isLoading.setValue(true);

        if (newFileUri != null) {
            // 1. Eliminar CV antiguo si existe
            if (oldCvUrl != null && !oldCvUrl.isEmpty()) {
                try {
                    StorageReference oldRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldCvUrl);
                    oldRef.delete().addOnFailureListener(e -> {
                        // Ignoramos fallo silenciosamente (puede que ya no exista)
                    });
                } catch (Exception ignored) {
                    // URL inválida o ya eliminada → continuamos
                }
            }

            // 2. Subir nuevo CV
            String fileName = "cv_" + uid + "_" + System.currentTimeMillis() + ".pdf";
            StorageReference newFileRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child("curriculums/" + fileName);

            newFileRef.putFile(newFileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        newFileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // 3. Guardar todo en Firestore
                            actualizarDatosFirestore(uid, nombre, apellido, telefono, sectores, uri.toString());
                        }).addOnFailureListener(e -> {
                            isLoading.setValue(false);
                            error.setValue("No se pudo obtener URL del nuevo CV: " + e.getMessage());
                        });
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        error.setValue("Error al subir el CV: " + e.getMessage());
                    });
        } else {
            // No hay nuevo archivo → solo actualizamos datos textuales
            actualizarDatosFirestore(uid, nombre, apellido, telefono, sectores, oldCvUrl);
        }
    }

    // ────────────────────────────────────────────────
    // Helper para guardar/actualizar en Firestore
    // ────────────────────────────────────────────────
    private void actualizarDatosFirestore(String uid, String nombre, String apellido,
                                          String telefono, List<String> sectores, String cvUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("apellido", apellido);
        updates.put("telefono", telefono);
        updates.put("sectores", sectores);
        if (cvUrl != null) {
            updates.put("cvUrl", cvUrl);
        }

        repository.updateUserProfile(uid, updates).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                updateSuccess.setValue(true);
                loadUserProfile(); // Refrescar vista
            } else {
                error.setValue("Error al guardar los cambios en el servidor.");
            }
        });
    }

    // ────────────────────────────────────────────────
    // Actualizar ubicación (ya existía)
    // ────────────────────────────────────────────────
    public void updateLocation(double lat, double lng, String address, int radius) {
        String uid = repository.getCurrentUserUid();
        if (uid == null) return;

        isLoading.setValue(true);
        Map<String, Object> updates = new HashMap<>();
        updates.put("latitud", lat);
        updates.put("longitud", lng);
        updates.put("direccion", address);
        updates.put("radioBusqueda", radius);

        repository.updateUserProfile(uid, updates).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                updateSuccess.setValue(true);
                loadUserProfile();
            } else {
                error.setValue("Error al actualizar la ubicación.");
            }
        });
    }

    // ────────────────────────────────────────────────
    // Resetear estado de éxito (útil tras mostrar mensaje)
    // ────────────────────────────────────────────────
    public void resetUpdateStatus() {
        updateSuccess.setValue(null);
    }
}