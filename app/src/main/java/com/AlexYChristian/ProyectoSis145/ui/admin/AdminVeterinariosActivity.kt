package com.AlexYChristian.ProyectoSis145.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.ui.auth.LoginActivity
import com.AlexYChristian.ProyectoSis145.ui.maps.MapsActivity
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

class AdminVeterinariosActivity : AppCompatActivity() {

    private lateinit var rvVeterinarios: RecyclerView
    private lateinit var layoutVacioVets: LinearLayout
    private lateinit var adapter: VeterinarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_veterinarios)
        configurarCabeceraVerde(findViewById(R.id.main), findViewById(R.id.contenidoCabecera))

        rvVeterinarios = findViewById(R.id.rvVeterinarios)
        layoutVacioVets = findViewById(R.id.layoutVacioVets)

        adapter = VeterinarioAdapter { vet ->
            val intent = Intent(this, MapsActivity::class.java).apply {
                putExtra("VET_LAT", vet.latitud)
                putExtra("VET_LNG", vet.longitud)
                putExtra("VET_NOMBRE", vet.nombre)
                putExtra("VET_CONSULTORIO", vet.consultorio)
            }
            startActivity(intent)
        }

        rvVeterinarios.layoutManager = LinearLayoutManager(this)
        rvVeterinarios.adapter = adapter

        findViewById<ExtendedFloatingActionButton>(R.id.fabAgregarVet).setOnClickListener {
            startActivity(Intent(this, RegistrarVeterinarioActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnCerrarSesionAdmin).setOnClickListener {
            SessionManager(this).cerrarSesion()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarVeterinarios()
    }

    private fun cargarVeterinarios() {
        lifecycleScope.launch {
            val vets = VeterinariaDatabase.getInstance(this@AdminVeterinariosActivity)
                .usuarioDao()
                .obtenerVeterinarios()

            adapter.actualizar(vets)
            rvVeterinarios.visibility = if (vets.isEmpty()) View.GONE else View.VISIBLE
            layoutVacioVets.visibility = if (vets.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
