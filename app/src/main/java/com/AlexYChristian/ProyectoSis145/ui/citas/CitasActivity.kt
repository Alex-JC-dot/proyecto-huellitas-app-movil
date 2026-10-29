package com.AlexYChristian.ProyectoSis145.ui.citas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
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
import com.AlexYChristian.ProyectoSis145.util.CitaUtils
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

/**
 * Mis citas.
 * 1. Lee todas las citas del usuario con CitaDao.obtenerPorUsuario() (JOIN con mascota y servicio).
 * 2. Pestañas:
 *    - Próximas: pendientes de hoy en adelante (de la más cercana a la más lejana).
 *    - Pasadas: completadas, canceladas o con fecha vencida (la más reciente primero).
 *    - Todas.
 * 3. Al tocar una cita pendiente se puede marcar como completada o cancelarla.
 */
class CitasActivity : AppCompatActivity() {

    private lateinit var adapter: CitaAdapter
    private lateinit var tabs: TabLayout
    private lateinit var rvCitas: RecyclerView
    private lateinit var layoutVacio: LinearLayout

    private var todasLasCitas: List<CitaDetalle> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_citas)
        configurarCabeceraVerde(findViewById(R.id.main), findViewById(R.id.contenidoCabecera))

        tabs = findViewById(R.id.tabsCitas)
        rvCitas = findViewById(R.id.rvCitas)
        layoutVacio = findViewById(R.id.layoutVacio)

        adapter = CitaAdapter { cita -> mostrarOpciones(cita) }
        rvCitas.layoutManager = LinearLayoutManager(this)
        rvCitas.adapter = adapter

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = aplicarPestana()
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        findViewById<ExtendedFloatingActionButton>(R.id.fabAgendar).setOnClickListener {
            startActivity(Intent(this, AgendarCitaActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarCitas()
    }

    private fun cargarCitas() {
        val usuarioId = SessionManager(this).obtenerUsuarioId()
        lifecycleScope.launch {
            todasLasCitas = VeterinariaDatabase.getInstance(this@CitasActivity)
                .citaDao()
                .obtenerPorUsuario(usuarioId)
            aplicarPestana()
        }
    }

    /** Una cita es "próxima" si está pendiente y su fecha es hoy o después. */
    private fun esProxima(cita: CitaDetalle): Boolean =
        cita.estado == Cita.ESTADO_PENDIENTE && cita.fecha >= CitaUtils.hoyBD()

    private fun aplicarPestana() {
        val lista = when (tabs.selectedTabPosition) {
            0 -> todasLasCitas.filter { esProxima(it) }                       // ya vienen ordenadas
            1 -> todasLasCitas.filter { !esProxima(it) }.reversed()            // la más reciente primero
            else -> todasLasCitas
        }
        adapter.actualizar(lista)
        rvCitas.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
        layoutVacio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    /** Opciones de una cita pendiente: completarla o cancelarla. */
    private fun mostrarOpciones(cita: CitaDetalle) {
        if (cita.estado != Cita.ESTADO_PENDIENTE) return
        val opciones = arrayOf(getString(R.string.citas_completar), getString(R.string.citas_cancelar))
        MaterialAlertDialogBuilder(this)
            .setTitle("${cita.servicioNombre} · ${cita.mascotaNombre}")
            .setItems(opciones) { _, posicion ->
                val nuevoEstado = if (posicion == 0) Cita.ESTADO_COMPLETADA else Cita.ESTADO_CANCELADA
                cambiarEstado(cita, nuevoEstado)
            }
            .setNegativeButton(R.string.citas_cerrar, null)
            .show()
    }

    private fun cambiarEstado(cita: CitaDetalle, estado: String) {
        lifecycleScope.launch {
            VeterinariaDatabase.getInstance(this@CitasActivity).citaDao().actualizarEstado(cita.id, estado)
            Toast.makeText(this@CitasActivity, R.string.citas_actualizada, Toast.LENGTH_SHORT).show()
            cargarCitas()
        }
    }
}
