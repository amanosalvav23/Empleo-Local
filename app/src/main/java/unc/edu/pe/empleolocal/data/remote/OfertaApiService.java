package unc.edu.pe.empleolocal.data.remote;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import unc.edu.pe.empleolocal.data.model.Oferta;

public interface OfertaApiService {
    // Usamos ?meta=false para que JSONBin nos devuelva directamente la lista [ ... ]
    @GET("b/69a7b12eae596e708f5d3090/latest?meta=false")
    Call<List<Oferta>> getOfertas(@Header("X-Master-Key") String masterKey);
}
