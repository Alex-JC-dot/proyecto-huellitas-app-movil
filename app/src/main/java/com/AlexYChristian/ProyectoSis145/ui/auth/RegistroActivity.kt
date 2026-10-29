package com.AlexYChristian.ProyectoSis145.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * Pantalla: Crear cuenta
 * Responsable: Alex
 *
 * Por hacer:
 *  - Formulario: nombre, email, teléfono y contraseña
 *  - Revisar que el email no exista con db.usuarioDao().existeEmail(email)
 *  - Guardar con db.usuarioDao().insertar(usuario) y volver al login
 */
class RegistroActivity : AppCompatActivity() {
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilTelefono: TextInputLayout
    private lateinit var tilContrasena: TextInputLayout
    private lateinit var tilConfirmarContrasena: TextInputLayout


    private lateinit var etNombre: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etConfirmarContrasena: TextInputEditText

    private lateinit var btnCrearCuenta: MaterialButton
    private lateinit var tvIrLogin: TextView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tilNombre = findViewById(R.id.tilNombre)
        tilEmail = findViewById(R.id.tilEmail)
        tilTelefono = findViewById(R.id.tilTelefono)
        tilContrasena = findViewById(R.id.tilContrasena)
        tilConfirmarContrasena = findViewById(R.id.tilConfirmarContrasena)

        etNombre = findViewById(R.id.etNombre)
        etEmail = findViewById(R.id.etEmail)
        etTelefono = findViewById(R.id.etTelefono)
        etContrasena = findViewById(R.id.etContrasena)
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena)
        btnCrearCuenta = findViewById(R.id.btnCrearCuenta)
        tvIrLogin = findViewById(R.id.tvIrLogin)

        btnCrearCuenta.setOnClickListener { crearCuenta() }
        tvIrLogin.setOnClickListener { finish() }
    }

    private fun crearCuenta() {
        tilNombre.error = null
        tilEmail.error = null
        tilTelefono.error = null
        tilContrasena.error = null
        tilConfirmarContrasena.error = null

        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val contrasena = etContrasena.text.toString()
        val confirmarContrasena = etConfirmarContrasena.text.toString()


        if (nombre.isEmpty()) {
            tilNombre.error = getString(R.string.error_nombre_vacio)
            return
        }

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.error_email_vacio)
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = getString(R.string.error_email_invalido)
            return
        }

        if (telefono.length < 8) {
            tilTelefono.error = getString(R.string.error_telefono_invalido)
            return
        }

        if (contrasena.length < 6) {
            tilContrasena.error = getString(R.string.error_password_corta)
            return
        }

        if (contrasena != confirmarContrasena) {
            tilConfirmarContrasena.error = getString(R.string.error_passwords_no_coinciden)
            return
        }
        // guradar al usuario en la base de datos

        btnCrearCuenta.isEnabled = false // para que no lo toquen dos veces

        lifecycleScope.launch {
            val usuarioDao = VeterinariaDatabase.getInstance(this@RegistroActivity).usuarioDao()


            if (usuarioDao.existeEmail(email)) {
                tilEmail.error = getString(R.string.error_email_existente)
                btnCrearCuenta.isEnabled = true
                return@launch
            }


            val usuario = Usuario(
                nombre = nombre,
                email = email,
                telefono = telefono,
                password = contrasena
            )
            usuarioDao.insertar(usuario)

            // Avisar y volver al login
            Toast.makeText(this@RegistroActivity, R.string.registro_exito, Toast.LENGTH_LONG).show()
            finish()
        }

    }

}
