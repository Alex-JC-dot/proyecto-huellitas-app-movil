package com.AlexYChristian.ProyectoSis145.ui.mascotas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.AlexYChristian.ProyectoSis145.R

/**
 * Pantalla: Detalle de mascota
 * Responsable: Christian
 *
 * Por hacer:
 *  - Leer el id: intent.getIntExtra(EXTRA_MASCOTA_ID, -1)
 *  - Mostrar datos con db.mascotaDao().obtenerPorId(id)
 *  - Historial de vacunas con db.vacunaDao().obtenerPorMascota(id)
 *  - Botón para agendar cita (AgendarCitaActivity)
 */
class DetalleMascotaActivity : AppCompatActivity() {

    companion object {
        /** Clave para enviar el id de la mascota entre pantallas. */
        const val EXTRA_MASCOTA_ID = "mascota_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_mascota)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
