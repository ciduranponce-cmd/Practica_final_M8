
package xyz.cduran.fotosgps

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.cduran.fotosgps.data.AppDatabase
import xyz.cduran.fotosgps.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIniciarSesion.setOnClickListener {
            iniciarSesion()
        }

        binding.tvIrRegistro.setOnClickListener {
            startActivity(
                Intent(this, RegistroActivity::class.java)
            )
        }
    }

    private fun iniciarSesion() {
        val correo = binding.etCorreo.text.toString()
            .trim()
            .lowercase()

        val contrasena = binding.etContrasena.text.toString()

        if (correo.isBlank() || contrasena.isBlank()) {
            mostrarMensaje("Ingresa tu correo y contraseña.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarMensaje("El correo electrónico no es válido.")
            return
        }

        binding.btnIniciarSesion.isEnabled = false
        mostrarMensaje("Verificando acceso...")

        lifecycleScope.launch {
            try {
                val usuario = withContext(Dispatchers.IO) {
                    val dao = AppDatabase
                        .obtenerInstancia(applicationContext)
                        .usuarioDao()

                    dao.buscarPorCorreo(correo)
                }

                val accesoValido = usuario != null &&
                        PasswordHasher.verificar(
                            contrasena,
                            usuario.contrasenaHash
                        )

                if (accesoValido && usuario != null) {
                    val intent = Intent(
                        this@LoginActivity,
                        MainActivity::class.java
                    ).apply {
                        putExtra("USUARIO_ID", usuario.id)
                        putExtra("USUARIO_NOMBRE", usuario.nombre)
                    }

                    startActivity(intent)
                    finish()
                } else {
                    mostrarMensaje("Correo o contraseña incorrectos.")
                }
            } catch (e: Exception) {
                mostrarMensaje("Ocurrió un error al iniciar sesión.")
            } finally {
                binding.btnIniciarSesion.isEnabled = true
            }
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        binding.tvMensajeLogin.text = mensaje
    }
}
