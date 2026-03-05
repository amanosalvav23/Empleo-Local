package unc.edu.pe.empleolocal.ui.auth;

import android.net.Uri;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import unc.edu.pe.empleolocal.data.model.Usuario;
import unc.edu.pe.empleolocal.data.repository.FirebaseRepository;

public class AuthViewModel extends ViewModel {
    private final FirebaseRepository repository;
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEmailAvailable = new MutableLiveData<>();
    private final MutableLiveData<List<String>> sectorsLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        this.repository = new FirebaseRepository();
    }

    public LiveData<FirebaseUser> getUserLiveData() { return userLiveData; }
    public LiveData<String> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<Boolean> getIsEmailAvailable() { return isEmailAvailable; }
    public LiveData<List<String>> getSectorsLiveData() { return sectorsLiveData; }

    public void login(String email, String password) {
        isLoading.setValue(true);
        repository.login(email, password).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful() && task.getResult() != null) {
                userLiveData.setValue(task.getResult().getUser());
            } else {
                errorLiveData.setValue("Correo o contraseña incorrectos");
            }
        });
    }

    public void checkEmailAvailability(String email) {
        isLoading.setValue(true);
        repository.checkEmailRegistered(email).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful() && task.getResult() != null) {
                boolean exists = task.getResult().getSignInMethods() != null && 
                                !task.getResult().getSignInMethods().isEmpty();
                if (exists) {
                    errorLiveData.setValue("Este correo electrónico ya está registrado");
                    isEmailAvailable.setValue(false);
                } else {
                    isEmailAvailable.setValue(true);
                }
            } else {
                isEmailAvailable.setValue(true); 
            }
        });
    }

    public void resetEmailAvailableState() {
        isEmailAvailable.setValue(null);
    }

    public void fetchSectors() {
        isLoading.setValue(true);
        repository.getSectors().addOnCompleteListener(task -> {
            isLoading.setValue(false);
            List<String> sectors = new ArrayList<>();
            
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                // Si hay datos en Firebase, los usamos
                for (DocumentSnapshot doc : task.getResult()) {
                    String name = doc.getString("nombre");
                    if (name != null) sectors.add(name);
                }
                sectorsLiveData.setValue(sectors);
            } else {
                // FALLBACK: Si Firebase está vacío o hay error de permisos, usamos la lista profesional
                sectors.addAll(Arrays.asList(
                    "Ingeniería", "Minería", "Salud", "Educación", 
                    "Agropecuario", "Turismo", "Gastronomía", 
                    "Construcción", "Comercio", "Tecnología", 
                    "Finanzas", "Administración", "Ventas"
                ));
                sectorsLiveData.setValue(sectors);
            }
        });
    }

    public void register(Usuario usuarioData, String password) {
        isLoading.setValue(true);
        repository.register(usuarioData.getCorreo(), password).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String uid = task.getResult().getUser().getUid();
                usuarioData.setUid(uid);
                repository.saveUser(usuarioData).addOnCompleteListener(saveTask -> {
                    isLoading.setValue(false);
                    if (saveTask.isSuccessful()) {
                        userLiveData.setValue(task.getResult().getUser());
                    } else {
                        errorLiveData.setValue("Error al crear el perfil en la base de datos.");
                    }
                });
            } else {
                isLoading.setValue(false);
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    errorLiveData.setValue("Este correo electrónico ya está registrado");
                } else {
                    errorLiveData.setValue("No se pudo completar el registro. Intente nuevamente.");
                }
            }
        });
    }

    public void resetPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            errorLiveData.setValue("Ingrese su correo electrónico");
            return;
        }
        String cleanEmail = email.trim().toLowerCase();
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            errorLiveData.setValue("El formato del correo no es válido");
            return;
        }
        isLoading.setValue(true);
        repository.sendPasswordResetEmail(cleanEmail).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                successMessage.setValue("Si el correo ingresado se encuentra registrado, recibirás un mensaje para restablecer tu contraseña.");
            } else {
                errorLiveData.setValue("No se pudo procesar la solicitud.");
            }
        });
    }
    public void registerWithCv(Usuario user, String password, Uri fileUri) {
        isLoading.setValue(true);

        // 1. Limpiar el correo para usarlo como nombre de archivo
        // Reemplaza puntos y arrobas para que sea un nombre de archivo válido
        String safeEmail = user.getCorreo().replace(".", "_").replace("@", "_");
        String fileName = "cv_" + safeEmail + "_" + System.currentTimeMillis();

        // 2. Crear la referencia completa al archivo dentro de la carpeta "curriculums"
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("curriculums/" + fileName);

        // 3. Subir archivo
        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // 4. Obtener la URL de descarga
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // Guardamos la URL en el objeto user
                        user.setCvUrl(downloadUrl);

                        // 5. Proceder con el registro normal
                        register(user, password);
                    });
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorLiveData.setValue("Error al subir CV: " + e.getMessage());
                });
    }
}
