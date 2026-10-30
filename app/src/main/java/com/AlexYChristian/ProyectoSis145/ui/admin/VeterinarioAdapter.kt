package com.AlexYChristian.ProyectoSis145.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.google.android.material.button.MaterialButton

class VeterinarioAdapter(
    private val alTocarMapa: (Usuario) -> Unit
) : RecyclerView.Adapter<VeterinarioAdapter.VetViewHolder>() {

    private var veterinarios: List<Usuario> = emptyList()

    fun actualizar(lista: List<Usuario>) {
        veterinarios = lista
        notifyDataSetChanged()
    }

    class VetViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvNombreVet: TextView = vista.findViewById(R.id.tvNombreVet)
        val tvEspecialidad: TextView = vista.findViewById(R.id.tvEspecialidad)
        val tvConsultorio: TextView = vista.findViewById(R.id.tvConsultorio)
        val tvLicencia: TextView = vista.findViewById(R.id.tvLicencia)
        val tvContacto: TextView = vista.findViewById(R.id.tvContacto)
        val btnVerEnMapa: MaterialButton = vista.findViewById(R.id.btnVerEnMapa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VetViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_veterinario, parent, false)
        return VetViewHolder(vista)
    }

    override fun onBindViewHolder(holder: VetViewHolder, position: Int) {
        val vet = veterinarios[position]
        holder.tvNombreVet.text = vet.nombre
        holder.tvEspecialidad.text = "Especialidad: ${if (vet.especialidad.isEmpty()) "General" else vet.especialidad}"
        holder.tvConsultorio.text = "🏥 Consultorio: ${if (vet.consultorio.isEmpty()) "Sin especificar" else vet.consultorio}"
        holder.tvLicencia.text = "📜 Licencia: ${if (vet.licencia.isEmpty()) "N/A" else vet.licencia}"
        holder.tvContacto.text = "📧 ${vet.email} · 📞 ${vet.telefono}"

        holder.btnVerEnMapa.setOnClickListener { alTocarMapa(vet) }
    }

    override fun getItemCount(): Int = veterinarios.size
}
