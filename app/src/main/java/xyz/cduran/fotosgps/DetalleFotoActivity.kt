package xyz.cduran.fotosgps

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.cduran.fotosgps.data.AppDatabase
import xyz.cduran.fotosgps.databinding.ActivityDetalleFotoBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.AlertDialog
import android.widget.Toast


class DetalleFotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleFotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fotoId = intent.getIntExtra("FOTO_ID", -1)
        val usuarioId = intent.getIntExtra("USUARIO_ID", -1)

        binding.btnVolverDetalle.setOnClickListener {

            binding.btnEliminarFoto.setOnClickListener {
                confirmarEliminacion(fotoId, usuarioId)
            }
            finish()
        }

        if (fotoId <= 0 || usuarioId <= 0) {
            finish()
            return
        }

        lifecycleScope.launch {
            val foto = withContext(Dispatchers.IO) {
                AppDatabase.obtenerInstancia(applicationContext)
                    .fotoDao()
                    .buscarPorId(fotoId, usuarioId)
            }

            if (foto == null) {
                finish()
                return@launch
            }

            val archivo = File(foto.rutaImagen)

            if (archivo.exists()) {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(archivo.absolutePath)
                }

                if (bitmap != null) {
                    binding.ivFotoDetalle.setImageBitmap(bitmap)
                }
            }

            binding.tvComentarioDetalle.text = foto.comentario

            val formatoFecha = SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
            )
            binding.tvFechaDetalle.text =
                "Fecha: ${formatoFecha.format(Date(foto.fecha))}"

            binding.tvCoordenadasDetalle.text =
                "Latitud: ${foto.latitud}\nLongitud: ${foto.longitud}"
        }
    }

    private fun confirmarEliminacion(fotoId: Int, usuarioId: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar fotografía")
            .setMessage(
                "¿Estás seguro de que deseas eliminar esta fotografía? " +
                        "Esta acción no se puede deshacer."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->

                binding.btnEliminarFoto.isEnabled = false

                lifecycleScope.launch {
                    try {
                        val eliminada = withContext(Dispatchers.IO) {
                            val dao = AppDatabase
                                .obtenerInstancia(applicationContext)
                                .fotoDao()

                            // Consultar primero la foto del usuario actual.
                            val foto = dao.buscarPorId(fotoId, usuarioId)

                            if (foto == null) {
                                false
                            } else {
                                // Borrar primero el registro de Room.
                                val filas = dao.eliminar(fotoId, usuarioId)

                                if (filas > 0) {
                                    // Borrar el archivo solamente si se eliminó
                                    // correctamente el registro.
                                    File(foto.rutaImagen).delete()
                                    true
                                } else {
                                    false
                                }
                            }
                        }

                        if (eliminada) {
                            Toast.makeText(
                                this@DetalleFotoActivity,
                                "Fotografía eliminada",
                                Toast.LENGTH_SHORT
                            ).show()

                            setResult(RESULT_OK)
                            finish()
                        } else {
                            binding.btnEliminarFoto.isEnabled = true
                            Toast.makeText(
                                this@DetalleFotoActivity,
                                "No se encontró la fotografía",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        binding.btnEliminarFoto.isEnabled = true
                        Toast.makeText(
                            this@DetalleFotoActivity,
                            "No se pudo eliminar la fotografía",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .show()
    }



}
