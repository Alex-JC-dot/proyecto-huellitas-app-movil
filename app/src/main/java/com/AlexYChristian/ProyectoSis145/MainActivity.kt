package com.AlexYChristian.ProyectoSis145

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.ui.auth.LoginActivity
import com.AlexYChristian.ProyectoSis145.ui.menu.MenuActivity

/**
 * Punto de entrada de la app. No tiene diseño: solo revisa si hay una sesión
 * guardada con "Recordarme" (SharedPreferences) y abre el Menú o el Login según corresponda.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)
        val destino = if (session.haySesionRecordada()) {
            MenuActivity::class.java
        } else {
            session.cerrarSesion()   // si no marcó "Recordarme", se pide login otra vez
            LoginActivity::class.java
        }
        startActivity(Intent(this, destino))
        finish()
    }
}
