package com.AlexYChristian.ProyectoSis145.ui.mascotas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.SessionManager
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import com.AlexYChristian.ProyectoSis145.util.MascotaUtils
import com.AlexYChristian.ProyectoSis145.util.configurarCabeceraVerde
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

/**
 * Mis mascotas: lista en 2 columnas de las mascotas del usuario.
 * 1. Lee las mascotas del usuario con MascotaDao.obtenerPorUsuario().
 * 2. Los chips filtran por especie (sin volver a consultar la base de datos).
 * 3. Al tocar una mascota se abre su detalle; el botón flotante abre el formulario.
 * 4. En onResume se recarga la lista (por si se registró o eliminó una mascota).
 */
class MascotasActivity : AppCompatActivity() {

    private lateinit var adapter: MascotaAdapter
    private lateinit var rvMascotas: RecyclerView
    private lateinit var layoutVacio: LinearLayout
    private lateinit var tvVacioTitulo: TextView
    private lateinit var tvVacioTexto: TextView
    private lateinit var chipsFiltro: ChipGroup

    // Todas las mascotas del usuario; el filtro trabaja sobre esta lista
    private var todasLasMascotas: List<Mascota> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mascotas)
        configurarCabeceraVerde(findViewById(R.id.main), findViewById(R.id.contenidoCabecera))

        rvMascotas = findViewById(R.id.rvMascotas)
        layoutVacio = findViewById(R.id.layoutVacio)
        tvVacioTitulo = findViewById(R.id.tvVacioTitulo)
        tvVacioTexto = findViewById(R.id.tvVacioTexto)
        chipsFiltro = findViewById(R.id.chipsFiltro)

        // RecyclerView: cuadrícula de 2 columnas + adapter
        adapter = MascotaAdapter { mascota -> abrirDetalle(mascota) }
        rvMascotas.layoutManager = GridLayoutManager(this, 2)
        rvMascotas.adapter = adapter

        // Al cambiar el chip elegido, se vuelve a filtrar
        chipsFiltro.setOnCheckedStateChangeListener { _, _ -> aplicarFiltro() }

        findViewById<ExtendedFloatingActionButton>(R.id.fabRegistrar).setOnClickListener {
            startActivity(Intent(this, RegistrarMascotaActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarMascotas()
    }

    private fun cargarMascotas() {
        val usuarioId = SessionManager(this).obtenerUsuarioId()
        lifecycleScope.launch {
            todasLasMascotas = VeterinariaDatabase.getInstance(this@MascotasActivity)
                .mascotaDao()
                .obtenerPorUsuario(usuarioId)
            aplicarFiltro()
        }
    }

    /** Muestra solo las mascotas de la especie elegida en los chips. */
    private fun aplicarFiltro() {
        val especie = when (chipsFiltro.checkedChipId) {
            R.id.chipPerros -> MascotaUtils.ESPECIE_PERRO
            R.id.chipGatos -> MascotaUtils.ESPECIE_GATO
            R.id.chipAves -> MascotaUtils.ESPECIE_AVE
            R.id.chipOtros -> MascotaUtils.ESPECIE_OTRO
            else -> null   // "Todas"
        }
        val filtradas = if (especie == null) todasLasMascotas
        else todasLasMascotas.filter { it.especie == especie }

        adapter.actualizar(filtradas)

        // Si no hay nada que mostrar, se muestra el mensaje en lugar de la lista
        val vacia = filtradas.isEmpty()
        rvMascotas.visibility = if (vacia) View.GONE else View.VISIBLE
        layoutVacio.visibility = if (vacia) View.VISIBLE else View.GONE
        if (todasLasMascotas.isEmpty()) {
            tvVacioTitulo.setText(R.string.mascotas_vacio_titulo)
            tvVacioTexto.visibility = View.VISIBLE
        } else {
            tvVacioTitulo.setText(R.string.mascotas_vacio_filtro)
            tvVacioTexto.visibility = View.GONE
        }
    }

    private fun abrirDetalle(mascota: Mascota) {
        val intent = Intent(this, DetalleMascotaActivity::class.java)
        intent.putExtra(DetalleMascotaActivity.EXTRA_MASCOTA_ID, mascota.id)
        startActivity(intent)
    }
}
