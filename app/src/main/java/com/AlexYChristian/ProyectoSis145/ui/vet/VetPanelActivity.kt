package com.AlexYChristian.ProyectoSis145.ui.vet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Cita
import com.AlexYChristian.ProyectoSis145.data.model.CitaDetalle
import com.AlexYChristian.ProyectoSis145.ui.auth.LoginActivity
import com.AlexYChristian.ProyectoSis145.ui.citas.CitaAdapter
import com.AlexYChristian.ProyectoSis145.ui.mascotas.HistorialMedicoActivity
import com.AlexYChristian.ProyectoSis145.util.MakeWebhook
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

/**
 * Panel del Veterinario.
 * - Ve sus citas y las que todavía no tienen veterinario.
 * - Puede confirmar, atender (abre la ficha médica) o cancelar una cita.
 * - Al confirmar o atender, la cita queda asignada a él.
 * - Cada cambio se avisa al escenario de Make (cita_estado) con los datos del dueño.
 */
class VetPanelActivity : AppCompatActivity() {

    private lateinit var tvNombreVetHeader: TextView
    private lateinit var tvEspecialidadHeader: TextView
    private lateinit var tabs: TabLayout
    private lateinit var rvVetCitas: RecyclerView
    private lateinit var layoutVacioVetCitas: LinearLayout

    private lateinit var adapter: CitaAdapter
    private var todasLasCitas: List<CitaDetalle> = emptyList()
    private var vetId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vet_panel)
        configurarCabeceraVerde(findViewById(R.id.main), findViewById(R.id.contenidoCabecera))

        vetId = SessionManager(this).obtenerUsuarioId()

        tvNombreVetHeader = findViewById(R.id.tvNombreVetHeader)
        tvEspecialidadHeader = findViewById(R.id.tvEspecialidadHeader)
        tabs = findViewById(R.id.tabsVetCitas)
        rvVetCitas = findViewById(R.id.rvVetCitas)
        layoutVacioVetCitas = findViewById(R.id.layoutVacioVetCitas)

        adapter = CitaAdapter { cita -> mostrarOpcionesVet(cita) }
        rvVetCitas.layoutManager = LinearLayoutManager(this)
        rvVetCitas.adapter = adapter

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = aplicarPestana()
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        findViewById<MaterialButton>(R.id.btnCerrarSesionVet).setOnClickListener {
            SessionManager(this).cerrarSesion()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        cargarPerfilVet()
    }

    override fun onResume() {
        super.onResume()
        cargarCitasVet()
    }

    private fun cargarPerfilVet() {
        lifecycleScope.launch {
            val db = VeterinariaDatabase.getInstance(this@VetPanelActivity)
            val vet = db.usuarioDao().obtenerPorId(vetId)
            vet?.let {
                tvNombreVetHeader.text = it.nombre
                tvEspecialidadHeader.text = "👨‍⚕️ ${if (it.especialidad.isEmpty()) "Veterinario General" else it.especialidad}"
            }
        }
    }

    private fun cargarCitasVet() {
        lifecycleScope.launch {
            todasLasCitas = VeterinariaDatabase.getInstance(this@VetPanelActivity)
                .citaDao()
                .obtenerPorVeterinario(vetId)
            aplicarPestana()
        }
    }

    private fun aplicarPestana() {
        val lista = when (tabs.selectedTabPosition) {
            0 -> todasLasCitas.filter { it.estado == Cita.ESTADO_PENDIENTE || it.estado == Cita.ESTADO_CONFIRMADA }
            1 -> todasLasCitas.filter { it.estado == Cita.ESTADO_COMPLETADA }
            else -> todasLasCitas
        }
        adapter.actualizar(lista)
        rvVetCitas.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
        layoutVacioVetCitas.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarOpcionesVet(cita: CitaDetalle) {
        val opciones = arrayOf(
            "✅ Confirmar Cita",
            "🩺 Atender & Agregar Ficha Médica",
            "❌ Cancelar Cita"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("${cita.servicioNombre} · ${cita.mascotaNombre}")
            .setItems(opciones) { _, posicion ->
                when (posicion) {
                    0 -> cambiarEstadoYCita(cita, Cita.ESTADO_CONFIRMADA)
                    1 -> {
                        cambiarEstadoYCita(cita, Cita.ESTADO_COMPLETADA)
                        val intent = Intent(this, HistorialMedicoActivity::class.java).apply {
                            putExtra("MASCOTA_ID", cita.mascotaId)
                            putExtra("MASCOTA_NOMBRE", cita.mascotaNombre)
                        }
                        startActivity(intent)
                    }
                    2 -> cambiarEstadoYCita(cita, Cita.ESTADO_CANCELADA)
                }
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun cambiarEstadoYCita(cita: CitaDetalle, nuevoEstado: String) {
        lifecycleScope.launch {
            val db = VeterinariaDatabase.getInstance(this@VetPanelActivity)
            db.citaDao().actualizarEstado(cita.id, nuevoEstado)
            db.citaDao().asignarVeterinario(cita.id, vetId)

            // Avisar a Make: el correo le llega al DUEÑO de la mascota
            val mascota = db.mascotaDao().obtenerPorId(cita.mascotaId)
            val servicio = db.servicioDao().obtenerPorId(cita.servicioId)
            val duenio = mascota?.let { db.usuarioDao().obtenerPorId(it.usuarioId) }
            if (mascota != null && servicio != null && duenio != null) {
                val citaActualizada = Cita(
                    id = cita.id,
                    mascotaId = cita.mascotaId,
                    servicioId = cita.servicioId,
                    fecha = cita.fecha,
                    hora = cita.hora,
                    estado = nuevoEstado,
                    notas = cita.notas,
                    veterinarioId = vetId
                )
                MakeWebhook.citaCambioEstado(duenio, mascota, servicio, citaActualizada)
            }

            Toast.makeText(
                this@VetPanelActivity,
                "Cita ${nuevoEstado.lowercase()}",
                Toast.LENGTH_SHORT
            ).show()
            cargarCitasVet()
        }
    }
}
