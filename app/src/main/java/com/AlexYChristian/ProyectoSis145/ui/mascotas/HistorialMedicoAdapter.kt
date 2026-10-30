package com.AlexYChristian.ProyectoSis145.ui.mascotas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.entity.HistorialMedico
import com.AlexYChristian.ProyectoSis145.util.MascotaUtils

class HistorialMedicoAdapter : RecyclerView.Adapter<HistorialMedicoAdapter.HistorialViewHolder>() {

    private var historialList: List<HistorialMedico> = emptyList()

    fun actualizar(lista: List<HistorialMedico>) {
        historialList = lista
        notifyDataSetChanged()
    }

    class HistorialViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvFechaHistorial: TextView = vista.findViewById(R.id.tvFechaHistorial)
        val tvPesoHistorial: TextView = vista.findViewById(R.id.tvPesoHistorial)
        val tvDiagnostico: TextView = vista.findViewById(R.id.tvDiagnostico)
        val tvTratamiento: TextView = vista.findViewById(R.id.tvTratamiento)
        val tvNotasHistorial: TextView = vista.findViewById(R.id.tvNotasHistorial)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_historial_medico, parent, false)
        return HistorialViewHolder(vista)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val item = historialList[position]
        holder.tvFechaHistorial.text = "📅 ${MascotaUtils.fechaParaMostrar(item.fecha)}"
        holder.tvPesoHistorial.text = "⚖️ ${item.peso} kg"
        holder.tvDiagnostico.text = "🩺 Diagnóstico: ${item.diagnostico}"
        holder.tvTratamiento.text = "💊 Tratamiento: ${item.tratamiento}"

        if (item.notas.isNotEmpty()) {
            holder.tvNotasHistorial.text = "📝 Notas: ${item.notas}"
            holder.tvNotasHistorial.visibility = View.VISIBLE
        } else {
            holder.tvNotasHistorial.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = historialList.size
}
