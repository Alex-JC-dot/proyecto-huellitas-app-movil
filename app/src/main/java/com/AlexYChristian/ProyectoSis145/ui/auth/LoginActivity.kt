package com.AlexYChristian.ProyectoSis145.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.AlexYChristian.ProyectoSis145.ui.admin.AdminVeterinariosActivity
import com.AlexYChristian.ProyectoSis145.ui.menu.MenuActivity
import com.AlexYChristian.ProyectoSis145.ui.vet.VetPanelActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * Pantalla de Login con soporte para Roles (Super Admin, Veterinario, Cliente).
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var cbRecordarme: MaterialCheckBox
    private lateinit var btnIngresar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbRecordarme = findViewById(R.id.cbRecordarme)
        btnIngresar = findViewById(R.id.btnIngresar)

        btnIngresar.setOnClickListener { iniciarSesion() }

        findViewById<TextView>(R.id.tvIrRegistro).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun iniciarSesion() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        tilEmail.error = null
        tilPassword.error = null

        if (email.isEmpty()) {
            tilEmail.error = getString(R.string.error_email_vacio)
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = getString(R.string.error_email_invalido)
            return
        }
        if (password.isEmpty()) {
            tilPassword.error = getString(R.string.error_password_vacia)
            return
        }

        btnIngresar.isEnabled = false

        lifecycleScope.launch {
            val db = VeterinariaDatabase.getInstance(this@LoginActivity)
            val usuario = db.usuarioDao().login(email, password)

            btnIngresar.isEnabled = true

            if (usuario != null) {
                // Guardar la sesión con id y rol
                SessionManager(this@LoginActivity).iniciarSesion(
                    usuarioId = usuario.id,
                    rol = usuario.rol,
                    recordar = cbRecordarme.isChecked
                )

                // Redirigir según el ROL
                val destino = when (usuario.rol) {
                    Usuario.ROL_SUPER_ADMIN -> AdminVeterinariosActivity::class.java
                    Usuario.ROL_VETERINARIO -> VetPanelActivity::class.java
                    else -> MenuActivity::class.java
                }

                val intent = Intent(this@LoginActivity, destino)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                tilPassword.error = getString(R.string.error_credenciales)
            }
        }
    }
}
