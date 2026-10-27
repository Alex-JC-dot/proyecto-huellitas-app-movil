package com.AlexYChristian.ProyectoSis145.ui.citas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.AlexYChristian.ProyectoSis145.R

/**
 * Pantalla: Mis citas
 * Responsable: Christian
 *
 * Por hacer:
 *  - RecyclerView con db.citaDao().obtenerPorUsuario(session.obtenerUsuarioId())
 *  - Cada cita: mascota, servicio, fecha, hora y estado
 *  - Botón flotante (+) para abrir AgendarCitaActivity
 *  - Opción para cancelar: db.citaDao().actualizarEstado(id, Cita.ESTADO_CANCELADA)
 */
class CitasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_citas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
