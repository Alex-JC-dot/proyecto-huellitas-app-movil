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
import com.AlexYChristian.ProyectoSis145.ui.mascotas.DetalleMascotaActivity
import com.AlexYChristian.ProyectoSis145.ui.mascotas.RegistrarMascotaActivity
import com.AlexYChristian.ProyectoSis145.util.CitaUtils
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

/**
 * Agendar cita.
 * 1. Carga las mascotas del usuario (chips) y los servicios (tarjetas de colores).
 *    Si viene desde la ficha de una mascota, esa mascota ya aparece elegida.
 * 2. Fecha con calendario (solo de hoy en adelante) y hora con reloj de 24 h.
 * 3. Valida y guarda con CitaDao.insertar(); la cita queda PENDIENTE.
 * 4. Al terminar abre "Mis citas".
 */
class AgendarCitaActivity : AppCompatActivity() {

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

    // Qué mascota representa cada chip (id del chip → mascota)
    private val mascotaPorChip = mutableMapOf<Int, Mascota>()

    // Selección del usuario
    private var servicioElegido: Servicio? = null
    private var fechaBD: String = ""   // "yyyy-MM-dd"
    private var hora: String = ""      // "HH:mm"

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

        etFecha.setOnClickListener { mostrarCalendario() }
        etHora.setOnClickListener { mostrarReloj() }
        chipsMascotas.setOnCheckedStateChangeListener { _, _ -> tvErrorMascota.visibility = View.GONE }
        btnConfirmar.setOnClickListener { confirmarCita() }
        findViewById<MaterialButton>(R.id.btnIrRegistrarMascota).setOnClickListener {
            startActivity(Intent(this, RegistrarMascotaActivity::class.java))
        }
    }

    // Se cargan los datos en onResume: si fue a registrar una mascota, al volver ya aparece
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

    /** Crea un chip por mascota. */
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
        // Si solo tiene una mascota, se elige sola
        if (mascotas.size == 1) (chipsMascotas.getChildAt(0) as Chip).isChecked = true
    }

    /** Crea una tarjeta por servicio; al tocarla queda elegida (borde y fondo de su color). */
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

    /** Calendario: solo deja elegir de hoy en adelante. */
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

    /** Reloj de 24 horas. */
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
        // Si es para hoy, la hora no puede haber pasado ("HH:mm" se compara bien como texto)
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
                notas = etNotas.text.toString().trim()
            )
            VeterinariaDatabase.getInstance(this@AgendarCitaActivity).citaDao().insertar(cita)

            Toast.makeText(
                this@AgendarCitaActivity,
                getString(R.string.agendar_exito, mascota.nombre, MascotaUtils.fechaParaMostrar(fechaBD), hora),
                Toast.LENGTH_LONG
            ).show()

            // Ir a "Mis citas" y cerrar esta pantalla
            startActivity(Intent(this@AgendarCitaActivity, CitasActivity::class.java))
            finish()
        }
    }
}
