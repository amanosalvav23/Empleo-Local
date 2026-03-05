package unc.edu.pe.empleolocal.ui.lista;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.empleolocal.data.model.Oferta;
import unc.edu.pe.empleolocal.data.model.Postulacion;
import unc.edu.pe.empleolocal.data.repository.FirebaseRepository;
import unc.edu.pe.empleolocal.data.repository.OfertaRepository;
import unc.edu.pe.empleolocal.utils.NotificationHelper;

public class ListaOfertasViewModel extends AndroidViewModel {
    private final OfertaRepository repository;
    private final FirebaseRepository firebaseRepository;
    private final LiveData<List<Oferta>> ofertas;

    private final MutableLiveData<Boolean> applicationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> applicationError = new MutableLiveData<>();

    public ListaOfertasViewModel(@NonNull Application application) {
        super(application);
        repository = new OfertaRepository(application);
        firebaseRepository = new FirebaseRepository();
        ofertas = repository.getOfertasLocales();
    }

    public LiveData<List<Oferta>> getOfertas() {
        return ofertas;
    }

    public LiveData<Boolean> getApplicationSuccess() { return applicationSuccess; }
    public LiveData<String> getApplicationError() { return applicationError; }

    public void postularAOferta(Oferta oferta) {
        String uid = firebaseRepository.getCurrentUserUid();
        if (uid == null) {
            applicationError.setValue("Debe iniciar sesión para postular");
            return;
        }

        firebaseRepository.checkYaPostulo(uid, oferta.getId()).addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                applicationError.setValue("Ya has postulado a esta oferta anteriormente");
            } else {
                Postulacion p = new Postulacion();
                p.setUsuarioId(uid);
                p.setOfertaId(oferta.getId());
                p.setTituloOferta(oferta.getTitulo());
                p.setEmpresaOferta(oferta.getEmpresa());
                p.setLogoUrl(oferta.getLogoUrl());
                p.setDireccionOferta(oferta.getDireccion());
                p.setSalarioOferta(oferta.getSalario());
                p.setFechaPostulacion(System.currentTimeMillis());
                p.setEstado("Postulado");

                firebaseRepository.postular(p).addOnSuccessListener(documentReference -> {
                    NotificationHelper.showPostulacionNotification(getApplication(), oferta.getTitulo());
                    applicationSuccess.setValue(true);
                }).addOnFailureListener(e -> {
                    applicationError.setValue("Error al procesar la postulación: " + e.getMessage());
                });
            }
        });
    }

    public void clearStatus() {
        applicationSuccess.setValue(null);
        applicationError.setValue(null);
    }
}
