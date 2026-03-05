package unc.edu.pe.empleolocal.ui.notificaciones;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.empleolocal.data.model.Notificacion;
import unc.edu.pe.empleolocal.data.repository.FirebaseRepository;

public class NotificacionesViewModel extends ViewModel {
    private final FirebaseRepository repository;
    private final MutableLiveData<List<Notificacion>> notificaciones = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private ListenerRegistration registration;

    public NotificacionesViewModel() {
        this.repository = new FirebaseRepository();
        startListening();
    }

    public LiveData<List<Notificacion>> getNotificaciones() {
        return notificaciones;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void startListening() {
        String uid = repository.getCurrentUserUid();
        if (uid == null) return;

        if (registration != null) registration.remove();

        registration = repository.listenNotificaciones(uid, (value, e) -> {
            if (e != null) {
                error.setValue(e.getMessage());
                return;
            }

            List<Notificacion> list = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Notificacion n = doc.toObject(Notificacion.class);
                    if (n != null) {
                        n.setId(doc.getId());
                        list.add(n);
                    }
                }
            }
            notificaciones.setValue(list);
        });
    }

    public void marcarComoLeida(String notificacionId) {
        String uid = repository.getCurrentUserUid();
        if (uid != null) {
            repository.marcarComoLeida(uid, notificacionId);
        }
    }

    public void marcarTodasComoLeidas() {
        String uid = repository.getCurrentUserUid();
        if (uid != null) {
            repository.marcarTodasComoLeidas(uid);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (registration != null) registration.remove();
    }
}
