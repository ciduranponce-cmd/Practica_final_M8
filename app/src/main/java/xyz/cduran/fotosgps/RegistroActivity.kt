
package xyz.cduran.fotosgps

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.cduran.fotosgps.data.AppDatabase
import xyz.cduran.fotosgps.data.entity.UsuarioEntity
import xyz.cduran.fotosgps.databinding.ActivityRegistroBinding

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun registrarUsuario() {
        val nombre = binding.etNombre.text.toString().trim()
        val apellidos = binding.etApellidos.text.toString().trim()
        val correo = binding.etCorreo.text.toString()
            .trim()
            .lowercase()
        val contrasena = binding.etContrasena.text.toString()
        val confirmar = binding.etConfirmarContrasena.text.toString()

        // Validar campos obligatorios
        if (
            nombre.isBlank() ||
            apellidos.isBlank() ||
            correo.isBlank() ||
            contrasena.isBlank() ||
            confirmar.isBlank()
        ) {
            mostrarMensaje("Completa todos los campos.")
            return
        }

        // Validar formato del correo
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            mostrarMensaje("Ingresa un correo electrónico válido.")
            return
        }

        // Validar longitud mínima de la contraseña
        if (contrasena.length < 8) {
            mostrarMensaje("La contraseña debe tener al menos 8 caracteres.")
            return
        }

        // Comprobar que las contraseñas coincidan
        if (contrasena != confirmar) {
            mostrarMensaje("Las contraseñas no coinciden.")
            return
        }

        binding.btnRegistrar.isEnabled = false
        binding.tvMensaje.text = "Registrando usuario..."

        lifecycleScope.launch {
            try {
                val resultado = withContext(Dispatchers.IO) {
                    val dao = AppDatabase
                        .obtenerInstancia(applicationContext)
                        .usuarioDao()

                    val usuario = UsuarioEntity(
                        nombre = nombre,
                        apellidos = apellidos,
                        correo = correo,
                        contrasenaHash =
                            PasswordHasher.generarHash(contrasena)
                    )

                    dao.insertar(usuario)
                }

                if (resultado == -1L) {
                    mostrarMensaje(
                        "Ese correo ya está registrado."
                    )
                } else {
                    Toast.makeText(
                        this@RegistroActivity,
                        "Usuario registrado correctamente.",
                        Toast.LENGTH_LONG
                    ).show()

                    limpiarFormulario()
                    binding.tvMensaje.text =
                        "Registro exitoso. Ya puedes iniciar sesión."
                }
            } catch (e: Exception) {
                mostrarMensaje(
                    "No se pudo registrar el usuario. Inténtalo de nuevo."
                )
            } finally {
                binding.btnRegistrar.isEnabled = true
            }
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        binding.tvMensaje.text = mensaje
    }

    private fun limpiarFormulario() {
        binding.etNombre.text?.clear()
        binding.etApellidos.text?.clear()
        binding.etCorreo.text?.clear()
        binding.etContrasena.text?.clear()
        binding.etConfirmarContrasena.text?.clear()
    }
}
