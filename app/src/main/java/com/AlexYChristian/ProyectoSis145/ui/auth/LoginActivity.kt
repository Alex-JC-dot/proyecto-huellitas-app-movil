package com.AlexYChristian.ProyectoSis145.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.ui.menu.MenuActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * Pantalla de Login.
 * 1. El usuario escribe email y contraseña.
 * 2. Se validan los campos (que no estén vacíos y que el email tenga formato correcto).
 * 3. Se busca el usuario en la base de datos (Room) con UsuarioDao.login().
 * 4. Si existe, se guarda la sesión (SharedPreferences) y se abre el Menú.
 */
class LoginActivity : AppCompatActivity() {

    // Vistas del layout (se inicializan en onCreate con findViewById)
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnIngresar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Conectar las variables con los elementos del XML (por su id)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnIngresar = findViewById(R.id.btnIngresar)

        btnIngresar.setOnClickListener { iniciarSesion() }

        findViewById<TextView>(R.id.tvIrRegistro).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun iniciarSesion() {
        val email = etEmail.text.toString().trim()   // trim() quita espacios al inicio y al final
        val password = etPassword.text.toString()

        // Limpiar errores anteriores
        tilEmail.error = null
        tilPassword.error = null

        // --- Validaciones ---
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

        // --- Consulta a la base de datos ---
        // Se desactiva el botón para evitar que lo presionen dos veces mientras se busca.
        btnIngresar.isEnabled = false

        // lifecycleScope.launch ejecuta la consulta sin congelar la pantalla (corrutina).
        lifecycleScope.launch {
            val db = VeterinariaDatabase.getInstance(this@LoginActivity)
            val usuario = db.usuarioDao().login(email, password)

            btnIngresar.isEnabled = true

            if (usuario != null) {
                // Guardar quién inició sesión
                SessionManager(this@LoginActivity).iniciarSesion(usuario.id)

                // Abrir el menú y borrar el login del historial,
                // para que el botón "atrás" no vuelva al login.
                val intent = Intent(this@LoginActivity, MenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                tilPassword.error = getString(R.string.error_credenciales)
            }
        }
    }
}
