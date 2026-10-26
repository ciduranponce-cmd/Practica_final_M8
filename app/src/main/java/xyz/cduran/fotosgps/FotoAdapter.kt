package xyz.cduran.fotosgps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import xyz.cduran.fotosgps.data.entity.FotoEntity
import xyz.cduran.fotosgps.databinding.ItemFotoBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FotoAdapter(
    private var fotografias: List<FotoEntity>
) : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    class FotoViewHolder(
        val binding: ItemFotoBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FotoViewHolder {
        val binding = ItemFotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FotoViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FotoViewHolder,
        position: Int
    ) {
        val foto = fotografias[position]
        val binding = holder.binding

        binding.tvComentarioFoto.text = foto.comentario

        val formatoFecha = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

        binding.tvFechaFoto.text =
            "Fecha: ${formatoFecha.format(Date(foto.fecha))}"

        binding.tvCoordenadasFoto.text =
            "Lat: ${foto.latitud}\nLon: ${foto.longitud}"

        Glide.with(binding.ivMiniatura)
            .load(File(foto.rutaImagen))
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .centerCrop()
            .into(binding.ivMiniatura)

        binding.root.setOnClickListener {
            val contexto = binding.root.context

            val intent = android.content.Intent(
                contexto,
                DetalleFotoActivity::class.java
            ).apply {
                putExtra("FOTO_ID", foto.id)
                putExtra("USUARIO_ID", foto.usuarioId)
            }

            contexto.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = fotografias.size

    fun actualizarLista(nuevaLista: List<FotoEntity>) {
        fotografias = nuevaLista
        notifyDataSetChanged()
    }
}
