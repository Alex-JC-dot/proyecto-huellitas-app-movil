package com.AlexYChristian.ProyectoSis145.ui.citas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.AlexYChristian.ProyectoSis145.R

/**
 * Pantalla: Agendar cita
 * Responsable: Christian
 *
 * Por hacer:
 *  - Elegir mascota (Spinner con db.mascotaDao().obtenerPorUsuario(...))
 *  - Elegir servicio (Spinner con db.servicioDao().obtenerTodos())
 *  - Elegir fecha y hora (DatePickerDialog / TimePickerDialog)
 *  - Guardar con db.citaDao().insertar(cita)
 */
class AgendarCitaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agendar_cita)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
