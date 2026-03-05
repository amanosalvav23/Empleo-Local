package unc.edu.pe.empleolocal.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import unc.edu.pe.empleolocal.data.model.Postulacion;

@Dao
public interface PostulacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Postulacion postulacion);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertarTodas(List<Postulacion> postulaciones);

    @Update
    void actualizar(Postulacion postulacion);

    @Query("SELECT * FROM postulaciones WHERE usuarioId = :uid ORDER BY fechaPostulacion DESC")
    LiveData<List<Postulacion>> obtenerPorUsuario(String uid);

    @Query("DELETE FROM postulaciones")
    void eliminarTodo();
}
