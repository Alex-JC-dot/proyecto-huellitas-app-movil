package com.AlexYChristian.ProyectoSis145.ui.menu

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.AlexYChristian.ProyectoSis145.R

/**
 * Pantalla: Menú principal
 * Responsable: Alex
 *
 * Por hacer:
 *  - Saludo con el nombre del usuario (db.usuarioDao().obtenerPorId(session.obtenerUsuarioId()))
 *  - Botones/tarjetas hacia: MascotasActivity, CitasActivity, ServiciosActivity
 *  - Botón de cerrar sesión: session.cerrarSesion() y volver a LoginActivity
 */
class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
