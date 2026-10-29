package com.AlexYChristian.ProyectoSis145.util

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Configura una pantalla con cabecera verde olivo que llega hasta arriba:
 * - La hora y la batería se ven en blanco.
 * - La raíz no tiene margen arriba (el verde pasa por detrás de la barra de estado).
 * - El contenido de la cabecera baja lo justo para no quedar debajo de la hora.
 */
fun AppCompatActivity.configurarCabeceraVerde(raiz: View, contenidoCabecera: View) {
    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    val paddingTopOriginal = contenidoCabecera.paddingTop
    ViewCompat.setOnApplyWindowInsetsListener(raiz) { v, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(barras.left, 0, barras.right, barras.bottom)
        contenidoCabecera.setPadding(
            contenidoCabecera.paddingLeft,
            paddingTopOriginal + barras.top,
            contenidoCabecera.paddingRight,
            contenidoCabecera.paddingBottom
        )
        insets
    }
}
