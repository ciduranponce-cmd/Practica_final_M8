package xyz.cduran.fotosgps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import android.widget.Toast
import xyz.cduran.fotosgps.databinding.ActivityCameraBinding
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null

    private val solicitarPermiso =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedido ->
            if (concedido) {
                iniciarCamara()
            } else {
                binding.tvEstadoCamara.text =
                    "Debes permitir el acceso a la cámara."
                binding.btnCapturar.isEnabled = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCapturar.isEnabled = false

        binding.btnCapturar.setOnClickListener {
            tomarFotografia()
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarCamara()
        } else {
            solicitarPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    private fun iniciarCamara() {
        val proveedor = ProcessCameraProvider.getInstance(this)

        proveedor.addListener({
            try {
                val cameraProvider = proveedor.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider =
                            binding.previewCamara.surfaceProvider
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(
                        ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                    )
                    .build()

                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    selector,
                    preview,
                    imageCapture
                )

                binding.btnCapturar.isEnabled = true
                binding.tvEstadoCamara.text =
                    "Cámara lista. Puedes tomar tu foto."

            } catch (e: Exception) {
                binding.tvEstadoCamara.text =
                    "No se pudo iniciar la cámara."
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun tomarFotografia() {
        val captura = imageCapture ?: return

        binding.btnCapturar.isEnabled = false
        binding.tvEstadoCamara.text = "Guardando fotografía..."

        val carpeta = File(filesDir, "fotos")

        if (!carpeta.exists() && !carpeta.mkdirs()) {
            binding.btnCapturar.isEnabled = true
            binding.tvEstadoCamara.text =
                "No se pudo crear la carpeta de fotografías."
            return
        }

        val archivo = File(
            carpeta,
            "foto_${System.currentTimeMillis()}.jpg"
        )

        val opciones = OutputFileOptions.Builder(archivo).build()

        captura.takePicture(
            opciones,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults: ImageCapture.OutputFileResults
                ) {
                    val resultado = Intent().apply {
                        putExtra("RUTA_IMAGEN", archivo.absolutePath)
                    }

                    setResult(RESULT_OK, resultado)

                    Toast.makeText(
                        this@CameraActivity,
                        "Fotografía guardada correctamente.",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    binding.btnCapturar.isEnabled = true
                    binding.tvEstadoCamara.text =
                        "Error al tomar la fotografía. Inténtalo de nuevo."
                }
            }
        )
    }
}
