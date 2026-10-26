
package xyz.cduran.fotosgps

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import xyz.cduran.fotosgps.databinding.ActivityMainBinding
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var usuarioId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar los datos enviados desde LoginActivity
        usuarioId = intent.getIntExtra("USUARIO_ID", -1)

        val nombre = intent.getStringExtra("USUARIO_NOMBRE")
            ?: "Usuario"

        // Evitar mostrar esta pantalla sin un usuario autenticado
        if (usuarioId <= 0) {
            regresarAlLogin()
            return
        }

        binding.tvNombreUsuario.text = "Hola, $nombre 👋"


        binding.btnTomarFoto.setOnClickListener {
            camaraLauncher.launch(
                Intent(this, CameraActivity::class.java)
            )
        }

        binding.btnGaleria.setOnClickListener {
            val intent = Intent(this, GaleriaActivity::class.java).apply {
                putExtra("USUARIO_ID", usuarioId)
            }
            startActivity(intent)
        }

        binding.btnCerrarSesion.setOnClickListener {
            regresarAlLogin()
        }
    }

    private fun regresarAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }


    private val camaraLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado ->

            if (resultado.resultCode == RESULT_OK) {
                val ruta = resultado.data
                    ?.getStringExtra("RUTA_IMAGEN")

                if (!ruta.isNullOrBlank()) {
                    val intent = Intent(
                        this,
                        GuardarFotoActivity::class.java
                    ).apply {
                        putExtra("RUTA_IMAGEN", ruta)
                        putExtra("USUARIO_ID", usuarioId)
                    }

                    startActivity(intent)
                }
            }
        }



}
