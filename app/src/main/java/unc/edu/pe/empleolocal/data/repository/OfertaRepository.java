package unc.edu.pe.empleolocal.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import unc.edu.pe.empleolocal.data.model.AppDatabase;
import unc.edu.pe.empleolocal.data.database.dao.OfertaDao;
import unc.edu.pe.empleolocal.data.model.Oferta;
import unc.edu.pe.empleolocal.data.remote.OfertaApiService;

public class OfertaRepository {
    private final OfertaDao ofertaDao;
    private final OfertaApiService apiService;
    private final String MASTER_KEY = "$2a$10$30BRnpnuKgPKWVP/RtHnLek.Vpkf1fFm2VfQ0PFLoO.ulOSWlr392";

    public OfertaRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        ofertaDao = db.ofertaDao();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.jsonbin.io/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(OfertaApiService.class);
    }

    public LiveData<List<Oferta>> getOfertasLocales() {
        return ofertaDao.obtenerTodas();
    }

    public void refreshOfertas() {
        Log.d("API_OFERTAS", "Iniciando carga desde JSONBin...");
        apiService.getOfertas(MASTER_KEY).enqueue(new Callback<List<Oferta>>() {
            @Override
            public void onResponse(Call<List<Oferta>> call, Response<List<Oferta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Oferta> lista = response.body();
                    Log.d("API_OFERTAS", "Éxito: " + lista.size() + " ofertas recibidas.");
                    new Thread(() -> {
                        try {
                            ofertaDao.eliminarTodo();
                            ofertaDao.insertarOfertas(lista);
                            Log.d("API_OFERTAS", "Datos guardados en BD local.");
                        } catch (Exception e) {
                            Log.e("API_OFERTAS", "Error al guardar en BD: " + e.getMessage());
                        }
                    }).start();
                } else {
                    Log.e("API_OFERTAS", "Error en respuesta: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Oferta>> call, Throwable t) {
                Log.e("API_OFERTAS", "Falla de red: " + t.getMessage());
            }
        });
    }
}
