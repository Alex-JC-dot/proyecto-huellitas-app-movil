package com.AlexYChristian.ProyectoSis145.ui.mascotas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.AlexYChristian.ProyectoSis145.R

/**
 * Pantalla: Registrar mascota
 * Responsable: Christian
 *
 * Por hacer:
 *  - Formulario: nombre, especie, raza, sexo, fecha de nacimiento y peso
 *  - Guardar con db.mascotaDao().insertar(mascota) usando usuarioId = session.obtenerUsuarioId()
 *  - Al guardar: finish() para volver a la lista
 */
class RegistrarMascotaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_mascota)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
