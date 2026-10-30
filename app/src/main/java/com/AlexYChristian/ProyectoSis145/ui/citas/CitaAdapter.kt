package com.AlexYChristian.ProyectoSis145.ui.citas

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.entity.Cita
import com.AlexYChristian.ProyectoSis145.data.model.CitaDetalle
import com.AlexYChristian.ProyectoSis145.util.CitaUtils
import com.AlexYChristian.ProyectoSis145.util.MascotaUtils
import com.AlexYChristian.ProyectoSis145.util.ServicioEstilo

/**
 * Adapter de "Mis citas". Funciona igual que MascotaAdapter:
 * crea tarjetas a partir de item_cita.xml y las llena con cada CitaDetalle
 * (cita + nombre de la mascota + nombre y precio del servicio, del JOIN del CitaDao).
 */
class CitaAdapter(
    private val alTocarCita: (CitaDetalle) -> Unit
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    private var citas: List<CitaDetalle> = emptyList()

    fun actualizar(nuevasCitas: List<CitaDetalle>) {
        citas = nuevasCitas
        notifyDataSetChanged()
    }

    class CitaViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val barraColor: View = vista.findViewById(R.id.barraColor)
        val tvCuenta: TextView = vista.findViewById(R.id.tvCuenta)
        val tvFechaHora: TextView = vista.findViewById(R.id.tvFechaHora)
        val tvServicio: TextView = vista.findViewById(R.id.tvServicio)
        val tvMascotaPrecio: TextView = vista.findViewById(R.id.tvMascotaPrecio)
        val tvNotas: TextView = vista.findViewById(R.id.tvNotas)
        val fondoIcono: FrameLayout = vista.findViewById(R.id.fondoIcono)
        val ivIcono: ImageView = vista.findViewById(R.id.ivIcono)
        val tvEstado: TextView = vista.findViewById(R.id.tvEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = citas[position]
        val ctx = holder.itemView.context
        val estiloServicio = ServicioEstilo.deServicio(cita.servicioNombre)

        // Barra lateral e ícono con el color del servicio
        holder.barraColor.setBackgroundColor(ContextCompat.getColor(ctx, estiloServicio.color))
        CitaUtils.pintarIcono(holder.fondoIcono, holder.ivIcono, cita.servicioNombre)

        holder.tvServicio.text = cita.servicioNombre
        holder.tvFechaHora.text = "${MascotaUtils.fechaParaMostrar(cita.fecha)} · ${cita.hora}"
        holder.tvMascotaPrecio.text = "${cita.mascotaNombre} · ${CitaUtils.precio(cita.servicioPrecio)}"

        if (cita.notas.isNotEmpty()) {
            holder.tvNotas.text = "📝 ${cita.notas}"
            holder.tvNotas.visibility = View.VISIBLE
        } else {
            holder.tvNotas.visibility = View.GONE
        }

        // Cuenta regresiva: solo para citas pendientes que todavía no pasaron
        val cuenta = if (cita.estado == Cita.ESTADO_PENDIENTE || cita.estado == Cita.ESTADO_CONFIRMADA) CitaUtils.textoCuentaRegresiva(ctx, cita.fecha) else null
        if (cuenta != null) {
            holder.tvCuenta.text = cuenta
            holder.tvCuenta.visibility = View.VISIBLE
        } else {
            holder.tvCuenta.visibility = View.GONE
        }

        // Etiqueta de estado con su color
        val estiloEstado = ServicioEstilo.deEstado(cita.estado)
        holder.tvEstado.text = ServicioEstilo.textoEstado(cita.estado)
        holder.tvEstado.setTextColor(ContextCompat.getColor(ctx, estiloEstado.color))
        ViewCompat.setBackgroundTintList(
            holder.tvEstado,
            ColorStateList.valueOf(ContextCompat.getColor(ctx, estiloEstado.fondo))
        )

        holder.itemView.setOnClickListener { alTocarCita(cita) }
    }

    override fun getItemCount(): Int = citas.size
}
