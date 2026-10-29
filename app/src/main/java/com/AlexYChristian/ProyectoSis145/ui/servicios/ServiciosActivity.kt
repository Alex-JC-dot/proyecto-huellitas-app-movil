package com.AlexYChristian.ProyectoSis145.ui.servicios

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.ui.citas.AgendarCitaActivity
import com.AlexYChristian.ProyectoSis145.util.CitaUtils
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Servicios de la veterinaria (los 5 que se precargan al crear la base de datos).
 * Como son pocos, en vez de un RecyclerView se crea una tarjeta por servicio
 * dentro de un LinearLayout (inflando item_servicio.xml).
 */
class ServiciosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_servicios)
        configurarCabeceraVerde(findViewById(R.id.main), findViewById(R.id.contenidoCabecera))

        findViewById<MaterialButton>(R.id.btnAgendar).setOnClickListener {
            startActivity(Intent(this, AgendarCitaActivity::class.java))
        }

        val contenedor = findViewById<LinearLayout>(R.id.layoutServicios)
        lifecycleScope.launch {
            val servicios = VeterinariaDatabase.getInstance(this@ServiciosActivity).servicioDao().obtenerTodos()
            for (servicio in servicios) {
                val vista = LayoutInflater.from(this@ServiciosActivity)
                    .inflate(R.layout.item_servicio, contenedor, false)
                CitaUtils.llenarServicio(vista, servicio)
                contenedor.addView(vista)
            }
        }
    }
}
