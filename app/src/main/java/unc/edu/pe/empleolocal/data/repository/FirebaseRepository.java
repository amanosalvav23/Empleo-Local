package unc.edu.pe.empleolocal.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import unc.edu.pe.empleolocal.data.model.Notificacion;
import unc.edu.pe.empleolocal.data.model.Postulacion;
import unc.edu.pe.empleolocal.data.model.Usuario;

public class FirebaseRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public FirebaseRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> register(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    public Task<SignInMethodQueryResult> checkEmailRegistered(String email) {
        return auth.fetchSignInMethodsForEmail(email);
    }

    public Task<QuerySnapshot> checkUserExistsByEmail(String email) {
        return db.collection("usuarios")
                .whereEqualTo("correo", email)
                .limit(1)
                .get();
    }

    public Task<Void> saveUser(Usuario usuario) {
        return db.collection("usuarios").document(usuario.getUid()).set(usuario);
    }

    public Task<DocumentSnapshot> getUserProfile(String uid) {
        return db.collection("usuarios").document(uid).get();
    }

    public Task<QuerySnapshot> getSectors() {
        return db.collection("sectores").get();
    }

    public Task<Void> updateUserLocation(String uid, double lat, double lng, String address) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("latitud", lat);
        updates.put("longitud", lng);
        updates.put("direccion", address);
        return db.collection("usuarios").document(uid).update(updates);
    }

    public Task<Void> updateUserProfile(String uid, Map<String, Object> data) {
        return db.collection("usuarios").document(uid).set(data, SetOptions.merge());
    }

    public String getCurrentUserUid() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
    
    public void logout() {
        auth.signOut();
    }

    // --- POSTULACIONES ---

    public Task<DocumentReference> postular(Postulacion postulacion) {
        return db.collection("postulaciones").add(postulacion);
    }

    public Task<QuerySnapshot> getPostulacionesUsuario(String uid) {
        return db.collection("postulaciones")
                .whereEqualTo("usuarioId", uid)
                .get();
    }

    public ListenerRegistration listenPostulacionesUsuario(String uid, EventListener<QuerySnapshot> listener) {
        return db.collection("postulaciones")
                .whereEqualTo("usuarioId", uid)
                .addSnapshotListener(listener);
    }

    public Task<QuerySnapshot> checkYaPostulo(String uid, String ofertaId) {
        return db.collection("postulaciones")
                .whereEqualTo("usuarioId", uid)
                .whereEqualTo("ofertaId", ofertaId)
                .limit(1)
                .get();
    }

    public Task<Void> eliminarPostulacion(String id) {
        return db.collection("postulaciones").document(id).delete();
    }

    // --- GUARDADOS ---

    public Task<Void> guardarOferta(String uid, String ofertaId) {
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", System.currentTimeMillis());
        return db.collection("usuarios").document(uid)
                .collection("guardados").document(ofertaId)
                .set(data);
    }

    public Task<Void> eliminarOfertaGuardada(String uid, String ofertaId) {
        return db.collection("usuarios").document(uid)
                .collection("guardados").document(ofertaId)
                .delete();
    }

    public Task<DocumentSnapshot> checkOfertaGuardada(String uid, String ofertaId) {
        return db.collection("usuarios").document(uid)
                .collection("guardados").document(ofertaId).get();
    }

    public ListenerRegistration listenOfertasGuardadas(String uid, EventListener<QuerySnapshot> listener) {
        return db.collection("usuarios").document(uid)
                .collection("guardados")
                .addSnapshotListener(listener);
    }

    // --- NOTIFICACIONES ---

    public Task<DocumentReference> crearNotificacion(Notificacion notificacion) {
        return db.collection("usuarios").document(notificacion.getUsuarioId())
                .collection("notificaciones").add(notificacion);
    }

    public ListenerRegistration listenNotificaciones(String uid, EventListener<QuerySnapshot> listener) {
        return db.collection("usuarios").document(uid)
                .collection("notificaciones")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    public Task<Void> marcarComoLeida(String uid, String notificacionId) {
        return db.collection("usuarios").document(uid)
                .collection("notificaciones").document(notificacionId)
                .update("leida", true);
    }

    public Task<Void> marcarTodasComoLeidas(String uid) {
        // Firestore doesn't support bulk updates easily without a loop or cloud function
        // For simplicity, we'll let the ViewModel handle the batch if needed or just mark one by one
        return db.collection("usuarios").document(uid)
                .collection("notificaciones")
                .whereEqualTo("leida", false)
                .get()
                .continueWithTask(task -> {
                    for (DocumentSnapshot doc : task.getResult()) {
                        doc.getReference().update("leida", true);
                    }
                    return null;
                });
    }
}
