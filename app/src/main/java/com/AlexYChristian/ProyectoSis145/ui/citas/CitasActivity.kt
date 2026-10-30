package com.AlexYChristian.ProyectoSis145.ui.citas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Cita
import com.AlexYChristian.ProyectoSis145.data.entity.Vacuna
import com.AlexYChristian.ProyectoSis145.data.model.CitaDetalle
import com.AlexYChristian.ProyectoSis145.util.CitaUtils
import com.AlexYChristian.ProyectoSis145.util.MakeWebhook
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * Mis citas.
 * 1. Lee todas las citas del usuario con CitaDao.obtenerPorUsuario() (JOIN con mascota y servicio).
 * 2. Pestañas:
 *    - Próximas: pendientes de hoy en adelante (de la más cercana a la más lejana).
 *    - Pasadas: completadas, canceladas o con fecha vencida (la más reciente primero).
 *    - Todas.
 * 3. Al tocar una cita pendiente se puede marcar como completada o cancelarla.
 *    Si es de vacunación, al completarla se registra la vacuna en la ficha de la mascota.
 * 4. Cada cambio se avisa al escenario de Make (MakeWebhook).
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
        estaActiva(cita) && cita.fecha >= CitaUtils.hoyBD()

    /** Pendiente o confirmada por el veterinario: todavía se puede completar o cancelar. */
    private fun estaActiva(cita: CitaDetalle) =
        cita.estado == Cita.ESTADO_PENDIENTE || cita.estado == Cita.ESTADO_CONFIRMADA

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
        if (!estaActiva(cita)) return
        val opciones = arrayOf(getString(R.string.citas_completar), getString(R.string.citas_cancelar))
        MaterialAlertDialogBuilder(this)
            .setTitle("${cita.servicioNombre} · ${cita.mascotaNombre}")
            .setItems(opciones) { _, posicion ->
                when {
                    posicion == 1 -> cambiarEstado(cita, Cita.ESTADO_CANCELADA)
                    // Si era una cita de vacunación, se pregunta qué vacuna se aplicó
                    esVacunacion(cita) -> mostrarDialogoVacuna(cita)
                    else -> cambiarEstado(cita, Cita.ESTADO_COMPLETADA)
                }
            }
            .setNegativeButton(R.string.citas_cerrar, null)
            .show()
    }

    private fun esVacunacion(cita: CitaDetalle) = "vacun" in cita.servicioNombre.lowercase()

    /**
     * Diálogo para registrar la vacuna aplicada: nombre (lista o escrito) y cuándo es la
     * próxima dosis. Al guardar, la cita queda COMPLETADA y la vacuna pasa a la ficha.
     */
    private fun mostrarDialogoVacuna(cita: CitaDetalle) {
        val vista = LayoutInflater.from(this).inflate(R.layout.dialog_vacuna, null)
        val tilVacuna = vista.findViewById<TextInputLayout>(R.id.tilVacuna)
        val actvVacuna = vista.findViewById<MaterialAutoCompleteTextView>(R.id.actvVacuna)
        val chipsRefuerzo = vista.findViewById<ChipGroup>(R.id.chipsRefuerzo)

        actvVacuna.setSimpleItems(resources.getStringArray(R.array.vacunas_comunes))

        val dialogo = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.vacuna_titulo, cita.mascotaNombre))
            .setView(vista)
            .setPositiveButton(R.string.vacuna_guardar, null)   // se configura abajo para validar
            .setNeutralButton(R.string.vacuna_omitir) { _, _ -> cambiarEstado(cita, Cita.ESTADO_COMPLETADA) }
            .setNegativeButton(R.string.citas_cerrar, null)
            .create()

        dialogo.setOnShowListener {
            dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = actvVacuna.text.toString().trim()
                if (nombre.isEmpty()) {
                    tilVacuna.error = getString(R.string.vacuna_error_nombre)
                    return@setOnClickListener
                }
                val proximaDosis = when (chipsRefuerzo.checkedChipId) {
                    R.id.chipRefuerzo1Mes -> CitaUtils.fechaEnMeses(1)
                    R.id.chipRefuerzo6Meses -> CitaUtils.fechaEnMeses(6)
                    R.id.chipSinRefuerzo -> ""
                    else -> CitaUtils.fechaEnMeses(12)   // "En 1 año"
                }
                dialogo.dismiss()
                cambiarEstado(cita, Cita.ESTADO_COMPLETADA, nombre, proximaDosis)
            }
        }
        dialogo.show()
    }

    /**
     * Cambia el estado de la cita. Si se pasa una vacuna, también la guarda en la ficha.
     * Después avisa a Make: "cita_estado" y, si hubo vacuna, "vacuna_registrada".
     */
    private fun cambiarEstado(
        cita: CitaDetalle,
        estado: String,
        vacunaNombre: String? = null,
        proximaDosis: String = ""
    ) {
        lifecycleScope.launch {
            val db = VeterinariaDatabase.getInstance(this@CitasActivity)
            db.citaDao().actualizarEstado(cita.id, estado)

            var vacuna: Vacuna? = null
            if (vacunaNombre != null) {
                val nueva = Vacuna(
                    mascotaId = cita.mascotaId,
                    nombre = vacunaNombre,
                    fechaAplicacion = CitaUtils.hoyBD(),
                    proximaDosis = proximaDosis
                )
                vacuna = nueva.copy(id = db.vacunaDao().insertar(nueva).toInt())
            }

            // Avisar a Make con los datos completos
            val usuario = db.usuarioDao().obtenerPorId(SessionManager(this@CitasActivity).obtenerUsuarioId())
            val mascota = db.mascotaDao().obtenerPorId(cita.mascotaId)
            val servicio = db.servicioDao().obtenerPorId(cita.servicioId)
            if (usuario != null && mascota != null && servicio != null) {
                val citaActualizada = Cita(
                    id = cita.id,
                    mascotaId = cita.mascotaId,
                    servicioId = cita.servicioId,
                    fecha = cita.fecha,
                    hora = cita.hora,
                    estado = estado,
                    notas = cita.notas
                )
                MakeWebhook.citaCambioEstado(usuario, mascota, servicio, citaActualizada)
                vacuna?.let { MakeWebhook.vacunaRegistrada(usuario, mascota, it) }
            }

            val mensaje = if (vacuna != null) getString(R.string.vacuna_guardada, cita.mascotaNombre)
            else getString(R.string.citas_actualizada)
            Toast.makeText(this@CitasActivity, mensaje, Toast.LENGTH_SHORT).show()
            cargarCitas()
        }
    }
}
