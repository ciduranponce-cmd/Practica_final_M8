package xyz.cduran.fotosgps

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.cduran.fotosgps.data.AppDatabase
import xyz.cduran.fotosgps.data.entity.FotoEntity
import xyz.cduran.fotosgps.databinding.ActivityGuardarFotoBinding
import java.io.File

class GuardarFotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardarFotoBinding

    private var usuarioId: Int = -1
    private var rutaImagen: String = ""

    private var latitud: Double? = null
    private var longitud: Double? = null

    private val clienteUbicacion by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val solicitarPermisosUbicacion =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->
            val concedido =
                permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (concedido) {
                obtenerUbicacion()
            } else {
                binding.tvCoordenadas.text =
                    "Permiso de ubicación denegado."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGuardarFotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioId = intent.getIntExtra("USUARIO_ID", -1)
        rutaImagen = intent.getStringExtra("RUTA_IMAGEN") ?: ""

        if (usuarioId <= 0 || rutaImagen.isBlank() ||
            !File(rutaImagen).exists()
        ) {
            binding.tvCoordenadas.text =
                "No se recibió una fotografía válida."
            binding.btnGuardarFoto.isEnabled = false
            return
        }

        val bitmap = BitmapFactory.decodeFile(rutaImagen)
        if (bitmap != null) {
            binding.ivFotoCapturada.setImageBitmap(bitmap)
        } else {
            binding.tvCoordenadas.text =
                "No se pudo mostrar la fotografía."
            binding.btnGuardarFoto.isEnabled = false
            return
        }

        binding.btnObtenerUbicacion.setOnClickListener {
            comprobarPermisosUbicacion()
        }

        binding.btnGuardarFoto.setOnClickListener {
            guardarFotografia()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }

        comprobarPermisosUbicacion()
    }

    private fun comprobarPermisosUbicacion() {
        val permisoFino = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permisoFino || permisoAproximado) {
            obtenerUbicacion()
        } else {
            solicitarPermisosUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @Suppress("MissingPermission")
    private fun obtenerUbicacion() {
        val permisoFino = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val permisoAproximado = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permisoFino && !permisoAproximado) {
            return
        }

        binding.tvCoordenadas.text = "Obteniendo ubicación..."

        val token = CancellationTokenSource()

        clienteUbicacion.getCurrentLocation(
            if (permisoFino) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            },
            token.token
        ).addOnSuccessListener { ubicacion ->
            if (ubicacion != null) {
                latitud = ubicacion.latitude
                longitud = ubicacion.longitude

                binding.tvCoordenadas.text =
                    "Latitud: $latitud\nLongitud: $longitud"
            } else {
                latitud = null
                longitud = null

                binding.tvCoordenadas.text =
                    "No se obtuvo ubicación. Activa la ubicación e inténtalo otra vez."
            }
        }.addOnFailureListener {
            latitud = null
            longitud = null

            binding.tvCoordenadas.text =
                "Error al obtener la ubicación. Inténtalo de nuevo."
        }
    }

    private fun guardarFotografia() {
        val comentario = binding.etComentario.text.toString().trim()
        val lat = latitud
        val lon = longitud

        if (comentario.isBlank()) {
            binding.etComentario.error =
                "Escribe un comentario."
            return
        }

        if (lat == null || lon == null) {
            binding.tvCoordenadas.text =
                "Necesitas obtener la ubicación antes de guardar."
            return
        }

        binding.btnGuardarFoto.isEnabled = false

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val foto = FotoEntity(
                        usuarioId = usuarioId,
                        rutaImagen = rutaImagen,
                        comentario = comentario,
                        latitud = lat,
                        longitud = lon
                    )

                    AppDatabase
                        .obtenerInstancia(applicationContext)
                        .fotoDao()
                        .insertar(foto)
                }

                setResult(RESULT_OK)
                finish()

            } catch (e: Exception) {
                binding.tvCoordenadas.text =
                    "No se pudo guardar la fotografía en la base de datos."
                binding.btnGuardarFoto.isEnabled = true
            }
        }
    }
}
