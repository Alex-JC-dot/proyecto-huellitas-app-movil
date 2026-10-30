package com.AlexYChristian.ProyectoSis145.ui.mascotas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.HistorialMedico
import com.AlexYChristian.ProyectoSis145.util.CitaUtils
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class HistorialMedicoActivity : AppCompatActivity() {

    private lateinit var rvHistorial: RecyclerView
    private lateinit var layoutVacioHistorial: LinearLayout
    private lateinit var tvTituloMascotaHistorial: TextView

    private lateinit var adapter: HistorialMedicoAdapter
    private var mascotaId: Int = -1
    private var mascotaNombre: String = "Mascota"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial_medico)
        configurarCabeceraVerde(findViewById(R.id.main), findViewById(R.id.contenidoCabecera))

        mascotaId = intent.getIntExtra("MASCOTA_ID", -1)
        mascotaNombre = intent.getStringExtra("MASCOTA_NOMBRE") ?: "Mascota"

        tvTituloMascotaHistorial = findViewById(R.id.tvTituloMascotaHistorial)
        rvHistorial = findViewById(R.id.rvHistorialMedico)
        layoutVacioHistorial = findViewById(R.id.layoutVacioHistorial)

        tvTituloMascotaHistorial.text = "Historial de $mascotaNombre"

        adapter = HistorialMedicoAdapter()
        rvHistorial.layoutManager = LinearLayoutManager(this)
        rvHistorial.adapter = adapter

        findViewById<ExtendedFloatingActionButton>(R.id.fabAgregarHistorial).setOnClickListener {
            mostrarDialogoNuevoRegistro()
        }

        cargarHistorial()
    }

    private fun cargarHistorial() {
        if (mascotaId == -1) return
        lifecycleScope.launch {
            val lista = VeterinariaDatabase.getInstance(this@HistorialMedicoActivity)
                .historialMedicoDao()
                .obtenerPorMascota(mascotaId)

            adapter.actualizar(lista)
            rvHistorial.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
            layoutVacioHistorial.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun mostrarDialogoNuevoRegistro() {
        val vistaDialogo = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_historial, null)
        val etDiagnostico = vistaDialogo.findViewById<TextInputEditText>(R.id.etDiagHistorial)
        val etTratamiento = vistaDialogo.findViewById<TextInputEditText>(R.id.etTratHistorial)
        val etPeso = vistaDialogo.findViewById<TextInputEditText>(R.id.etPesoHistorial)
        val etNotas = vistaDialogo.findViewById<TextInputEditText>(R.id.etNotasHistorial)

        MaterialAlertDialogBuilder(this)
            .setTitle("🩺 Nueva Consulta / Ficha Médica")
            .setView(vistaDialogo)
            .setPositiveButton("Guardar Ficha") { _, _ ->
                val diag = etDiagnostico.text.toString().trim()
                val trat = etTratamiento.text.toString().trim()
                val peso = etPeso.text.toString().toDoubleOrNull() ?: 0.0
                val notas = etNotas.text.toString().trim()

                if (diag.isEmpty()) {
                    Toast.makeText(this, "Ingresa el diagnóstico", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val vetId = SessionManager(this).obtenerUsuarioId()
                val fechaHoy = CitaUtils.hoyBD()

                lifecycleScope.launch {
                    val nuevoRegistro = HistorialMedico(
                        mascotaId = mascotaId,
                        veterinarioId = vetId,
                        fecha = fechaHoy,
                        diagnostico = diag,
                        tratamiento = trat.ifEmpty { "Consulta de control" },
                        peso = peso,
                        notas = notas
                    )

                    VeterinariaDatabase.getInstance(this@HistorialMedicoActivity)
                        .historialMedicoDao()
                        .insertar(nuevoRegistro)

                    Toast.makeText(this@HistorialMedicoActivity, "Ficha médica guardada offline", Toast.LENGTH_SHORT).show()
                    cargarHistorial()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
