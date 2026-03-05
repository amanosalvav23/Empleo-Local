package unc.edu.pe.empleolocal.data.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import unc.edu.pe.empleolocal.data.model.Usuario;

@Dao
public interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Usuario usuario);

    @Update
    void actualizar(Usuario usuario);

    @Delete
    void eliminar(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE uid = :uid LIMIT 1")
    LiveData<Usuario> obtenerPorId(String uid);

    @Query("SELECT * FROM usuarios")
    LiveData<List<Usuario>> obtenerTodos();

    @Query("DELETE FROM usuarios")
    void eliminarTodo();
}
