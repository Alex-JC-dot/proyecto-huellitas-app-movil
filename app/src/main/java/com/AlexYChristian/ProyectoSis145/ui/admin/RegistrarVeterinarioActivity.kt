package com.AlexYChristian.ProyectoSis145.ui.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.AlexYChristian.ProyectoSis145.ui.maps.MapsActivity
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrarVeterinarioActivity : AppCompatActivity() {

    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilTelefono: TextInputLayout
    private lateinit var tilEspecialidad: TextInputLayout
    private lateinit var tilLicencia: TextInputLayout
    private lateinit var tilConsultorio: TextInputLayout
    private lateinit var tilLatitud: TextInputLayout
    private lateinit var tilLongitud: TextInputLayout

    private lateinit var etNombre: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etEspecialidad: TextInputEditText
    private lateinit var etLicencia: TextInputEditText
    private lateinit var etConsultorio: TextInputEditText
    private lateinit var etLatitud: TextInputEditText
    private lateinit var etLongitud: TextInputEditText
    private lateinit var btnGuardar: MaterialButton

    private val mapPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lat = result.data?.getDoubleExtra("SELECTED_LAT", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("SELECTED_LNG", 0.0) ?: 0.0
            etLatitud.setText(lat.toString())
            etLongitud.setText(lng.toString())
            Toast.makeText(this, "Coordenadas seleccionadas desde el mapa", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_veterinario)
        configurarCabeceraVerde(findViewById(R.id.main), findViewById(R.id.contenidoCabecera))

        tilNombre = findViewById(R.id.tilNombreVet)
        tilEmail = findViewById(R.id.tilEmailVet)
        tilPassword = findViewById(R.id.tilPasswordVet)
        tilTelefono = findViewById(R.id.tilTelefonoVet)
        tilEspecialidad = findViewById(R.id.tilEspecialidadVet)
        tilLicencia = findViewById(R.id.tilLicenciaVet)
        tilConsultorio = findViewById(R.id.tilConsultorioVet)
        tilLatitud = findViewById(R.id.tilLatitudVet)
        tilLongitud = findViewById(R.id.tilLongitudVet)

        etNombre = findViewById(R.id.etNombreVet)
        etEmail = findViewById(R.id.etEmailVet)
        etPassword = findViewById(R.id.etPasswordVet)
        etTelefono = findViewById(R.id.etTelefonoVet)
        etEspecialidad = findViewById(R.id.etEspecialidadVet)
        etLicencia = findViewById(R.id.etLicenciaVet)
        etConsultorio = findViewById(R.id.etConsultorioVet)
        etLatitud = findViewById(R.id.etLatitudVet)
        etLongitud = findViewById(R.id.etLongitudVet)
        btnGuardar = findViewById(R.id.btnGuardarVet)

        // Valores por defecto sugeridos para probar Sucre, Bolivia si están vacíos
        etLatitud.setText("-19.03332")
        etLongitud.setText("-65.26274")

        findViewById<MaterialButton>(R.id.btnElegirEnMapa).setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java).apply {
                putExtra("PICKER_MODE", true)
            }
            mapPickerLauncher.launch(intent)
        }

        btnGuardar.setOnClickListener { registrarVeterinario() }
    }

    private fun registrarVeterinario() {
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val telefono = etTelefono.text.toString().trim()
        val especialidad = etEspecialidad.text.toString().trim()
        val licencia = etLicencia.text.toString().trim()
        val consultorio = etConsultorio.text.toString().trim()
        val lat = etLatitud.text.toString().toDoubleOrNull() ?: 0.0
        val lng = etLongitud.text.toString().toDoubleOrNull() ?: 0.0

        if (nombre.isEmpty()) {
            tilNombre.error = "Ingresa el nombre completo"
            return
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Ingresa un email válido"
            return
        }
        if (password.length < 4) {
            tilPassword.error = "Mínimo 4 caracteres"
            return
        }
        if (consultorio.isEmpty()) {
            tilConsultorio.error = "Ingresa el nombre del consultorio"
            return
        }

        btnGuardar.isEnabled = false

        lifecycleScope.launch {
            val db = VeterinariaDatabase.getInstance(this@RegistrarVeterinarioActivity)
            
            if (db.usuarioDao().existeEmail(email)) {
                tilEmail.error = "Este correo ya está registrado"
                btnGuardar.isEnabled = true
                return@launch
            }

            val nuevoVet = Usuario(
                nombre = nombre,
                email = email,
                password = password,
                telefono = telefono,
                rol = Usuario.ROL_VETERINARIO,
                especialidad = especialidad.ifEmpty { "General" },
                licencia = licencia.ifEmpty { "REG-VET" },
                consultorio = consultorio,
                latitud = lat,
                longitud = lng
            )

            val idGenerado = db.usuarioDao().insertar(nuevoVet).toInt()

            Toast.makeText(
                this@RegistrarVeterinarioActivity,
                "Veterinario registrado: ya puede iniciar sesión con su email (id $idGenerado)",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }
}
