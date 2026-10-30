package com.AlexYChristian.ProyectoSis145

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.AlexYChristian.ProyectoSis145.ui.admin.AdminVeterinariosActivity
import com.AlexYChristian.ProyectoSis145.ui.auth.LoginActivity
import com.AlexYChristian.ProyectoSis145.ui.menu.MenuActivity
import com.AlexYChristian.ProyectoSis145.ui.vet.VetPanelActivity

/**
 * Punto de entrada de la app. Revisa la sesión y direcciona a la pantalla
 * correspondiente según el ROL (Super Admin, Veterinario o Cliente).
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)
        if (session.haySesionRecordada()) {
            val destino = when (session.obtenerRol()) {
                Usuario.ROL_SUPER_ADMIN -> AdminVeterinariosActivity::class.java
                Usuario.ROL_VETERINARIO -> VetPanelActivity::class.java
                else -> MenuActivity::class.java
            }
            startActivity(Intent(this, destino))
        } else {
            session.cerrarSesion()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
