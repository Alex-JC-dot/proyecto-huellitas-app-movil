package com.AlexYChristian.ProyectoSis145

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.ui.auth.LoginActivity
import com.AlexYChristian.ProyectoSis145.ui.menu.MenuActivity

/**
 * Punto de entrada de la app. No tiene diseño: solo revisa si hay una sesión
 * guardada (SharedPreferences) y abre el Menú o el Login según corresponda.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destino = if (SessionManager(this).haySesionIniciada()) {
            MenuActivity::class.java
        } else {
            LoginActivity::class.java
        }
        startActivity(Intent(this, destino))
        finish()
    }
}
