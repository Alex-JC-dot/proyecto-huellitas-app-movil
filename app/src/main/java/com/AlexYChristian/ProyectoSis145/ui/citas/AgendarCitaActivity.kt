package com.AlexYChristian.ProyectoSis145.ui.citas

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Cita
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import com.AlexYChristian.ProyectoSis145.data.entity.Servicio
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.AlexYChristian.ProyectoSis145.ui.mascotas.DetalleMascotaActivity
import com.AlexYChristian.ProyectoSis145.ui.mascotas.RegistrarMascotaActivity
import com.AlexYChristian.ProyectoSis145.util.CitaUtils
import com.AlexYChristian.ProyectoSis145.util.MakeWebhook
import com.AlexYChristian.ProyectoSis145.util.MascotaUtils
import com.AlexYChristian.ProyectoSis145.util.ServicioEstilo
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AgendarCitaActivity : AppCompatActivity() {

    companion object {
        /** Id del veterinario elegido en el mapa (opcional). */
        const val EXTRA_VET_ID = "VET_ID"
    }

    private lateinit var chipsMascotas: ChipGroup
    private lateinit var layoutServicios: LinearLayout
    private lateinit var tvErrorMascota: TextView
    private lateinit var tvErrorServicio: TextView
    private lateinit var tilFecha: TextInputLayout
    private lateinit var tilHora: TextInputLayout
    private lateinit var etFecha: TextInputEditText
    private lateinit var etHora: TextInputEditText
    private lateinit var etNotas: TextInputEditText
    private lateinit var btnConfirmar: MaterialButton

    private val mascotaPorChip = mutableMapOf<Int, Mascota>()
    private var servicioElegido: Servicio? = null
    private var veterinarioElegidoId: Int? = null
    private var fechaBD: String = ""
    private var hora: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agendar_cita)
        configurarCabeceraVerde(findViewById(R.id.main), findViewById(R.id.contenidoCabecera))

        chipsMascotas = findViewById(R.id.chipsMascotas)
        layoutServicios = findViewById(R.id.layoutServicios)
        tvErrorMascota = findViewById(R.id.tvErrorMascota)
        tvErrorServicio = findViewById(R.id.tvErrorServicio)
        tilFecha = findViewById(R.id.tilFecha)
        tilHora = findViewById(R.id.tilHora)
        etFecha = findViewById(R.id.etFecha)
        etHora = findViewById(R.id.etHora)
        etNotas = findViewById(R.id.etNotas)
        btnConfirmar = findViewById(R.id.btnConfirmar)

        // Si viene desde el mapa de consultorios, la cita queda asignada a ese veterinario
        veterinarioElegidoId = intent.getIntExtra(EXTRA_VET_ID, -1).takeIf { it != -1 }

        etFecha.setOnClickListener { mostrarCalendario() }
        etHora.setOnClickListener { mostrarReloj() }
        chipsMascotas.setOnCheckedStateChangeListener { _, _ -> tvErrorMascota.visibility = View.GONE }
        btnConfirmar.setOnClickListener { confirmarCita() }
        findViewById<MaterialButton>(R.id.btnIrRegistrarMascota).setOnClickListener {
            startActivity(Intent(this, RegistrarMascotaActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        val mascotaInicial = intent.getIntExtra(DetalleMascotaActivity.EXTRA_MASCOTA_ID, -1)
        lifecycleScope.launch {
            val db = VeterinariaDatabase.getInstance(this@AgendarCitaActivity)
            val mascotas = db.mascotaDao().obtenerPorUsuario(SessionManager(this@AgendarCitaActivity).obtenerUsuarioId())
            val servicios = db.servicioDao().obtenerTodos()
            mostrarMascotas(mascotas, mascotaInicial)
            if (layoutServicios.childCount == 0) mostrarServicios(servicios)
        }
    }

    private fun mostrarMascotas(mascotas: List<Mascota>, mascotaInicial: Int) {
        val elegidaAntes = mascotaPorChip[chipsMascotas.checkedChipId]?.id ?: mascotaInicial
        chipsMascotas.removeAllViews()
        mascotaPorChip.clear()

        findViewById<View>(R.id.layoutSinMascotas).visibility =
            if (mascotas.isEmpty()) View.VISIBLE else View.GONE

        for (mascota in mascotas) {
            val chip = LayoutInflater.from(this)
                .inflate(R.layout.item_chip_mascota, chipsMascotas, false) as Chip
            chip.id = View.generateViewId()
            chip.text = "${MascotaUtils.emoji(mascota.especie)} ${mascota.nombre}"
            chipsMascotas.addView(chip)
            mascotaPorChip[chip.id] = mascota
            if (mascota.id == elegidaAntes) chip.isChecked = true
        }
        if (mascotas.size == 1) (chipsMascotas.getChildAt(0) as Chip).isChecked = true
    }

    private fun mostrarServicios(servicios: List<Servicio>) {
        val tarjetas = mutableListOf<Pair<MaterialCardView, Servicio>>()
        for (servicio in servicios) {
            val vista = LayoutInflater.from(this)
                .inflate(R.layout.item_servicio, layoutServicios, false) as MaterialCardView
            CitaUtils.llenarServicio(vista, servicio)
            vista.isClickable = true
            vista.isFocusable = true
            vista.setOnClickListener {
                servicioElegido = servicio
                tvErrorServicio.visibility = View.GONE
                for ((tarjeta, s) in tarjetas) marcarTarjeta(tarjeta, s, s == servicio)
            }
            layoutServicios.addView(vista)
            tarjetas.add(vista to servicio)
        }
    }

    private fun marcarTarjeta(tarjeta: MaterialCardView, servicio: Servicio, elegida: Boolean) {
        val estilo = ServicioEstilo.deServicio(servicio.nombre)
        if (elegida) {
            tarjeta.setCardBackgroundColor(ContextCompat.getColor(this, estilo.fondo))
            tarjeta.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, estilo.color)))
            tarjeta.strokeWidth = (2 * resources.displayMetrics.density).toInt()
        } else {
            tarjeta.setCardBackgroundColor(ContextCompat.getColor(this, R.color.superficie))
            tarjeta.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.fondo_suave)))
            tarjeta.strokeWidth = (1.5f * resources.displayMetrics.density).toInt()
        }
    }

    private fun mostrarCalendario() {
        val hoyUtc = MaterialDatePicker.todayInUtcMilliseconds()
        val calendario = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.agendar_fecha)
            .setSelection(hoyUtc)
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setStart(hoyUtc)
                    .setValidator(DateValidatorPointForward.from(hoyUtc))
                    .build()
            )
            .build()
        calendario.addOnPositiveButtonClickListener { milisegundos ->
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            fechaBD = formato.format(Date(milisegundos))
            etFecha.setText(MascotaUtils.fechaParaMostrar(fechaBD))
            tilFecha.error = null
        }
        calendario.show(supportFragmentManager, "calendario")
    }

    private fun mostrarReloj() {
        val reloj = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(9)
            .setMinute(0)
            .setTitleText(R.string.agendar_hora)
            .build()
        reloj.addOnPositiveButtonClickListener {
            hora = String.format(Locale.US, "%02d:%02d", reloj.hour, reloj.minute)
            etHora.setText(hora)
            tilHora.error = null
        }
        reloj.show(supportFragmentManager, "reloj")
    }

    private fun confirmarCita() {
        tvErrorMascota.visibility = View.GONE
        tvErrorServicio.visibility = View.GONE
        tilFecha.error = null
        tilHora.error = null

        val mascota = mascotaPorChip[chipsMascotas.checkedChipId]
        val servicio = servicioElegido

        if (mascota == null) {
            tvErrorMascota.setText(R.string.error_cita_mascota)
            tvErrorMascota.visibility = View.VISIBLE
            return
        }
        if (servicio == null) {
            tvErrorServicio.setText(R.string.error_cita_servicio)
            tvErrorServicio.visibility = View.VISIBLE
            return
        }
        if (fechaBD.isEmpty()) {
            tilFecha.error = getString(R.string.error_cita_fecha)
            return
        }
        if (hora.isEmpty()) {
            tilHora.error = getString(R.string.error_cita_hora)
            return
        }
        if (fechaBD == CitaUtils.hoyBD() && hora <= CitaUtils.horaActual()) {
            tilHora.error = getString(R.string.error_cita_hora_pasada)
            return
        }

        btnConfirmar.isEnabled = false
        lifecycleScope.launch {
            val cita = Cita(
                mascotaId = mascota.id,
                servicioId = servicio.id,
                fecha = fechaBD,
                hora = hora,
                notas = etNotas.text.toString().trim(),
                veterinarioId = veterinarioElegidoId
            )
            val db = VeterinariaDatabase.getInstance(this@AgendarCitaActivity)
            val citaId = db.citaDao().insertar(cita)

            // Avisar a Make con todos los datos de la cita
            val usuario = db.usuarioDao().obtenerPorId(SessionManager(this@AgendarCitaActivity).obtenerUsuarioId())
            if (usuario != null) {
                MakeWebhook.citaAgendada(usuario, mascota, servicio, cita.copy(id = citaId.toInt()))
            }

            Toast.makeText(
                this@AgendarCitaActivity,
                getString(R.string.agendar_exito, mascota.nombre, MascotaUtils.fechaParaMostrar(fechaBD), hora),
                Toast.LENGTH_LONG
            ).show()

            startActivity(Intent(this@AgendarCitaActivity, CitasActivity::class.java))
            finish()
        }
    }
}
