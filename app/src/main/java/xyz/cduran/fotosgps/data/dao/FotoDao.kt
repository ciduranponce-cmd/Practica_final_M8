
package xyz.cduran.fotosgps.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xyz.cduran.fotosgps.data.entity.FotoEntity

@Dao
interface FotoDao {

    @Insert
    suspend fun insertar(foto: FotoEntity): Long

    @Query("""
        SELECT * FROM fotos
        WHERE usuarioId = :usuarioId
        ORDER BY fecha DESC
    """)
    suspend fun listarPorUsuario(usuarioId: Int): List<FotoEntity>

    @Query("""
        SELECT * FROM fotos
        WHERE id = :fotoId AND usuarioId = :usuarioId
        LIMIT 1
    """)
    suspend fun buscarPorId(
        fotoId: Int,
        usuarioId: Int
    ): FotoEntity?

    @Query("DELETE FROM fotos WHERE id = :fotoId AND usuarioId = :usuarioId")
    suspend fun eliminar(fotoId: Int, usuarioId: Int): Int
}
