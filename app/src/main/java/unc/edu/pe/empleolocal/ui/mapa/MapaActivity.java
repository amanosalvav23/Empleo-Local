package unc.edu.pe.empleolocal.ui.mapa;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.empleolocal.R;
import unc.edu.pe.empleolocal.data.repository.FirebaseRepository;
import unc.edu.pe.empleolocal.databinding.ActivityMapaEmpleosBinding;
import unc.edu.pe.empleolocal.filtrar_ofertas;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapaEmpleosBinding binding;
    private FirebaseRepository repository;
    private LatLng userLocation = new LatLng(-7.1561, -78.5147);
    
    // Lista para almacenar los sectores filtrados temporalmente sin afectar la BD
    private List<String> temporarySectors = null;

    // Launcher para recibir los resultados de la pantalla de filtros
    private final ActivityResultLauncher<Intent> filterResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Recibimos los sectores seleccionados de forma temporal
                    temporarySectors = result.getData().getStringArrayListExtra("selectedSectors");
                    refreshMapData(); // Refrescamos el mapa inmediatamente
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapaEmpleosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new FirebaseRepository();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Usamos el launcher en lugar de startActivity simple
        binding.fabFilter.setOnClickListener(v -> {
            Intent intent = new Intent(this, filtrar_ofertas.class);
            filterResultLauncher.launch(intent);
        });

        binding.fabMyLocation.setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        });
    }

    private void refreshMapData() {
        String uid = repository.getCurrentUserUid();
        if (uid == null || mMap == null) return;

        repository.getUserProfile(uid).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Actualizar ubicación
                Double lat = documentSnapshot.getDouble("latitud");
                Double lng = documentSnapshot.getDouble("longitud");
                if (lat != null && lng != null && lat != 0) userLocation = new LatLng(lat, lng);

                // Obtener radio (Este sí se actualiza desde Firebase porque filtrar_ofertas lo guarda)
                Object radiusObj = documentSnapshot.get("radioBusqueda");
                int radiusInKm = (radiusObj instanceof Number) ? ((Number) radiusObj).intValue() : 5;
                int radiusInMeters = radiusInKm * 1000;

                mMap.clear(); // Limpiar mapa

                // Marcador de ubicación del usuario (Punto azul)
                mMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .title("Tu ubicación")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                // Dibujar círculo actualizado
                mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(radiusInMeters)
                        .strokeWidth(3)
                        .strokeColor(Color.parseColor("#1A73E8"))
                        .fillColor(Color.argb(40, 26, 115, 232)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, getZoomLevel(radiusInMeters)));

                // Cargar marcadores usando sectores temporales si existen
                loadFilteredMarkers(documentSnapshot, radiusInMeters);
            }
        });
    }

    private void loadFilteredMarkers(DocumentSnapshot userProfile, int radiusInMeters) {
        // Si temporarySectors no es null, usamos esos. Si no, usamos los guardados en el perfil (BD).
        final List<String> sectorsToUse;
        if (temporarySectors != null) {
            sectorsToUse = temporarySectors;
        } else {
            sectorsToUse = (List<String>) userProfile.get("sectores");
        }

        FirebaseFirestore.getInstance().collection("ofertas").get().addOnSuccessListener(querySnapshot -> {
            for (QueryDocumentSnapshot doc : querySnapshot) {
                Double lat = doc.getDouble("latitud");
                Double lng = doc.getDouble("longitud");
                String jobSector = doc.getString("sector");

                if (lat != null && lng != null) {
                    LatLng jobPos = new LatLng(lat, lng);

                    // Filtramos por radio y por sectores (temporales o de BD)
                    if (isWithinRadius(jobPos, radiusInMeters) && 
                        (sectorsToUse == null || sectorsToUse.isEmpty() || sectorsToUse.contains(jobSector))) {
                        mMap.addMarker(new MarkerOptions().position(jobPos).title(doc.getString("titulo")));
                    }
                }
            }
        });
    }

    private boolean isWithinRadius(LatLng jobPos, int maxMeters) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(userLocation.latitude, userLocation.longitude, jobPos.latitude, jobPos.longitude, results);
        return results[0] <= maxMeters;
    }

    private float getZoomLevel(int radiusMeters) {
        double scale = radiusMeters / 500.0;
        return (float) (16 - Math.log(scale) / Math.log(2));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        refreshMapData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshMapData();
    }
}