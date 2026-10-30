package com.AlexYChristian.ProyectoSis145.ui.mascotas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import com.AlexYChristian.ProyectoSis145.data.entity.Vacuna
import com.AlexYChristian.ProyectoSis145.ui.citas.AgendarCitaActivity
import com.AlexYChristian.ProyectoSis145.util.FotoUtils
import com.AlexYChristian.ProyectoSis145.util.MascotaUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Detalle de mascota (su "ficha").
 * 1. Recibe el id de la mascota por el Intent (EXTRA_MASCOTA_ID).
 * 2. Busca la mascota y sus vacunas en Room y las muestra.
 * 3. "Agendar cita" abre AgendarCitaActivity con esta mascota.
 * 4. "Eliminar" pide confirmación, borra la mascota (y por CASCADE sus citas y vacunas)
 *    y también el archivo de su foto.
 */
class DetalleMascotaActivity : AppCompatActivity() {

    companion object {
        /** Clave para enviar el id de la mascota entre pantallas. */
        const val EXTRA_MASCOTA_ID = "mascota_id"
    }

    private var mascota: Mascota? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_mascota)

        // La foto llega hasta arriba, detrás de la hora: íconos en blanco
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, 0, barras.right, barras.bottom)
            insets
        }

        val mascotaId = intent.getIntExtra(EXTRA_MASCOTA_ID, -1)
        if (mascotaId == -1) {
            finish()   // se abrió sin decir qué mascota mostrar
            return
        }

        findViewById<MaterialButton>(R.id.btnVerHistorialMedico).setOnClickListener {
            val intent = Intent(this, HistorialMedicoActivity::class.java).apply {
                putExtra("MASCOTA_ID", mascotaId)
                putExtra("MASCOTA_NOMBRE", mascota?.nombre ?: "Mascota")
            }
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.btnAgendarCita).setOnClickListener {
            val intent = Intent(this, AgendarCitaActivity::class.java)
            intent.putExtra(EXTRA_MASCOTA_ID, mascotaId)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.btnEliminar).setOnClickListener { confirmarEliminacion() }

        cargarMascota(mascotaId)
    }

    private fun cargarMascota(mascotaId: Int) {
        lifecycleScope.launch {
            val db = VeterinariaDatabase.getInstance(this@DetalleMascotaActivity)
            val encontrada = db.mascotaDao().obtenerPorId(mascotaId)
            if (encontrada == null) {
                finish()
                return@launch
            }
            mascota = encontrada
            mostrarDatos(encontrada)
            mostrarVacunas(db.vacunaDao().obtenerPorMascota(mascotaId))
        }
    }

    private fun mostrarDatos(m: Mascota) {
        FotoUtils.mostrar(findViewById(R.id.ivFoto), findViewById(R.id.ivSilueta), m)

        findViewById<TextView>(R.id.tvNombre).text = m.nombre
        findViewById<TextView>(R.id.tvSexoSimbolo).text = MascotaUtils.simboloSexo(m.sexo)

        // Píldoras
        findViewById<TextView>(R.id.tvEdad).text = MascotaUtils.calcularEdad(m.fechaNacimiento)
        findViewById<TextView>(R.id.tvRaza).text = m.raza
        findViewById<TextView>(R.id.tvPeso).text = MascotaUtils.pesoParaMostrar(m.peso)

        // Información
        findViewById<TextView>(R.id.tvEspecie).text = "${MascotaUtils.emoji(m.especie)} ${m.especie}"
        findViewById<TextView>(R.id.tvSexo).text = "${MascotaUtils.simboloSexo(m.sexo)} ${m.sexo}"
        findViewById<TextView>(R.id.tvNacimiento).text = MascotaUtils.fechaParaMostrar(m.fechaNacimiento)
        findViewById<TextView>(R.id.tvPesoInfo).text = MascotaUtils.pesoParaMostrar(m.peso)
    }

    /** Crea una fila de texto por cada vacuna dentro de layoutVacunas. */
    private fun mostrarVacunas(vacunas: List<Vacuna>) {
        val contenedor = findViewById<LinearLayout>(R.id.layoutVacunas)
        contenedor.removeAllViews()
        findViewById<TextView>(R.id.tvSinVacunas).visibility =
            if (vacunas.isEmpty()) View.VISIBLE else View.GONE

        for (vacuna in vacunas) {
            val fila = TextView(this).apply {
                var texto = "💉 ${vacuna.nombre} — ${MascotaUtils.fechaParaMostrar(vacuna.fechaAplicacion)}"
                if (vacuna.proximaDosis.isNotEmpty()) {
                    texto += "\n     " + getString(
                        R.string.detalle_vacuna_proxima,
                        MascotaUtils.fechaParaMostrar(vacuna.proximaDosis)
                    )
                }
                text = texto
                textSize = 15f
                setPadding(0, 12, 0, 0)
            }
            contenedor.addView(fila)
        }
    }

    private fun confirmarEliminacion() {
        val m = mascota ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.detalle_eliminar_titulo, m.nombre))
            .setMessage(R.string.detalle_eliminar_mensaje)
            .setNegativeButton(R.string.detalle_eliminar_cancelar, null)
            .setPositiveButton(R.string.detalle_eliminar_confirmar) { _, _ -> eliminar(m) }
            .show()
    }

    private fun eliminar(m: Mascota) {
        lifecycleScope.launch {
            VeterinariaDatabase.getInstance(this@DetalleMascotaActivity).mascotaDao().eliminar(m)
            FotoUtils.borrarFoto(m.foto)
            Toast.makeText(this@DetalleMascotaActivity, R.string.detalle_eliminada, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
