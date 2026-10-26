
package xyz.cduran.fotosgps.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fotos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"])]
)
data class FotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val usuarioId: Int,
    val rutaImagen: String,
    val comentario: String,
    val latitud: Double,
    val longitud: Double,
    val fecha: Long = System.currentTimeMillis()
)
