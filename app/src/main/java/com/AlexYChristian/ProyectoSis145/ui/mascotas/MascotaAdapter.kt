package com.AlexYChristian.ProyectoSis145.ui.mascotas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import com.AlexYChristian.ProyectoSis145.util.FotoUtils
import com.AlexYChristian.ProyectoSis145.util.MascotaUtils

/**
 * Adapter del RecyclerView de "Mis mascotas".
 *
 * El RecyclerView NO crea una vista por cada mascota: crea solo las que caben en
 * pantalla y las RECICLA al hacer scroll. El Adapter es el "puente" entre la lista
 * de datos (List<Mascota>) y esas vistas:
 *  - onCreateViewHolder: crea una tarjeta vacía a partir de item_mascota.xml
 *  - onBindViewHolder:   llena una tarjeta con los datos de una mascota
 *  - getItemCount:       cuántas mascotas hay
 *
 * @param alTocarMascota función que se ejecuta cuando tocan una tarjeta
 */
class MascotaAdapter(
    private val alTocarMascota: (Mascota) -> Unit
) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    private var mascotas: List<Mascota> = emptyList()

    /** Reemplaza la lista y redibuja. */
    fun actualizar(nuevasMascotas: List<Mascota>) {
        mascotas = nuevasMascotas
        notifyDataSetChanged()
    }

    /**
     * ViewHolder: guarda las referencias a las vistas de UNA tarjeta,
     * para no tener que hacer findViewById cada vez que se recicla.
     */
    class MascotaViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val ivFoto: ImageView = vista.findViewById(R.id.ivFoto)
        val ivSilueta: ImageView = vista.findViewById(R.id.ivSilueta)
        val tvNombre: TextView = vista.findViewById(R.id.tvNombre)
        val tvSexo: TextView = vista.findViewById(R.id.tvSexo)
        val tvDetalle: TextView = vista.findViewById(R.id.tvDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mascota, parent, false)
        return MascotaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = mascotas[position]

        holder.tvNombre.text = mascota.nombre
        holder.tvSexo.text = MascotaUtils.simboloSexo(mascota.sexo)
        holder.tvDetalle.text = "${mascota.raza} · ${MascotaUtils.calcularEdad(mascota.fechaNacimiento)}"
        FotoUtils.mostrar(holder.ivFoto, holder.ivSilueta, mascota)

        holder.itemView.setOnClickListener { alTocarMascota(mascota) }
    }

    override fun getItemCount(): Int = mascotas.size
}
