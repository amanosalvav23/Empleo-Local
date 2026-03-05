package unc.edu.pe.empleolocal.ui.main;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import unc.edu.pe.empleolocal.data.model.Notificacion;
import unc.edu.pe.empleolocal.data.model.Oferta;
import unc.edu.pe.empleolocal.data.model.Postulacion;
import unc.edu.pe.empleolocal.data.repository.FirebaseRepository;
import unc.edu.pe.empleolocal.data.repository.OfertaRepository;
import unc.edu.pe.empleolocal.utils.NotificationHelper;

public class OfertaViewModel extends AndroidViewModel {
    private final OfertaRepository repository;
    private final FirebaseRepository firebaseRepository;
    private final LiveData<List<Oferta>> ofertas;
    private final MutableLiveData<List<String>> sectores = new MutableLiveData<>();
    
    private final MutableLiveData<String> selectedSector = new MutableLiveData<>("");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> customRadius = new MutableLiveData<>(-1);

    private final MutableLiveData<Boolean> applicationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> applicationError = new MutableLiveData<>();
    private final MutableLiveData<List<Postulacion>> postulaciones = new MutableLiveData<>();

    // Guardados
    private final MutableLiveData<List<String>> savedIds = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isCurrentSaved = new MutableLiveData<>(false);
    private final MutableLiveData<String> saveActionMessage = new MutableLiveData<>();
    private String currentViewingId;
    private ListenerRegistration savedListener;
    private FirebaseAuth.AuthStateListener authListener;

    public OfertaViewModel(@NonNull Application application) {
        super(application);
        repository = new OfertaRepository(application);
        firebaseRepository = new FirebaseRepository();
        ofertas = repository.getOfertasLocales();
        loadSectors();
        setupAuthListener();
    }

    private void setupAuthListener() {
        authListener = firebaseAuth -> {
            String uid = firebaseAuth.getUid();
            if (uid != null) {
                listenToSaved(uid);
            } else {
                stopListeningSaved();
                savedIds.setValue(new ArrayList<>());
                isCurrentSaved.setValue(false);
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
    }

    public LiveData<List<Oferta>> getOfertas() { return ofertas; }
    public LiveData<List<String>> getSectores() { return sectores; }

    public void refreshOfertas() { repository.refreshOfertas(); }

    public void loadSectors() {
        firebaseRepository.getSectors().addOnSuccessListener(queryDocumentSnapshots -> {
            List<String> listaSectores = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                String nombre = doc.getString("nombre");
                if (nombre != null) listaSectores.add(nombre);
            }
            sectores.setValue(listaSectores);
        });
    }

    public LiveData<String> getSelectedSector() { return selectedSector; }
    public void setSelectedSector(String sector) { 
        if (selectedSector.getValue() == null || !selectedSector.getValue().equals(sector)) {
            selectedSector.setValue(sector);
        }
    }

    public LiveData<String> getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String query) { 
        if (searchQuery.getValue() == null || !searchQuery.getValue().equals(query)) {
            searchQuery.setValue(query);
        }
    }
    
    public LiveData<Integer> getCustomRadius() { return customRadius; }
    public void setCustomRadius(int radius) { customRadius.setValue(radius); }

    public LiveData<Boolean> getApplicationSuccess() { return applicationSuccess; }
    public LiveData<String> getApplicationError() { return applicationError; }
    public LiveData<List<Postulacion>> getPostulaciones() { return postulaciones; }
    public LiveData<String> getSaveActionMessage() { return saveActionMessage; }

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
                    // 1. Mostrar notificación en el celular
                    NotificationHelper.showPostulacionNotification(getApplication(), oferta.getTitulo());
                    
                    // 2. Guardar notificación en Firestore para la pantalla de notificaciones
                    Notificacion notif = new Notificacion(
                            "¡Postulación Exitosa!",
                            "Has postulado correctamente a: " + oferta.getTitulo(),
                            "POSTULACION",
                            uid
                    );
                    firebaseRepository.crearNotificacion(notif);

                    applicationSuccess.setValue(true);
                    loadPostulaciones();
                }).addOnFailureListener(e -> {
                    applicationError.setValue("Error al procesar la postulación: " + e.getMessage());
                });
            }
        });
    }

    public void loadPostulaciones() {
        String uid = firebaseRepository.getCurrentUserUid();
        if (uid == null) return;
        firebaseRepository.getPostulacionesUsuario(uid).addOnSuccessListener(queryDocumentSnapshots -> {
            List<Postulacion> lista = new ArrayList<>();
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    Postulacion p = doc.toObject(Postulacion.class);
                    if (p != null) {
                        p.setId(doc.getId());
                        lista.add(p);
                    }
                }
            }
            postulaciones.setValue(lista);
        });
    }

    // --- GUARDADOS ---

    public LiveData<List<String>> getSavedIds() { return savedIds; }
    public LiveData<Boolean> getIsCurrentSaved() { return isCurrentSaved; }

    private void listenToSaved(String uid) {
        if (savedListener != null) return;

        Log.d("OFERTA_VM", "Iniciando listener de guardados para UID: " + uid);
        savedListener = firebaseRepository.listenOfertasGuardadas(uid, (value, error) -> {
            if (error != null) {
                Log.e("OFERTA_VM", "Error en listener: " + error.getMessage());
                return;
            }
            if (value != null) {
                List<String> ids = new ArrayList<>();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    ids.add(doc.getId());
                }
                Log.d("OFERTA_VM", "Firestore actualizó guardados. Total: " + ids.size());
                savedIds.setValue(ids); // setValue es para el hilo principal
                
                if (currentViewingId != null) {
                    boolean isSaved = ids.contains(currentViewingId);
                    Log.d("OFERTA_VM", "¿Oferta actual " + currentViewingId + " guardada? " + isSaved);
                    isCurrentSaved.setValue(isSaved);
                }
            }
        });
    }

    private void stopListeningSaved() {
        if (savedListener != null) {
            savedListener.remove();
            savedListener = null;
        }
    }

    public void checkIsSaved(String ofertaId) {
        this.currentViewingId = ofertaId;
        List<String> ids = savedIds.getValue();
        boolean saved = ids != null && ids.contains(ofertaId);
        isCurrentSaved.setValue(saved);
        Log.d("OFERTA_VM", "checkIsSaved local: " + saved + " para ID " + ofertaId);
    }

    public void toggleGuardar(String ofertaId) {
        String uid = firebaseRepository.getCurrentUserUid();
        if (uid == null) {
            applicationError.setValue("Inicie sesión para guardar");
            return;
        }
        if (ofertaId == null) return;

        List<String> ids = savedIds.getValue();
        boolean currentlySaved = ids != null && ids.contains(ofertaId);
        
        Log.d("OFERTA_VM", "Ejecutando toggleGuardar. Estado actual: " + currentlySaved);

        if (currentlySaved) {
            firebaseRepository.eliminarOfertaGuardada(uid, ofertaId)
                .addOnSuccessListener(v -> {
                    saveActionMessage.setValue("Oferta eliminada de guardados");
                })
                .addOnFailureListener(e -> applicationError.setValue("Error al eliminar"));
        } else {
            firebaseRepository.guardarOferta(uid, ofertaId)
                .addOnSuccessListener(v -> {
                    saveActionMessage.setValue("Oferta guardada exitosamente");
                })
                .addOnFailureListener(e -> applicationError.setValue("Error al guardar"));
        }
    }

    public void clearStatus() {
        applicationSuccess.setValue(null);
        applicationError.setValue(null);
        saveActionMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopListeningSaved();
        if (authListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
        }
    }
}
