package unc.edu.pe.empleolocal.data.model;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import unc.edu.pe.empleolocal.data.database.converter.Converters;
import unc.edu.pe.empleolocal.data.database.dao.UsuarioDao;
import unc.edu.pe.empleolocal.data.database.dao.OfertaDao;
import unc.edu.pe.empleolocal.data.database.dao.PostulacionDao;

@Database(entities = {Usuario.class, Oferta.class, Postulacion.class}, version = 4, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract UsuarioDao usuarioDao();
    public abstract OfertaDao ofertaDao();
    public abstract PostulacionDao postulacionDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "empleo_local_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
