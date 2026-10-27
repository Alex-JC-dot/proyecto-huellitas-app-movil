package com.AlexYChristian.ProyectoSis145.ui.mascotas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.AlexYChristian.ProyectoSis145.R

/**
 * Pantalla: Mis mascotas
 * Responsable: Christian
 *
 * Por hacer:
 *  - RecyclerView con db.mascotaDao().obtenerPorUsuario(session.obtenerUsuarioId())
 *  - Al tocar una mascota: abrir DetalleMascotaActivity con putExtra(DetalleMascotaActivity.EXTRA_MASCOTA_ID, mascota.id)
 *  - Botón flotante (+) para abrir RegistrarMascotaActivity
 *  - Recargar la lista en onResume()
 */
class MascotasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mascotas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
