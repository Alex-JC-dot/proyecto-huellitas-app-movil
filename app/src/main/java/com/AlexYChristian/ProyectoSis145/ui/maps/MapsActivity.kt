package com.AlexYChristian.ProyectoSis145.ui.maps

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.VeterinariaDatabase
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.AlexYChristian.ProyectoSis145.ui.citas.AgendarCitaActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var isPickerMode: Boolean = false
    private var selectedLatLng: LatLng? = null

    private lateinit var cardDetalleConsultorio: MaterialCardView
    private lateinit var tvNombreConsultorio: TextView
    private lateinit var tvEspecialidadMapa: TextView
    private lateinit var tvDistanciaMapa: TextView
    private lateinit var btnTrazarRuta: MaterialButton
    private lateinit var btnAgendarConVet: MaterialButton
    private lateinit var btnConfirmarPicker: MaterialButton

    private var selectedVet: Usuario? = null
    private val defaultCenter = LatLng(-19.03332, -65.26274) // Sucre, Bolivia centro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_maps)

        isPickerMode = intent.getBooleanExtra("PICKER_MODE", false)

        cardDetalleConsultorio = findViewById(R.id.cardDetalleConsultorio)
        tvNombreConsultorio = findViewById(R.id.tvNombreConsultorio)
        tvEspecialidadMapa = findViewById(R.id.tvEspecialidadMapa)
        tvDistanciaMapa = findViewById(R.id.tvDistanciaMapa)
        btnTrazarRuta = findViewById(R.id.btnTrazarRuta)
        btnAgendarConVet = findViewById(R.id.btnAgendarConVet)
        btnConfirmarPicker = findViewById(R.id.btnConfirmarPicker)

        findViewById<MaterialButton>(R.id.btnVolverMapa).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnCerrarFicha).setOnClickListener {
            cardDetalleConsultorio.visibility = View.GONE
        }

        if (isPickerMode) {
            findViewById<TextView>(R.id.tvTituloMapa).text = "Seleccionar Ubicación"
            findViewById<TextView>(R.id.tvSubtituloMapa).text = "Toca el mapa para definir las coordenadas del consultorio"
            btnConfirmarPicker.visibility = View.VISIBLE
            btnConfirmarPicker.setOnClickListener {
                selectedLatLng?.let { latLng ->
                    val data = Intent().apply {
                        putExtra("SELECTED_LAT", latLng.latitude)
                        putExtra("SELECTED_LNG", latLng.longitude)
                    }
                    setResult(Activity.RESULT_OK, data)
                    finish()
                } ?: run {
                    Toast.makeText(this, "Por favor toca el mapa para marcar la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false

        if (isPickerMode) {
            setupPickerMode()
        } else {
            cargarConsultoriosEnMapa()
        }
    }

    private fun setupPickerMode() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 14f))

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            selectedLatLng = latLng
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Ubicación Seleccionada")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
            Toast.makeText(this, "Coordenadas fijadas: ${String.format(Locale.US, "%.5f, %.5f", latLng.latitude, latLng.longitude)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarConsultoriosEnMapa() {
        lifecycleScope.launch {
            mMap.clear()
            val vets = VeterinariaDatabase.getInstance(this@MapsActivity)
                .usuarioDao()
                .obtenerVeterinarios()

            val vetMap = mutableMapOf<Marker, Usuario>()
            val builder = LatLngBounds.Builder()
            var puntosValidos = 0

            val focusLat = intent.getDoubleExtra("VET_LAT", 0.0)
            val focusLng = intent.getDoubleExtra("VET_LNG", 0.0)

            for (vet in vets) {
                if (vet.latitud != 0.0 && vet.longitud != 0.0) {
                    val pos = LatLng(vet.latitud, vet.longitud)
                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title("🏥 ${if (vet.consultorio.isEmpty()) vet.nombre else vet.consultorio}")
                            .snippet("${vet.nombre} · ${vet.especialidad}")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )
                    marker?.let {
                        vetMap[it] = vet
                    }
                    builder.include(pos)
                    puntosValidos++
                }
            }

            if (focusLat != 0.0 && focusLng != 0.0) {
                val focusPos = LatLng(focusLat, focusLng)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(focusPos, 16f))
            } else if (puntosValidos > 0) {
                val bounds = builder.build()
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCenter, 14f))
            }

            mMap.setOnMarkerClickListener { marker ->
                val vet = vetMap[marker]
                if (vet != null) {
                    mostrarDetalleConsultorio(vet)
                }
                false
            }
        }
    }

    private fun mostrarDetalleConsultorio(vet: Usuario) {
        selectedVet = vet
        tvNombreConsultorio.text = if (vet.consultorio.isEmpty()) vet.nombre else vet.consultorio
        tvEspecialidadMapa.text = "${vet.nombre} · ${if (vet.especialidad.isEmpty()) "General" else vet.especialidad}"

        // Cálculo de distancia en km desde centro
        val distMetros = FloatArray(1)
        Location.distanceBetween(
            defaultCenter.latitude, defaultCenter.longitude,
            vet.latitud, vet.longitud,
            distMetros
        )
        val distKm = distMetros[0] / 1000f
        tvDistanciaMapa.text = "📏 Distancia estimada desde el centro: ${String.format(Locale.US, "%.1f", distKm)} km"

        cardDetalleConsultorio.visibility = View.VISIBLE

        btnTrazarRuta.setOnClickListener {
            val uri = Uri.parse("google.navigation:q=${vet.latitud},${vet.longitud}&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${vet.latitud},${vet.longitud}")
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        }

        btnAgendarConVet.setOnClickListener {
            val intent = Intent(this, AgendarCitaActivity::class.java).apply {
                putExtra(AgendarCitaActivity.EXTRA_VET_ID, vet.id)
                putExtra("VET_NOMBRE", vet.nombre)
            }
            startActivity(intent)
        }
    }
}
