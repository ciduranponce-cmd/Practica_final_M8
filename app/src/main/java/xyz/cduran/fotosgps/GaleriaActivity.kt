package xyz.cduran.fotosgps

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.cduran.fotosgps.data.AppDatabase
import xyz.cduran.fotosgps.databinding.ActivityGaleriaBinding

class GaleriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGaleriaBinding
    private lateinit var adaptador: FotoAdapter

    private var usuarioId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGaleriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioId = intent.getIntExtra("USUARIO_ID", -1)

        if (usuarioId <= 0) {
            finish()
            return
        }

        adaptador = FotoAdapter(emptyList())

        binding.rvFotografias.layoutManager =
            LinearLayoutManager(this)

        binding.rvFotografias.adapter = adaptador

        binding.btnVolverGaleria.setOnClickListener {
            finish()
        }
    }

    private fun cargarFotografias() {
        lifecycleScope.launch {
            try {
                val fotografias = withContext(Dispatchers.IO) {
                    AppDatabase.obtenerInstancia(applicationContext)
                        .fotoDao()
                        .listarPorUsuario(usuarioId)
                }

                adaptador.actualizarLista(fotografias)

                binding.tvGaleriaVacia.visibility =
                    if (fotografias.isEmpty()) View.VISIBLE
                    else View.GONE

                binding.rvFotografias.visibility =
                    if (fotografias.isEmpty()) View.GONE
                    else View.VISIBLE

            } catch (e: Exception) {
                binding.tvGaleriaVacia.visibility = View.VISIBLE
                binding.tvGaleriaVacia.text =
                    "No se pudieron cargar las fotografías."
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (::binding.isInitialized && usuarioId > 0) {
            cargarFotografias()
        }
    }
}
