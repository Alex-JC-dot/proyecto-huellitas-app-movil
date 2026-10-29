package com.AlexYChristian.ProyectoSis145.ui.menu

import android.content.Intent
import android.view.View
import android.os.Bundle
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
import com.AlexYChristian.ProyectoSis145.ui.auth.LoginActivity
import com.AlexYChristian.ProyectoSis145.ui.citas.AgendarCitaActivity
import com.AlexYChristian.ProyectoSis145.ui.citas.CitasActivity
import com.AlexYChristian.ProyectoSis145.ui.mascotas.MascotasActivity
import com.AlexYChristian.ProyectoSis145.ui.servicios.ServiciosActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Menú principal de Huellitas.
 * 1. Revisa que haya una sesión iniciada (si no, vuelve al login).
 * 2. Busca al usuario en Room y lo saluda por su nombre.
 * 3. Cada botón abre su pantalla: Mis mascotas, Mis citas, Agendar cita y Servicios.
 * 4. "Cerrar sesión" borra la sesión (SharedPreferences) y vuelve al login.
 */
class MenuActivity : AppCompatActivity() {

    private lateinit var session: SessionManager
    private lateinit var tvSaludo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        // La cabecera es verde oscuro: la hora y la batería se muestran en blanco
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        // La cabecera verde llega hasta arriba; su contenido baja lo justo
        // para no quedar debajo de la hora y la batería.
        val contenidoCabecera = findViewById<View>(R.id.contenidoCabecera)
        val paddingTopOriginal = contenidoCabecera.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            contenidoCabecera.setPadding(
                contenidoCabecera.paddingLeft,
                paddingTopOriginal + systemBars.top,
                contenidoCabecera.paddingRight,
                contenidoCabecera.paddingBottom
            )
            insets
        }

        session = SessionManager(this)
        tvSaludo = findViewById(R.id.tvSaludo)

        // Si por algún motivo no hay sesión, no se puede estar en el menú
        if (!session.haySesionIniciada()) {
            irAlLogin()
            return
        }

        mostrarSaludo()

        // Navegación: cada botón abre su pantalla
        findViewById<MaterialButton>(R.id.btnMisMascotas).setOnClickListener {
            startActivity(Intent(this, MascotasActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnMisCitas).setOnClickListener {
            startActivity(Intent(this, CitasActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnAgendarCita).setOnClickListener {
            startActivity(Intent(this, AgendarCitaActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnServicios).setOnClickListener {
            startActivity(Intent(this, ServiciosActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnCerrarSesion).setOnClickListener {
            session.cerrarSesion()
            irAlLogin()
        }
    }

    /** Busca al usuario en la base de datos y muestra "¡Hola, <primer nombre>!" */
    private fun mostrarSaludo() {
        lifecycleScope.launch {
            val usuario = VeterinariaDatabase.getInstance(this@MenuActivity)
                .usuarioDao()
                .obtenerPorId(session.obtenerUsuarioId())

            if (usuario == null) {
                // El usuario ya no existe (por ejemplo, se borraron los datos)
                session.cerrarSesion()
                irAlLogin()
                return@launch
            }

            // Solo el primer nombre: "Juan Pérez" → "Juan"
            val primerNombre = usuario.nombre.trim().split(" ").first()
            tvSaludo.text = getString(R.string.menu_saludo, primerNombre)
        }
    }

    /** Abre el login y borra el historial, para que "atrás" no regrese al menú. */
    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
