package unc.edu.pe.empleolocal.ui.postulaciones;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import unc.edu.pe.empleolocal.data.model.Postulacion;
import unc.edu.pe.empleolocal.data.repository.FirebaseRepository;

public class PostulacionesViewModel extends ViewModel {
    private final FirebaseRepository firebaseRepository;
    private final MutableLiveData<List<Postulacion>> postulaciones = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private ListenerRegistration registration;

    public PostulacionesViewModel() {
        this.firebaseRepository = new FirebaseRepository();
        startListening();
    }

    public LiveData<List<Postulacion>> getPostulaciones() {
        return postulaciones;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public void resetUpdateStatus() {
        updateSuccess.setValue(null);
    }

    public void startListening() {
        String uid = firebaseRepository.getCurrentUserUid();
        if (uid == null) {
            error.setValue("Usuario no autenticado");
            return;
        }

        isLoading.setValue(true);
        if (registration != null) registration.remove();

        registration = firebaseRepository.listenPostulacionesUsuario(uid, (value, e) -> {
            if (e != null) {
                error.setValue("Error al cargar: " + e.getMessage());
                isLoading.setValue(false);
                return;
            }

            List<Postulacion> lista = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value) {
                    Postulacion p = doc.toObject(Postulacion.class);
                    if (p != null) {
                        p.setId(doc.getId());
                        lista.add(p);
                    }
                }
            }
            
            // Ordenar localmente por fecha (descendente) para evitar necesidad de índice compuesto
            Collections.sort(lista, (p1, p2) -> Long.compare(p2.getFechaPostulacion(), p1.getFechaPostulacion()));
            
            postulaciones.setValue(lista);
            isLoading.setValue(false);
        });
    }

    public void cancelarPostulacion(String id) {
        if (id == null) return;
        isLoading.setValue(true);
        firebaseRepository.eliminarPostulacion(id)
                .addOnSuccessListener(aVoid -> {
                    updateSuccess.setValue(true);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    error.setValue("Error al eliminar: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (registration != null) registration.remove();
    }
}
