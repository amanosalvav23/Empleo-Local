package unc.edu.pe.empleolocal.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import unc.edu.pe.empleolocal.R;
import unc.edu.pe.empleolocal.adapter.NotificacionesAdapter;
import unc.edu.pe.empleolocal.adapter.PostulacionesAdapter;
import unc.edu.pe.empleolocal.data.model.Notificacion;
import unc.edu.pe.empleolocal.data.model.Oferta;
import unc.edu.pe.empleolocal.data.model.Postulacion;
import unc.edu.pe.empleolocal.data.model.Usuario;
import unc.edu.pe.empleolocal.databinding.ActivityDetalleOfertaBinding;
import unc.edu.pe.empleolocal.databinding.ActivityEditarPerfilBinding;
import unc.edu.pe.empleolocal.databinding.ActivityFiltrarOfertasBinding;
import unc.edu.pe.empleolocal.databinding.ActivityInicioBinding;
import unc.edu.pe.empleolocal.databinding.ActivityMainBinding;
import unc.edu.pe.empleolocal.databinding.ActivityMapaEmpleosBinding;
import unc.edu.pe.empleolocal.databinding.ActivityNotificacionesBinding;
import unc.edu.pe.empleolocal.databinding.ActivityPerfilBinding;
import unc.edu.pe.empleolocal.databinding.ActivityPerfilEmpresaBinding;
import unc.edu.pe.empleolocal.databinding.ActivityPostulacionesBinding;
import unc.edu.pe.empleolocal.databinding.ActivitySeguimientoPostulacionBinding;
import unc.edu.pe.empleolocal.databinding.ItemOfertaBinding;
import unc.edu.pe.empleolocal.databinding.LayoutLocationSelectionBinding;
import unc.edu.pe.empleolocal.ui.auth.AuthViewModel;
import unc.edu.pe.empleolocal.ui.auth.iniciar_sesion;
import unc.edu.pe.empleolocal.ui.notificaciones.NotificacionesViewModel;
import unc.edu.pe.empleolocal.ui.perfil.PerfilViewModel;
import unc.edu.pe.empleolocal.ui.postulaciones.PostulacionesViewModel;
import unc.edu.pe.empleolocal.utils.NotificationHelper;
import unc.edu.pe.empleolocal.utils.ViewUtils;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private OfertaViewModel ofertaViewModel;
    private NotificacionesViewModel notificacionesViewModel;
    private final InicioFragment inicioFragment = new InicioFragment();
    private final MapaFragment mapaFragment = new MapaFragment();
    private final PostulacionesFragment postulacionesFragment = new PostulacionesFragment();
    private final PerfilFragment perfilFragment = new PerfilFragment();
    private Fragment activeFragment = inicioFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ofertaViewModel = new ViewModelProvider(this).get(OfertaViewModel.class);
        notificacionesViewModel = new ViewModelProvider(this).get(NotificacionesViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupNavigation();
        setupToolbarActions();
        handleBackPress();
        setupGlobalObservers();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, perfilFragment, "4").hide(perfilFragment)
                    .add(R.id.fragment_container, postulacionesFragment, "3").hide(postulacionesFragment)
                    .add(R.id.fragment_container, mapaFragment, "2").hide(mapaFragment)
                    .add(R.id.fragment_container, inicioFragment, "1")
                    .commit();
            updateToolbar("EmpleoLocal", true, false, false, false, false, false);
        }
    }

    private void setupGlobalObservers() {
        ofertaViewModel.getIsCurrentSaved().observe(this, isSaved -> {
            Log.d("MAIN_ACTIVITY", "Icono Bookmark actualización. ¿Guardado? " + isSaved);
            binding.ivBookmark.setImageResource(isSaved ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
            binding.ivBookmark.setColorFilter(isSaved ? Color.BLACK : Color.GRAY);
        });

        ofertaViewModel.getSaveActionMessage().observe(this, message -> {
            if (message != null) {
                ViewUtils.showSnackbar(this, message, ViewUtils.MsgType.SUCCESS);
                ofertaViewModel.clearStatus();
            }
        });

        ofertaViewModel.getApplicationError().observe(this, error -> {
            if (error != null) {
                ViewUtils.showSnackbar(this, error, ViewUtils.MsgType.ERROR);
                ofertaViewModel.clearStatus();
            }
        });

        notificacionesViewModel.getNotificaciones().observe(this, list -> {
            if (list != null) {
                int unreadCount = 0;
                for (Notificacion n : list) {
                    if (!n.isLeida()) unreadCount++;
                }
                updateNotificationBadge(unreadCount);
            }
        });
    }

    private void updateNotificationBadge(int count) {
        if (count > 0) {
            binding.tvNotificationBadge.setVisibility(View.VISIBLE);
            binding.tvNotificationBadge.setText(String.valueOf(count));
        } else {
            binding.tvNotificationBadge.setVisibility(View.GONE);
        }
    }

    private void setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                updateToolbar("EmpleoLocal", true, false, false, false, false, false);
                showFragment(inicioFragment);
                return true;
            } else if (id == R.id.nav_map) {
                updateToolbar("Mapa de Empleos", true, false, false, false, false, false);
                showFragment(mapaFragment);
                return true;
            } else if (id == R.id.nav_apply) {
                updateToolbar("Mis Postulaciones", true, false, false, false, false, false);
                showFragment(postulacionesFragment);
                return true;
            } else if (id == R.id.nav_profile) {
                updateToolbar("Mi Perfil", false, true, false, false, false, false);
                showFragment(perfilFragment);
                return true;
            }
            return false;
        });
    }

    private void setupToolbarActions() {
        binding.ivBackButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.flNotifications.setOnClickListener(v -> {
            updateToolbar("Notificaciones", false, false, true, false, true, false);
            loadSubFragment(new NotificacionesFragment());
        });

        binding.ivEditProfile.setOnClickListener(v -> {
            updateToolbar("Editar Perfil", false, false, true, false, false, false);
            loadSubFragment(new EditarPerfilFragment());
        });

        binding.ivBookmark.setOnClickListener(v -> {
            if (activeFragment instanceof DetalleOfertaFragment) {
                Oferta current = ((DetalleOfertaFragment) activeFragment).getOferta();
                if (current != null) {
                    ofertaViewModel.toggleGuardar(current.getId());
                }
            }
        });

        binding.tvMarkReadButton.setOnClickListener(v -> {
            if (activeFragment instanceof NotificacionesFragment) {
                ((NotificacionesFragment) activeFragment).markAllRead();
            }
        });
    }

    private void handleBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    syncUIAfterBack();
                } else {
                    finish();
                }
            }
        });
    }

    private void showFragment(Fragment fragment) {
        if (fragment == activeFragment) return;
        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(fragment).commit();
        activeFragment = fragment;
    }

    private void loadSubFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment)
                .hide(activeFragment)
                .addToBackStack(null)
                .commit();
        activeFragment = fragment;
    }

    public void syncUIAfterBack() {
        binding.getRoot().postDelayed(() -> {
            Fragment current = null;
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment f : fragments) {
                if (f != null && f.isVisible()) {
                    current = f;
                    break;
                }
            }
            if (current instanceof InicioFragment) {
                updateToolbar("EmpleoLocal", true, false, false, false, false, false);
                binding.bottomNavigation.getMenu().findItem(R.id.nav_home).setChecked(true);
                activeFragment = inicioFragment;
            } else if (current instanceof MapaFragment) {
                updateToolbar("Mapa de Empleos", true, false, false, false, false, false);
                binding.bottomNavigation.getMenu().findItem(R.id.nav_map).setChecked(true);
                activeFragment = mapaFragment;
            } else if (current instanceof PostulacionesFragment) {
                updateToolbar("Mis Postulaciones", true, false, false, false, false, false);
                binding.bottomNavigation.getMenu().findItem(R.id.nav_apply).setChecked(true);
                activeFragment = postulacionesFragment;
            } else if (current instanceof PerfilFragment) {
                updateToolbar("Mi Perfil", false, true, false, false, false, false);
                binding.bottomNavigation.getMenu().findItem(R.id.nav_profile).setChecked(true);
                activeFragment = perfilFragment;
            } else if (current instanceof DetalleOfertaFragment) {
                activeFragment = current;
                Oferta o = ((DetalleOfertaFragment) current).getOferta();
                if (o != null) {
                    updateToolbar("Detalle de Oferta", false, false, true, false, false, true);
                    ofertaViewModel.checkIsSaved(o.getId());
                }
            } else if (current instanceof NotificacionesFragment) {
                updateToolbar("Notificaciones", false, false, true, false, true, false);
                activeFragment = current;
            }
        }, 100);
    }

    public void openJobDetail(Oferta oferta) {
        updateToolbar("Detalle de Oferta", false, false, true, false, false, true);
        ofertaViewModel.checkIsSaved(oferta.getId());
        DetalleOfertaFragment fragment = new DetalleOfertaFragment();
        Bundle args = new Bundle();
        args.putSerializable("oferta", oferta);
        fragment.setArguments(args);
        loadSubFragment(fragment);
    }

    public void openCompanyProfile(Oferta oferta) {
        updateToolbar("Perfil de Empresa", false, false, true, false, false, false);
        PerfilEmpresaFragment fragment = new PerfilEmpresaFragment();
        Bundle args = new Bundle();
        args.putSerializable("oferta", oferta);
        fragment.setArguments(args);
        loadSubFragment(fragment);
    }

    public void openTracking(Postulacion postulacion) {
        updateToolbar("Seguimiento de Postulación", false, false, true, false, false, false);
        SeguimientoPostulacionFragment fragment = new SeguimientoPostulacionFragment();
        Bundle args = new Bundle();
        args.putSerializable("postulacion", postulacion);
        fragment.setArguments(args);
        loadSubFragment(fragment);
    }

    public void openFilters() {
        updateToolbar("Filtrar Ofertas", false, false, true, false, false, false);
        loadSubFragment(new FiltrarOfertasFragment());
    }

    private void updateToolbar(String title, boolean showNotif, boolean showEdit, boolean showBack, boolean showSave, boolean showMarkRead, boolean showBookmark) {
        binding.tvToolbarTitle.setText(title);
        binding.flNotifications.setVisibility(showNotif ? View.VISIBLE : View.GONE);
        binding.ivEditProfile.setVisibility(showEdit ? View.VISIBLE : View.GONE);
        binding.ivBackButton.setVisibility(showBack ? View.VISIBLE : View.GONE);
        binding.tvSaveButton.setVisibility(showSave ? View.VISIBLE : View.GONE);
        binding.tvMarkReadButton.setVisibility(showMarkRead ? View.GONE : View.GONE);
        binding.ivBookmark.setVisibility(showBookmark ? View.VISIBLE : View.GONE);
    }

    // --- FRAGMENTS ---

    public static class InicioFragment extends Fragment {
        private ActivityInicioBinding b;
        private OfertaViewModel ofertaViewModel;
        private AuthViewModel authViewModel;
        private PerfilViewModel userViewModel;
        private RealOfertaAdapter adapter;
        private List<Oferta> listaOriginal = new ArrayList<>();
        private LayoutLocationSelectionBinding lb;
        private double tempLat = 0, tempLng = 0;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            if (b != null) return b.getRoot();
            b = ActivityInicioBinding.inflate(i, c, false);
            ofertaViewModel = new ViewModelProvider(requireActivity()).get(OfertaViewModel.class);
            authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
            userViewModel = new ViewModelProvider(requireActivity()).get(PerfilViewModel.class);
            adapter = new RealOfertaAdapter((MainActivity) getActivity());
            b.tvChange.setOnClickListener(v -> showLocationDialog());
            b.rvInicioOfertas.setLayoutManager(new LinearLayoutManager(getContext()));
            b.rvInicioOfertas.setAdapter(adapter);
            setupObservers();
            setupSectors();
            setupSearch();
            ofertaViewModel.refreshOfertas();
            authViewModel.fetchSectors();
            userViewModel.loadUserProfile();
            return b.getRoot();
        }

        private void setupSearch() {
            b.etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ofertaViewModel.setSearchQuery(s.toString().trim());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        private void setupObservers() {
            ofertaViewModel.getOfertas().observe(getViewLifecycleOwner(), ofertas -> {
                if (ofertas != null) {
                    listaOriginal = ofertas;
                    applyFilters();
                }
            });

            authViewModel.getSectorsLiveData().observe(getViewLifecycleOwner(), sectores -> {
                if (sectores != null) {
                    updateSectorChips(sectores);
                }
            });

            userViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
                if (user != null && user.getDireccion() != null) {
                    String city = user.getDireccion().split(",")[0].trim();
                    b.tvLocationInfo.setText("Mostrando empleos en " + city);
                    b.tvLocationRadio.setText("Radio: " + user.getRadioBusqueda() + "km");
                    applyFilters();
                }
            });

            ofertaViewModel.getSelectedSector().observe(getViewLifecycleOwner(), sector -> {
                syncChipsWithViewModel(sector);
                applyFilters();
            });

            ofertaViewModel.getSearchQuery().observe(getViewLifecycleOwner(), query -> {
                if (!b.etSearch.getText().toString().equals(query)) {
                    b.etSearch.setText(query);
                }
                applyFilters();
            });

            ofertaViewModel.getApplicationSuccess().observe(getViewLifecycleOwner(), success -> {
                if (success != null && success) {
                    ViewUtils.showSnackbar(getActivity(), "¡Postulación exitosa!", ViewUtils.MsgType.SUCCESS);
                    ofertaViewModel.clearStatus();
                }
            });

            ofertaViewModel.getApplicationError().observe(getViewLifecycleOwner(), error -> {
                if (error != null) {
                    ViewUtils.showSnackbar(getActivity(), error, ViewUtils.MsgType.ERROR);
                    ofertaViewModel.clearStatus();
                }
            });
        }

        private void setupSectors() {
            b.cgInicioFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    ofertaViewModel.setSelectedSector("");
                } else {
                    Chip chip = group.findViewById(checkedIds.get(0));
                    if (chip != null) {
                        ofertaViewModel.setSelectedSector(chip.getText().toString());
                    }
                }
            });
        }

        private void syncChipsWithViewModel(String sector) {
            for (int i = 0; i < b.cgInicioFilters.getChildCount(); i++) {
                Chip chip = (Chip) b.cgInicioFilters.getChildAt(i);
                if (chip.getText().toString().equals(sector)) {
                    chip.setChecked(true);
                    return;
                }
            }
            b.cgInicioFilters.clearCheck();
        }

        private void updateSectorChips(List<String> sectores) {
            b.cgInicioFilters.removeAllViews();
            Chip chipCerca = new Chip(requireContext());
            chipCerca.setText("Cerca (2km)");
            chipCerca.setCheckable(true);
            chipCerca.setId(View.generateViewId());
            chipCerca.setChipBackgroundColorResource(R.color.selector_chip_background);
            chipCerca.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.selector_chip_text));
            b.cgInicioFilters.addView(chipCerca);

            for (String sector : sectores) {
                Chip chip = new Chip(requireContext());
                chip.setText(sector);
                chip.setCheckable(true);
                chip.setId(View.generateViewId());
                chip.setChipBackgroundColorResource(R.color.selector_chip_background);
                chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.selector_chip_text));
                chip.setCheckedIconVisible(true);
                chip.setCheckedIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_check));
                b.cgInicioFilters.addView(chip);
            }

            syncChipsWithViewModel(ofertaViewModel.getSelectedSector().getValue());
        }

        private void applyFilters() {
            Usuario user = userViewModel.getUserProfile().getValue();
            if (user == null || listaOriginal.isEmpty()) return;

            String sector = ofertaViewModel.getSelectedSector().getValue();
            String query = ofertaViewModel.getSearchQuery().getValue().toLowerCase();

            List<Oferta> filtrada = new ArrayList<>();
            float[] results = new float[1];
            double userLat = user.getLatitud();
            double userLng = user.getLongitud();
            double radiusMt = user.getRadioBusqueda() * 1000;

            for (Oferta o : listaOriginal) {
                android.location.Location.distanceBetween(userLat, userLng, o.getLatitud(), o.getLongitud(), results);
                boolean inRadius = results[0] <= radiusMt;
                boolean matchesSector = sector == null || sector.isEmpty() || sector.equalsIgnoreCase("Cerca (2km)") || (o.getSector() != null && o.getSector().equalsIgnoreCase(sector));
                boolean matchesQuery = query.isEmpty() || o.getTitulo().toLowerCase().contains(query) || o.getEmpresa().toLowerCase().contains(query);
                if (inRadius && matchesSector && matchesQuery) {
                    filtrada.add(o);
                }
            }
            adapter.setOfertas(filtrada);
        }

        private void showLocationDialog() {
            BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
            lb = LayoutLocationSelectionBinding.inflate(getLayoutInflater());
            dialog.setContentView(lb.getRoot());

            Usuario user = userViewModel.getUserProfile().getValue();
            if (user != null) {
                if (user.getDireccion() != null) {
                    String[] parts = user.getDireccion().split(",");
                    if (parts.length >= 3) {
                        lb.etDistrito.setText(parts[0].trim());
                        lb.etProvincia.setText(parts[1].trim());
                        lb.etRegion.setText(parts[2].trim());
                    } else {
                        lb.etDistrito.setText(user.getDireccion());
                    }
                }
                lb.sliderRadius.setValue(user.getRadioBusqueda());
                lb.tvRadiusValue.setText(user.getRadioBusqueda() + "km");
                tempLat = user.getLatitud();
                tempLng = user.getLongitud();
            }

            lb.btnUseLocation.setOnClickListener(v -> getCurrentLocation());
            lb.sliderRadius.addOnChangeListener((slider, value, fromUser) -> lb.tvRadiusValue.setText((int) value + "km"));

            lb.btnAplicarUbicacion.setOnClickListener(v -> {
                if (tempLat == 0 || tempLng == 0) {
                    ViewUtils.showSnackbar(getActivity(), "Debe obtener una ubicación válida en Perú", ViewUtils.MsgType.WARNING);
                    return;
                }
                String d = lb.etDistrito.getText().toString().trim();
                String p = lb.etProvincia.getText().toString().trim();
                String r = lb.etRegion.getText().toString().trim();
                if (d.isEmpty() || r.isEmpty()) {
                    ViewUtils.showSnackbar(getActivity(), "Complete los campos de distrito y región", ViewUtils.MsgType.WARNING);
                    return;
                }
                String nuevaDir = d + ", " + p + ", " + r;
                int nuevoRadio = (int) lb.sliderRadius.getValue();
                userViewModel.updateLocation(tempLat, tempLng, nuevaDir, nuevoRadio);
                ViewUtils.showSnackbar(getActivity(), "Perfil sincronizado correctamente", ViewUtils.MsgType.SUCCESS);
                dialog.dismiss();
            });

            dialog.show();
        }

        private void getCurrentLocation() {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }

            lb.btnUseLocation.setEnabled(false);
            lb.btnUseLocation.setText("Detectando...");
            ViewUtils.showSnackbar(getActivity(), "Conectando con GPS...", ViewUtils.MsgType.INFO);

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
            CurrentLocationRequest request = new CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
            fusedLocationClient.getCurrentLocation(request, null).addOnSuccessListener(requireActivity(), location -> {
                lb.btnUseLocation.setEnabled(true);
                lb.btnUseLocation.setText(R.string.use_current_location);
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    Geocoder geocoder = new Geocoder(requireContext(), new Locale("es", "PE"));
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address addr = addresses.get(0);
                            if (addr.getCountryCode() == null || !addr.getCountryCode().equalsIgnoreCase("PE")) {
                                ViewUtils.showSnackbar(getActivity(), "Solo disponible para ubicaciones en Perú", ViewUtils.MsgType.WARNING);
                                resetDialogFields();
                                return;
                            }
                            tempLat = lat;
                            tempLng = lng;
                            ViewUtils.showSnackbar(getActivity(), "Ubicación detectada: " + addr.getLocality(), ViewUtils.MsgType.SUCCESS);
                            String district = addr.getLocality() != null ? addr.getLocality() : (addr.getSubLocality() != null ? addr.getSubLocality() : "");
                            String province = addr.getSubAdminArea() != null ? addr.getSubAdminArea() : "";
                            String region = addr.getAdminArea() != null ? addr.getAdminArea() : "";
                            lb.etDistrito.setText(district);
                            lb.etProvincia.setText(province);
                            lb.etRegion.setText(region);
                        }
                    } catch (IOException e) {
                        ViewUtils.showSnackbar(getActivity(), "Error de red al obtener dirección", ViewUtils.MsgType.ERROR);
                    }
                }
            });
        }

        private void resetDialogFields() {
            lb.etDistrito.setText("");
            lb.etProvincia.setText("");
            lb.etRegion.setText("");
            tempLat = 0;
            tempLng = 0;
        }
    }

    public static class MapaFragment extends Fragment implements OnMapReadyCallback {
        private ActivityMapaEmpleosBinding b;
        private GoogleMap mMap;
        private PerfilViewModel userViewModel;
        private OfertaViewModel ofertaViewModel;
        private Circle mCircle;
        private RealOfertaAdapter listAdapter;
        private final List<Oferta> allOfertas = new ArrayList<>();
        private final List<Oferta> filteredNearbyOfertas = new ArrayList<>();
        private Handler searchHandler = new Handler(Looper.getMainLooper());
        private Runnable searchRunnable;
        private final LatLngBounds PERU_BOUNDS = new LatLngBounds(new LatLng(-18.35, -81.33), new LatLng(-0.03, -68.65));

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            if (b != null) return b.getRoot();
            b = ActivityMapaEmpleosBinding.inflate(i, c, false);
            userViewModel = new ViewModelProvider(requireActivity()).get(PerfilViewModel.class);
            ofertaViewModel = new ViewModelProvider(requireActivity()).get(OfertaViewModel.class);
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) mapFragment.getMapAsync(this);
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(b.bottomSheet);
            b.viewListBtnContainer.setOnClickListener(v -> behavior.setState(BottomSheetBehavior.STATE_EXPANDED));
            listAdapter = new RealOfertaAdapter((MainActivity) getActivity());
            b.listContainerRv.setLayoutManager(new LinearLayoutManager(getContext()));
            b.listContainerRv.setAdapter(listAdapter);
            setupFiltersObservers();
            b.ivCloseCard.setOnClickListener(v -> b.cardSelectedJob.setVisibility(View.GONE));
            b.btnMapApply.setOnClickListener(v -> {
                Oferta selected = (Oferta) b.cardSelectedJob.getTag();
                if (selected != null) ofertaViewModel.postularAOferta(selected);
            });
            b.etMapSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                    b.ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }
                @Override
                public void afterTextChanged(Editable s) {
                    String query = s.toString().trim();
                    if (query.length() > 2) {
                        searchRunnable = () -> searchLocation(query);
                        searchHandler.postDelayed(searchRunnable, 1000);
                    }
                }
            });
            b.ivClearSearch.setOnClickListener(v -> {
                b.etMapSearch.setText("");
                b.cardSelectedJob.setVisibility(View.GONE);
            });
            b.ivMapSearchIcon.setOnClickListener(v -> searchLocation(b.etMapSearch.getText().toString()));
            b.etMapSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchLocation(b.etMapSearch.getText().toString());
                    return true;
                }
                return false;
            });
            b.fabMyLocation.setOnClickListener(v -> centerMapOnRealLocation());
            b.fabFilter.setOnClickListener(v -> ((MainActivity) getActivity()).openFilters());
            return b.getRoot();
        }

        private void setupFiltersObservers() {
            ofertaViewModel.getOfertas().observe(getViewLifecycleOwner(), ofertas -> {
                if (ofertas != null) {
                    allOfertas.clear();
                    allOfertas.addAll(ofertas);
                    updateFilteredListAndMarkers();
                }
            });
            ofertaViewModel.getSelectedSector().observe(getViewLifecycleOwner(), s -> updateFilteredListAndMarkers());
            ofertaViewModel.getSearchQuery().observe(getViewLifecycleOwner(), q -> {
                if (!b.etMapSearch.getText().toString().equals(q)) b.etMapSearch.setText(q);
                updateFilteredListAndMarkers();
            });
        }

        private void updateFilteredListAndMarkers() {
            Usuario user = userViewModel.getUserProfile().getValue();
            if (user == null || allOfertas.isEmpty() || mMap == null) return;
            String sector = ofertaViewModel.getSelectedSector().getValue();
            String query = ofertaViewModel.getSearchQuery().getValue().toLowerCase();
            filteredNearbyOfertas.clear();
            float[] results = new float[1];
            double userLat = user.getLatitud();
            double userLng = user.getLongitud();
            double radiusMt = user.getRadioBusqueda() * 1000;
            for (Oferta o : allOfertas) {
                android.location.Location.distanceBetween(userLat, userLng, o.getLatitud(), o.getLongitud(), results);
                boolean inRadius = results[0] <= radiusMt;
                boolean matchesSector = sector == null || sector.isEmpty() || sector.equalsIgnoreCase("Cerca (2km)") || (o.getSector() != null && o.getSector().equalsIgnoreCase(sector));
                boolean matchesQuery = query.isEmpty() || o.getTitulo().toLowerCase().contains(query) || o.getEmpresa().toLowerCase().contains(query);
                if (inRadius && matchesSector && matchesQuery) filteredNearbyOfertas.add(o);
            }
            listAdapter.setOfertas(filteredNearbyOfertas);
            updateMapTexts(filteredNearbyOfertas);
            addMarkersToMap(filteredNearbyOfertas);
            drawSearchCircle(new LatLng(userLat, userLng), user.getRadioBusqueda());
        }

        private void searchLocation(String query) {
            if (query.isEmpty() || !isAdded()) return;
            Geocoder geocoder = new Geocoder(requireContext(), new Locale("es", "PE"));
            try {
                List<Address> addresses = geocoder.getFromLocationName(query + ", Peru", 5);
                if (addresses != null && !addresses.isEmpty()) {
                    Address bestMatch = null;
                    for (Address addr : addresses) {
                        if (addr.getCountryCode() != null && addr.getCountryCode().equalsIgnoreCase("PE")) {
                            bestMatch = addr;
                            break;
                        }
                    }
                    if (bestMatch != null) {
                        LatLng pos = new LatLng(bestMatch.getLatitude(), bestMatch.getLongitude());
                        if (PERU_BOUNDS.contains(pos)) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14));
                            String district = bestMatch.getLocality() != null ? bestMatch.getLocality() : (bestMatch.getSubLocality() != null ? bestMatch.getSubLocality() : query);
                            String fullDir = district + ", " + (bestMatch.getSubAdminArea() != null ? bestMatch.getSubAdminArea() : "") + ", " + (bestMatch.getAdminArea() != null ? bestMatch.getAdminArea() : "");
                            Usuario current = userViewModel.getUserProfile().getValue();
                            if (current != null) userViewModel.updateLocation(pos.latitude, pos.longitude, fullDir, current.getRadioBusqueda());
                        }
                    }
                }
            } catch (IOException ignored) {}
        }

        private void updateMapTexts(List<Oferta> localOfertas) {
            Usuario user = userViewModel.getUserProfile().getValue();
            if (user != null && user.getDireccion() != null) {
                String district = user.getDireccion().split(",")[0].trim();
                b.tvJobsFoundCount.setText(localOfertas.size() + " empleos en el radio de " + user.getRadioBusqueda() + "km");
                b.tvCurrentLocationTag.setText(district);
            }
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setLatLngBoundsForCameraTarget(PERU_BOUNDS);
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
            mMap.setOnMapClickListener(latLng -> b.cardSelectedJob.setVisibility(View.GONE));
            mMap.setOnMarkerClickListener(marker -> {
                Oferta o = (Oferta) marker.getTag();
                if (o != null) {
                    b.cardSelectedJob.setVisibility(View.VISIBLE);
                    b.cardSelectedJob.setTag(o);
                    b.tvJobTitleMap.setText(o.getTitulo());
                    b.tvJobLocationMap.setText(o.getEmpresa() + " · " + o.getDireccion());
                    Glide.with(requireContext()).load(o.getLogoUrl()).placeholder(R.drawable.ic_briefcase).into(b.ivMapLogoImg);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }
                return true;
            });
            setupUserObserver();
            userViewModel.loadUserProfile();
        }

        private void setupUserObserver() {
            userViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
                if (user != null && mMap != null) {
                    LatLng userLatLng = new LatLng(user.getLatitud(), user.getLongitud());
                    drawSearchCircle(userLatLng, user.getRadioBusqueda());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
                    updateFilteredListAndMarkers();
                }
            });
        }

        private void addMarkersToMap(List<Oferta> localOfertas) {
            if (mMap == null) return;
            mMap.clear();
            for (Oferta oferta : localOfertas) {
                LatLng pos = new LatLng(oferta.getLatitud(), oferta.getLongitud());
                Marker m = mMap.addMarker(new MarkerOptions().position(pos).title(oferta.getTitulo()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                if (m != null) m.setTag(oferta);
            }
        }

        private void drawSearchCircle(LatLng center, int radiusKm) {
            if (mCircle != null) mCircle.remove();
            mCircle = mMap.addCircle(new CircleOptions().center(center).radius(radiusKm * 1000).strokeWidth(2).strokeColor(Color.parseColor("#441F89E5")).fillColor(Color.parseColor("#221F89E5")));
        }

        private void centerMapOnRealLocation() {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                return;
            }
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
            CurrentLocationRequest request = new CurrentLocationRequest.Builder().setPriority(Priority.PRIORITY_HIGH_ACCURACY).build();
            fusedLocationClient.getCurrentLocation(request, null).addOnSuccessListener(requireActivity(), location -> {
                if (location != null && mMap != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
                }
            });
        }
    }

    public static class PostulacionesFragment extends Fragment {
        private ActivityPostulacionesBinding b;
        private PostulacionesViewModel viewModel;
        private PostulacionesAdapter adapter;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            if (b != null) return b.getRoot();
            b = ActivityPostulacionesBinding.inflate(i, c, false);
            viewModel = new ViewModelProvider(this).get(PostulacionesViewModel.class);
            adapter = new PostulacionesAdapter();
            b.rvPostulaciones.setLayoutManager(new LinearLayoutManager(getContext()));
            b.rvPostulaciones.setAdapter(adapter);
            adapter.setOnItemClickListener(p -> ((MainActivity) getActivity()).openTracking(p));
            setupObservers();
            return b.getRoot();
        }

        private void setupObservers() {
            viewModel.getPostulaciones().observe(getViewLifecycleOwner(), lista -> {
                if (lista != null) {
                    updateFilterChips(lista);
                    applyFilterAndTab(lista);
                }
            });
            b.tabsPostulaciones.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    applyFilterAndTab(viewModel.getPostulaciones().getValue());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        private void updateFilterChips(List<Postulacion> lista) {
            int enviadas = 0, revision = 0, entrevista = 0;
            for (Postulacion p : lista) {
                String e = normalize(p.getEstado());
                if (e.contains("postulado")) enviadas++;
                else if (e.contains("revision")) revision++;
                else if (e.contains("entrevista")) entrevista++;
            }
            b.chipSent.setText("Enviadas (" + enviadas + ")");
            b.chipReview.setText("En Revisión (" + revision + ")");
            b.chipInterview.setText("Entrevista (" + entrevista + ")");
        }

        private void applyFilterAndTab(List<Postulacion> lista) {
            if (lista == null) return;
            int tabPos = b.tabsPostulaciones.getSelectedTabPosition();
            List<Postulacion> filtrada = new ArrayList<>();
            for (Postulacion p : lista) {
                String e = normalize(p.getEstado());
                boolean isFinalizada = e.contains("final") || e.contains("rechaz");
                if (tabPos == 1 && isFinalizada) continue;
                if (tabPos == 2 && !isFinalizada) continue;
                filtrada.add(p);
            }
            adapter.setPostulaciones(filtrada);
            b.tvEmptyPostulaciones.setVisibility(filtrada.isEmpty() ? View.VISIBLE : View.GONE);
        }

        private String normalize(String input) {
            if (input == null) return "";
            return Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        }
    }

    public static class SeguimientoPostulacionFragment extends Fragment {
        private ActivitySeguimientoPostulacionBinding b;
        private Postulacion postulacion;
        private PostulacionesViewModel viewModel;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            b = ActivitySeguimientoPostulacionBinding.inflate(i, c, false);
            viewModel = new ViewModelProvider(requireActivity()).get(PostulacionesViewModel.class);
            if (getArguments() != null) postulacion = (Postulacion) getArguments().getSerializable("postulacion");
            if (postulacion != null) {
                b.setPostulacion(postulacion);
                Glide.with(this).load(postulacion.getLogoUrl()).placeholder(R.drawable.ic_briefcase).into(b.ivCompanyLogo);
                updateVerticalTimeline(postulacion.getEstado());
            }
            b.btnWithdraw.setOnClickListener(v -> {
                if (postulacion != null) viewModel.cancelarPostulacion(postulacion.getId());
            });
            setupObservers();
            return b.getRoot();
        }

        private void setupObservers() {
            viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
                if (success != null && success) {
                    ViewUtils.showSnackbar(getActivity(), "Postulación retirada correctamente", ViewUtils.MsgType.SUCCESS);
                    viewModel.resetUpdateStatus();
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        private void updateVerticalTimeline(String estado) {
            String e = normalize(estado);
            int blue = ContextCompat.getColor(requireContext(), R.color.login_blue_text);
            int gray = Color.parseColor("#E0E0E0");
            int darkGray = ContextCompat.getColor(requireContext(), R.color.login_subtitle);
            // Reset todos
            setStepState(b.dot1, b.line1, b.tvStatus1, b.tvDesc1, gray, darkGray);
            setStepState(b.dot2, b.line2, b.tvStatus2, b.tvDesc2, gray, darkGray);
            setStepState(b.dot3, b.line3, b.tvStatus3, b.tvDesc3, gray, darkGray);
            setStepState(b.dot4, null, b.tvStatus4, b.tvDesc4, gray, darkGray);
            if (e.contains("postulado")) {
                setStepState(b.dot1, b.line1, b.tvStatus1, b.tvDesc1, blue, blue);
                b.tvStatusMessage.setText("Tu CV ha sido recibido. El reclutador lo revisará pronto.");
            } else if (e.contains("revision")) {
                setStepState(b.dot1, b.line1, b.tvStatus1, b.tvDesc1, blue, blue);
                setStepState(b.dot2, b.line2, b.tvStatus2, b.tvDesc2, blue, blue);
                b.tvStatusMessage.setText("Tu perfil está siendo evaluado por el equipo de selección.");
            } else if (e.contains("entrevista")) {
                setStepState(b.dot1, b.line1, b.tvStatus1, b.tvDesc1, blue, blue);
                setStepState(b.dot2, b.line2, b.tvStatus2, b.tvDesc2, blue, blue);
                setStepState(b.dot3, b.line3, b.tvStatus3, b.tvDesc3, blue, blue);
                b.tvStatusMessage.setText("¡Felicidades! Has sido seleccionado para una entrevista presencial.");
            } else if (e.contains("final") || e.contains("rechaz")) {
                setStepState(b.dot1, b.line1, b.tvStatus1, b.tvDesc1, blue, blue);
                setStepState(b.dot2, b.line2, b.tvStatus2, b.tvDesc2, blue, blue);
                setStepState(b.dot3, b.line3, b.tvStatus3, b.tvDesc3, blue, blue);
                setStepState(b.dot4, null, b.tvStatus4, b.tvDesc4, blue, blue);
                if (e.contains("rechaz")) {
                    b.tvStatus4.setText("Postulación no seleccionada");
                    b.dot4.setColorFilter(Color.RED);
                    b.tvStatusMessage.setText("Gracias por participar. En esta ocasión no hemos podido avanzar con tu perfil.");
                } else {
                    b.tvStatusMessage.setText("El proceso ha finalizado. ¡Gracias por tu interés!");
                }
            }
        }

        private void setStepState(View dot, View line, View title, View desc, int dotColor, int textColor) {
            ((android.widget.ImageView)dot).setColorFilter(dotColor);
            if (line != null) line.setBackgroundColor(dotColor);
            ((android.widget.TextView)title).setTextColor(textColor);
            ((android.widget.TextView)desc).setTextColor(textColor);
        }

        private String normalize(String input) {
            if (input == null) return "";
            return Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        }
    }

    public static class PerfilFragment extends Fragment {
        private ActivityPerfilBinding b;
        private PerfilViewModel userViewModel;
        private PostulacionesViewModel postViewModel;
        private OfertaViewModel ofertaViewModel;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            if (b != null) return b.getRoot();
            b = ActivityPerfilBinding.inflate(i, c, false);
            userViewModel = new ViewModelProvider(requireActivity()).get(PerfilViewModel.class);
            postViewModel = new ViewModelProvider(requireActivity()).get(PostulacionesViewModel.class);
            ofertaViewModel = new ViewModelProvider(requireActivity()).get(OfertaViewModel.class);
            setupObservers();
            setupInitialLabels();
            setupNotificationSwitch();
            userViewModel.loadUserProfile();
            b.btnLogout.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), iniciar_sesion.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            });
            return b.getRoot();
        }

        private void setupNotificationSwitch() {
            b.swNotifications.setChecked(NotificationHelper.areNotificationsEnabled(requireContext()));
            b.swNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                NotificationHelper.setNotificationsEnabled(requireContext(), isChecked);
                if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                    }
                }
            });
        }

        private void setupInitialLabels() {
            b.statPostulaciones.tvStatLabel.setText("POSTULACIONES");
            b.statEntrevistas.tvStatLabel.setText("ENTREVISTAS");
            b.statGuardados.tvStatLabel.setText("GUARDADOS");
        }

        private void setupObservers() {
            userViewModel.getUserProfile().observe(getViewLifecycleOwner(), this::updateUI);
            postViewModel.getPostulaciones().observe(getViewLifecycleOwner(), lista -> {
                if (lista != null) {
                    b.statPostulaciones.tvStatCount.setText(String.valueOf(lista.size()));
                    long entrevistas = 0;
                    for (Postulacion p : lista) {
                        if (p.getEstado() != null && p.getEstado().toLowerCase().contains("entrevista")) entrevistas++;
                    }
                    b.statEntrevistas.tvStatCount.setText(String.valueOf(entrevistas));
                }
            });
            ofertaViewModel.getSavedIds().observe(getViewLifecycleOwner(), ids -> {
                if (ids != null) b.statGuardados.tvStatCount.setText(String.valueOf(ids.size()));
            });
        }

        private void updateUI(Usuario usuario) {
            if (usuario == null || b == null) return;
            b.tvUserName.setText(usuario.getNombre() + " " + usuario.getApellido());
            String initials = "";
            if (usuario.getNombre() != null && !usuario.getNombre().isEmpty()) initials += usuario.getNombre().charAt(0);
            if (usuario.getApellido() != null && !usuario.getApellido().isEmpty()) initials += usuario.getApellido().charAt(0);
            b.tvUserInitials.setText(initials.toUpperCase());
            b.tvUserLocation.setText(usuario.getDireccion());
            b.tvLocationAddress.setText(usuario.getDireccion());
            b.tvLocationRadio.setText(usuario.getRadioBusqueda() + " km");
            if (usuario.getCvUrl() != null && !usuario.getCvUrl().isEmpty()) {
                b.tvCvName.setText("Curriculum_Vitae.pdf");
                b.tvCvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.login_logo_text));
                b.tvViewCv.setVisibility(View.VISIBLE);
                b.tvViewCv.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(usuario.getCvUrl()));
                    startActivity(intent);
                });
            } else {
                b.tvCvName.setText("Aún no has subido un CV");
                b.tvCvName.setTextColor(Color.GRAY);
                b.tvViewCv.setVisibility(View.GONE);
            }
            b.cgSectores.removeAllViews();
            if (usuario.getSectores() != null) {
                for (String sector : usuario.getSectores()) {
                    Chip chip = new Chip(getContext());
                    chip.setText(sector);
                    chip.setChipBackgroundColorResource(android.R.color.transparent);
                    chip.setChipStrokeWidth(1f);
                    chip.setChipStrokeColorResource(R.color.login_blue_text);
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.login_blue_text));
                    b.cgSectores.addView(chip);
                }
            }
        }
    }

    public static class NotificacionesFragment extends Fragment {
        private ActivityNotificacionesBinding b;
        private NotificacionesViewModel viewModel;
        private NotificacionesAdapter adapter;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            b = ActivityNotificacionesBinding.inflate(i, c, false);
            viewModel = new ViewModelProvider(this).get(NotificacionesViewModel.class);
            adapter = new NotificacionesAdapter();
            b.rvNotificaciones.setLayoutManager(new LinearLayoutManager(getContext()));
            b.rvNotificaciones.setAdapter(adapter);
            adapter.setOnNotificacionClickListener(notif -> {
                viewModel.marcarComoLeida(notif.getId());
            });
            setupObservers();
            setupTabs();
            return b.getRoot();
        }

        private void setupObservers() {
            viewModel.getNotificaciones().observe(getViewLifecycleOwner(), list -> {
                filterNotifications();
            });
        }

        private void setupTabs() {
            b.tabsNotifications.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    filterNotifications();
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }

        private void filterNotifications() {
            List<Notificacion> all = viewModel.getNotificaciones().getValue();
            if (all == null) return;
            int tabPos = b.tabsNotifications.getSelectedTabPosition();
            List<Notificacion> filtered = new ArrayList<>();
            for (Notificacion n : all) {
                if (tabPos == 0) filtered.add(n);
                else if (tabPos == 1 && "PROXIMIDAD".equals(n.getTipo())) filtered.add(n);
                else if (tabPos == 2 && "POSTULACION".equals(n.getTipo())) filtered.add(n);
            }
            adapter.setNotificaciones(filtered);
            b.tvEmptyNotif.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }

        public void markAllRead() {
            viewModel.marcarTodasComoLeidas();
        }
    }

    public static class FiltrarOfertasFragment extends Fragment {
        private ActivityFiltrarOfertasBinding b;
        private PerfilViewModel viewModel;
        private AuthViewModel authViewModel;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            b = ActivityFiltrarOfertasBinding.inflate(i, c, false);
            viewModel = new ViewModelProvider(requireActivity()).get(PerfilViewModel.class);
            authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
            viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
                if (user != null && user.getDireccion() != null) {
                    String city = user.getDireccion().split(",")[0];
                    b.tvSearchRadiusTitle.setText("Radio de búsqueda en " + city);
                }
            });
            authViewModel.getSectorsLiveData().observe(getViewLifecycleOwner(), sectors -> {
                b.cgSectors.removeAllViews();
                for (String sector : sectors) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(sector);
                    chip.setCheckable(true);
                    chip.setChipBackgroundColorResource(R.color.selector_chip_background);
                    chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.selector_chip_text));
                    b.cgSectors.addView(chip);
                }
            });
            authViewModel.fetchSectors();
            b.sliderRadius.addOnChangeListener((slider, value, fromUser) -> b.tvRadiusVal.setText((int)value + " km"));
            b.btnApply.setOnClickListener(v -> getActivity().getOnBackPressedDispatcher().onBackPressed());
            return b.getRoot();
        }
    }

    public static class EditarPerfilFragment extends Fragment {
        private ActivityEditarPerfilBinding b;
        private PerfilViewModel viewModel;
        private AuthViewModel authViewModel;
        private Uri selectedPdfUri;
        private static final int PICK_PDF_REQUEST = 101;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            b = ActivityEditarPerfilBinding.inflate(i, c, false);
            viewModel = new ViewModelProvider(requireActivity()).get(PerfilViewModel.class);
            authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
            setupObservers();
            viewModel.loadUserProfile();
            authViewModel.fetchSectors();
            b.btnSaveChanges.setOnClickListener(v -> saveChanges());
            b.btnUploadCv.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(Intent.createChooser(intent, "Selecciona tu CV (PDF)"), PICK_PDF_REQUEST);
            });
            return b.getRoot();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
                selectedPdfUri = data.getData();
                b.tvCvName.setText("Nuevo archivo listo para subir");
                b.tvCvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.login_blue_text));
            }
        }

        private void setupObservers() {
            viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    b.etNombre.setText(user.getNombre());
                    b.etApellido.setText(user.getApellido());
                    b.etTelefono.setText(user.getTelefono());
                    b.etCorreo.setText(user.getCorreo());
                    String initials = "";
                    if (user.getNombre() != null && !user.getNombre().isEmpty()) initials += user.getNombre().charAt(0);
                    if (user.getApellido() != null && !user.getApellido().isEmpty()) initials += user.getApellido().charAt(0);
                    b.tvUserInitials.setText(initials.toUpperCase());
                    if (user.getCvUrl() == null || user.getCvUrl().isEmpty()) {
                        b.tvCvName.setText("No se ha subido ningún CV");
                    } else {
                        b.tvCvName.setText("CV Cargado (Presiona para actualizar)");
                        b.tvCvName.setTextColor(ContextCompat.getColor(requireContext(), R.color.login_blue_text));
                    }
                    if (authViewModel.getSectorsLiveData().getValue() != null) {
                        markUserSectors(authViewModel.getSectorsLiveData().getValue(), user.getSectores());
                    }
                }
            });
            authViewModel.getSectorsLiveData().observe(getViewLifecycleOwner(), sectors -> {
                List<String> userSectors = viewModel.getUserProfile().getValue() != null ? viewModel.getUserProfile().getValue().getSectores() : new ArrayList<>();
                markUserSectors(sectors, userSectors);
            });
            viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
                if (success != null && success) {
                    ViewUtils.showSnackbar(getActivity(), "Perfil actualizado con éxito", ViewUtils.MsgType.SUCCESS);
                    viewModel.resetUpdateStatus();
                    getActivity().getSupportFragmentManager().popBackStack();
                    ((MainActivity)getActivity()).syncUIAfterBack();
                }
            });
            viewModel.getError().observe(getViewLifecycleOwner(), error -> {
                if (error != null) ViewUtils.showSnackbar(getActivity(), error, ViewUtils.MsgType.ERROR);
            });
        }

        private void markUserSectors(List<String> allSectors, List<String> selectedSectors) {
            b.cgSectores.removeAllViews();
            for (String sector : allSectors) {
                Chip chip = new Chip(requireContext());
                chip.setText(sector);
                chip.setCheckable(true);
                chip.setChipBackgroundColorResource(R.color.selector_chip_background);
                chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.selector_chip_text));
                if (selectedSectors != null && selectedSectors.contains(sector)) {
                    chip.setChecked(true);
                }
                b.cgSectores.addView(chip);
            }
        }

        private void saveChanges() {
            String nombre = b.etNombre.getText().toString().trim();
            String apellido = b.etApellido.getText().toString().trim();
            String telefono = b.etTelefono.getText().toString().trim();
            if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty()) {
                ViewUtils.showSnackbar(getActivity(), "Complete todos los campos obligatorios", ViewUtils.MsgType.WARNING);
                return;
            }
            if (telefono.length() != 9 || !telefono.startsWith("9")) {
                ViewUtils.showSnackbar(getActivity(), "Teléfono inválido", ViewUtils.MsgType.WARNING);
                return;
            }
            List<String> selectedSectores = new ArrayList<>();
            for (int i = 0; i < b.cgSectores.getChildCount(); i++) {
                Chip chip = (Chip) b.cgSectores.getChildAt(i);
                if (chip.isChecked()) selectedSectores.add(chip.getText().toString());
            }
            String oldCvUrl = "";
            if (viewModel.getUserProfile().getValue() != null) {
                oldCvUrl = viewModel.getUserProfile().getValue().getCvUrl();
            }
            viewModel.updateProfileWithCv(nombre, apellido, telefono, selectedSectores, selectedPdfUri, oldCvUrl);
            ViewUtils.showSnackbar(getActivity(), "Procesando cambios...", ViewUtils.MsgType.INFO);
        }
    }

    public static class DetalleOfertaFragment extends Fragment implements OnMapReadyCallback {
        private ActivityDetalleOfertaBinding b;
        private Oferta oferta;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            b = ActivityDetalleOfertaBinding.inflate(i, c, false);
            if (getArguments() != null) oferta = (Oferta) getArguments().getSerializable("oferta");
            if (oferta != null) {
                b.tvDetalleTitulo.setText(oferta.getTitulo());
                b.tvDetalleEmpresa.setText(oferta.getEmpresa());
                b.tvDetalleSalario.setText("S/. " + String.format(Locale.getDefault(), "%,.0f", oferta.getSalario()) + "/mes");
                b.tvDetalleDireccion.setText(oferta.getDireccion());
                b.tvDetalleTipo.setText(oferta.getTipoContrato());
                b.tvDetalleModalidad.setText(oferta.getModalidad());
                b.tvDetalleDescripcion.setText(oferta.getDescripcion());
                b.tvDetalleResponsabilidades.setText(oferta.getResponsabilidades());
                b.tvDetalleBeneficios.setText(oferta.getBeneficios());
                Glide.with(requireContext()).load(oferta.getLogoUrl()).placeholder(R.drawable.ic_briefcase).into(b.ivDetalleLogo);
                SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_detalle);
                if (mapFragment != null) mapFragment.getMapAsync(this);
            }
            b.btnAplicarDetalle.setOnClickListener(v -> {
                OfertaViewModel vm = new ViewModelProvider(requireActivity()).get(OfertaViewModel.class);
                vm.postularAOferta(oferta);
            });
            b.cvCompanyHeader.setOnClickListener(v -> ((MainActivity) getActivity()).openCompanyProfile(oferta));
            return b.getRoot();
        }

        public Oferta getOferta() {
            return oferta;
        }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            if (oferta != null) {
                LatLng jobPos = new LatLng(oferta.getLatitud(), oferta.getLongitud());
                googleMap.addMarker(new MarkerOptions().position(jobPos).title(oferta.getEmpresa()));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jobPos, 15));
                googleMap.getUiSettings().setAllGesturesEnabled(false);
            }
        }
    }

    public static class PerfilEmpresaFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
            ActivityPerfilEmpresaBinding b = ActivityPerfilEmpresaBinding.inflate(i, c, false);
            Oferta o = (getArguments() != null) ? (Oferta) getArguments().getSerializable("oferta") : null;
            if (o != null) {
                b.tvEmpresaNombre.setText(o.getEmpresa());
                b.tvEmpresaDireccion.setText(o.getDireccion());
                b.llEmpresaVerificado.setVisibility(o.isVerificado() ? View.VISIBLE : View.GONE);

                // Configurar etiquetas
                b.cardRuc.tvInfoLabel.setText("RUC");
                b.cardSector.tvInfoLabel.setText("SECTOR");
                b.cardSize.tvInfoLabel.setText("TAMAÑO");
                b.cardHq.tvInfoLabel.setText("SEDE PRINCIPAL");

                // Configurar valores
                b.cardRuc.tvInfoValue.setText(o.getRuc() != null ? o.getRuc() : "No disponible");
                b.cardSector.tvInfoValue.setText(o.getSector() != null ? o.getSector() : "No disponible");
                b.cardSize.tvInfoValue.setText(o.getTamanoEmpresa() != null ? o.getTamanoEmpresa() : "No disponible");
                b.cardHq.tvInfoValue.setText(o.getSedePrincipal() != null ? o.getSedePrincipal() : "No disponible");

                Glide.with(requireContext()).load(o.getLogoUrl()).placeholder(R.drawable.ic_briefcase).into(b.ivEmpresaLogo);

                // Configurar Ofertas Activas de la empresa
                RealOfertaAdapter adapter = new RealOfertaAdapter((MainActivity) getActivity());
                b.rvEmpresaOfertas.setLayoutManager(new LinearLayoutManager(getContext()));
                b.rvEmpresaOfertas.setAdapter(adapter);

                OfertaViewModel ofertaViewModel = new ViewModelProvider(requireActivity()).get(OfertaViewModel.class);
                ofertaViewModel.getOfertas().observe(getViewLifecycleOwner(), todas -> {
                    if (todas != null) {
                        List<Oferta> deEmpresa = new ArrayList<>();
                        for (Oferta item : todas) {
                            // Robust filter: use RUC if available, otherwise fallback to name comparison
                            boolean sameRuc = o.getRuc() != null && o.getRuc().equals(item.getRuc());
                            boolean sameName = item.getEmpresa() != null && item.getEmpresa().equalsIgnoreCase(o.getEmpresa());
                            
                            if (sameRuc || sameName) {
                                deEmpresa.add(item);
                            }
                        }
                        adapter.setOfertas(deEmpresa);
                    }
                });
            }
            return b.getRoot();
        }
    }

    public static class RealOfertaAdapter extends RecyclerView.Adapter<RealOfertaAdapter.ViewHolder> {
        private final MainActivity activity;
        private List<Oferta> ofertas = new ArrayList<>();

        public RealOfertaAdapter(MainActivity activity) {
            this.activity = activity;
        }

        public void setOfertas(List<Oferta> ofertas) {
            this.ofertas = ofertas;
            notifyDataSetChanged();
        }

        public List<Oferta> getOfertas() {
            return ofertas;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new ViewHolder(ItemOfertaBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            Oferta o = ofertas.get(pos);
            h.b.tvJobTitle.setText(o.getTitulo());
            h.b.tvCompanyName.setText(o.getEmpresa());
            if (activity != null) {
                h.itemView.setOnClickListener(v -> activity.openJobDetail(o));
                new ViewModelProvider(activity).get(OfertaViewModel.class).getSavedIds().observe(activity, ids -> {
                    boolean isSaved = ids != null && ids.contains(o.getId());
                    h.b.ivSaveItem.setImageResource(isSaved ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline);
                    h.b.ivSaveItem.setColorFilter(isSaved ? Color.BLACK : Color.GRAY);
                });
                h.b.ivSaveItem.setOnClickListener(v -> new ViewModelProvider(activity).get(OfertaViewModel.class).toggleGuardar(o.getId()));
            }
            h.b.btnAplicar.setOnClickListener(v -> {
                if (activity != null) {
                    new ViewModelProvider(activity).get(OfertaViewModel.class).postularAOferta(o);
                }
            });
            Glide.with(h.itemView.getContext()).load(o.getLogoUrl()).placeholder(R.drawable.ic_briefcase).into(h.b.ivCompanyLogo);
        }

        @Override
        public int getItemCount() {
            return ofertas.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final ItemOfertaBinding b;

            public ViewHolder(ItemOfertaBinding binding) {
                super(binding.getRoot());
                this.b = binding;
            }
        }
    }
}