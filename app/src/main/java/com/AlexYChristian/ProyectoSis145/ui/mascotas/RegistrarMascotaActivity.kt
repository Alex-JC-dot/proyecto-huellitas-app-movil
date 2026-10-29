package com.AlexYChristian.ProyectoSis145.ui.mascotas

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import com.AlexYChristian.ProyectoSis145.util.FotoUtils
import com.AlexYChristian.ProyectoSis145.util.MascotaUtils
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Registrar mascota.
 * 1. Foto: al tocar el círculo se abre el selector de fotos del celular (Photo Picker).
 *    No necesita pedir permisos: el sistema solo le entrega a la app la foto elegida.
 * 2. Especie y sexo se eligen con chips; la fecha con un calendario (MaterialDatePicker).
 * 3. Se validan los campos y se guarda con MascotaDao.insertar().
 *    La foto se copia a la carpeta privada de la app y en la BD se guarda su ruta.
 */
class RegistrarMascotaActivity : AppCompatActivity() {

    private lateinit var ivFoto: ShapeableImageView
    private lateinit var tvAgregarFoto: TextView
    private lateinit var tvTextoFoto: TextView

    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilRaza: TextInputLayout
    private lateinit var tilNacimiento: TextInputLayout
    private lateinit var tilPeso: TextInputLayout
    private lateinit var etNombre: TextInputEditText
    private lateinit var etRaza: TextInputEditText
    private lateinit var etNacimiento: TextInputEditText
    private lateinit var etPeso: TextInputEditText

    private lateinit var chipsEspecie: ChipGroup
    private lateinit var chipsSexo: ChipGroup
    private lateinit var tvErrorEspecie: TextView
    private lateinit var tvErrorSexo: TextView
    private lateinit var btnGuardar: MaterialButton

    private var fotoElegida: Uri? = null          // la foto que eligió (todavía sin copiar)
    private var fechaNacimientoBD: String = ""     // "yyyy-MM-dd", como se guarda en la BD

    /**
     * Selector de fotos del sistema. Se registra ANTES de que la pantalla se muestre;
     * cuando el usuario elige una foto, Android llama a esta función con su Uri.
     */
    private val selectorDeFoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                fotoElegida = uri
                ivFoto.setImageURI(uri)
                tvAgregarFoto.visibility = View.GONE
                tvTextoFoto.setText(R.string.registrar_mascota_cambiar_foto)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_mascota)
        configurarCabeceraVerde(findViewById(R.id.main), findViewById(R.id.contenidoCabecera))

        ivFoto = findViewById(R.id.ivFoto)
        tvAgregarFoto = findViewById(R.id.tvAgregarFoto)
        tvTextoFoto = findViewById(R.id.tvTextoFoto)
        tilNombre = findViewById(R.id.tilNombre)
        tilRaza = findViewById(R.id.tilRaza)
        tilNacimiento = findViewById(R.id.tilNacimiento)
        tilPeso = findViewById(R.id.tilPeso)
        etNombre = findViewById(R.id.etNombre)
        etRaza = findViewById(R.id.etRaza)
        etNacimiento = findViewById(R.id.etNacimiento)
        etPeso = findViewById(R.id.etPeso)
        chipsEspecie = findViewById(R.id.chipsEspecie)
        chipsSexo = findViewById(R.id.chipsSexo)
        tvErrorEspecie = findViewById(R.id.tvErrorEspecie)
        tvErrorSexo = findViewById(R.id.tvErrorSexo)
        btnGuardar = findViewById(R.id.btnGuardar)

        // Tocar la foto (o su texto) abre la galería, solo imágenes
        val abrirGaleria = View.OnClickListener {
            selectorDeFoto.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        findViewById<FrameLayout>(R.id.contenedorFoto).setOnClickListener(abrirGaleria)
        tvTextoFoto.setOnClickListener(abrirGaleria)

        etNacimiento.setOnClickListener { mostrarCalendario() }

        // Al elegir un chip, se borra su error
        chipsEspecie.setOnCheckedStateChangeListener { _, _ -> tvErrorEspecie.visibility = View.GONE }
        chipsSexo.setOnCheckedStateChangeListener { _, _ -> tvErrorSexo.visibility = View.GONE }

        btnGuardar.setOnClickListener { guardarMascota() }
    }

    /** Calendario de Material. Solo deja elegir fechas de hoy hacia atrás. */
    private fun mostrarCalendario() {
        val restricciones = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()
        val calendario = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.campo_mascota_nacimiento)
            .setCalendarConstraints(restricciones)
            .build()

        calendario.addOnPositiveButtonClickListener { milisegundos ->
            // El calendario devuelve la fecha en UTC: se formatea en UTC para no correrse un día
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            fechaNacimientoBD = formato.format(Date(milisegundos))
            etNacimiento.setText(MascotaUtils.fechaParaMostrar(fechaNacimientoBD))
            tilNacimiento.error = null
        }
        calendario.show(supportFragmentManager, "calendario")
    }

    private fun guardarMascota() {
        // 1. Limpiar errores
        tilNombre.error = null
        tilRaza.error = null
        tilNacimiento.error = null
        tilPeso.error = null
        tvErrorEspecie.visibility = View.GONE
        tvErrorSexo.visibility = View.GONE

        // 2. Leer los datos
        val nombre = etNombre.text.toString().trim()
        val raza = etRaza.text.toString().trim()
        val peso = etPeso.text.toString().replace(",", ".").toDoubleOrNull()
        val especie = when (chipsEspecie.checkedChipId) {
            R.id.chipPerro -> MascotaUtils.ESPECIE_PERRO
            R.id.chipGato -> MascotaUtils.ESPECIE_GATO
            R.id.chipAve -> MascotaUtils.ESPECIE_AVE
            R.id.chipOtro -> MascotaUtils.ESPECIE_OTRO
            else -> null
        }
        val sexo = when (chipsSexo.checkedChipId) {
            R.id.chipMacho -> MascotaUtils.SEXO_MACHO
            R.id.chipHembra -> MascotaUtils.SEXO_HEMBRA
            else -> null
        }

        // 3. Validar
        if (nombre.isEmpty()) {
            tilNombre.error = getString(R.string.error_mascota_nombre)
            return
        }
        if (especie == null) {
            tvErrorEspecie.setText(R.string.error_mascota_especie)
            tvErrorEspecie.visibility = View.VISIBLE
            return
        }
        if (sexo == null) {
            tvErrorSexo.setText(R.string.error_mascota_sexo)
            tvErrorSexo.visibility = View.VISIBLE
            return
        }
        if (raza.isEmpty()) {
            tilRaza.error = getString(R.string.error_mascota_raza)
            return
        }
        if (fechaNacimientoBD.isEmpty()) {
            tilNacimiento.error = getString(R.string.error_mascota_nacimiento)
            return
        }
        if (peso == null || peso <= 0.0 || peso > 200.0) {
            tilPeso.error = getString(R.string.error_mascota_peso)
            return
        }

        // 4. Guardar
        btnGuardar.isEnabled = false
        lifecycleScope.launch {
            // Copiar la foto es pesado: se hace en un hilo de fondo (Dispatchers.IO)
            val rutaFoto = fotoElegida?.let { uri ->
                withContext(Dispatchers.IO) { FotoUtils.guardarFoto(this@RegistrarMascotaActivity, uri) }
            } ?: ""
            if (fotoElegida != null && rutaFoto.isEmpty()) {
                Toast.makeText(this@RegistrarMascotaActivity, R.string.registrar_mascota_error_foto, Toast.LENGTH_LONG).show()
            }

            val mascota = Mascota(
                usuarioId = SessionManager(this@RegistrarMascotaActivity).obtenerUsuarioId(),
                nombre = nombre,
                especie = especie,
                raza = raza,
                sexo = sexo,
                fechaNacimiento = fechaNacimientoBD,
                peso = peso,
                foto = rutaFoto
            )
            VeterinariaDatabase.getInstance(this@RegistrarMascotaActivity).mascotaDao().insertar(mascota)

            Toast.makeText(
                this@RegistrarMascotaActivity,
                getString(R.string.registrar_mascota_exito, nombre),
                Toast.LENGTH_LONG
            ).show()
            finish()   // vuelve a "Mis mascotas", que se recarga en onResume
        }
    }
}
