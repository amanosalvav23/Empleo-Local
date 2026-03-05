package unc.edu.pe.empleolocal.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import unc.edu.pe.empleolocal.data.model.Oferta;

@Dao
public interface OfertaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarOfertas(List<Oferta> ofertas);

    @Query("SELECT * FROM ofertas")
    LiveData<List<Oferta>> obtenerTodas();

    @Query("DELETE FROM ofertas")
    void eliminarTodo();
}
