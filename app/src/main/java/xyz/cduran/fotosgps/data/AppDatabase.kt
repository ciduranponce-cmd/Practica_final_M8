
package xyz.cduran.fotosgps.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.cduran.fotosgps.data.dao.FotoDao
import xyz.cduran.fotosgps.data.dao.UsuarioDao
import xyz.cduran.fotosgps.data.entity.FotoEntity
import xyz.cduran.fotosgps.data.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        FotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun fotoDao(): FotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fotos_gps_database"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
