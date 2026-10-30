package com.AlexYChristian.ProyectoSis145.util

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.entity.Cita

/**
 * Colores e íconos de la app según el servicio o el estado de una cita.
 * Así toda la app usa el mismo color para lo mismo (ej: Vacunación siempre amarillo).
 */
data class Estilo(
    @DrawableRes val icono: Int,
    @ColorRes val fondo: Int,    // color suave (fondo del cuadro o de la píldora)
    @ColorRes val color: Int     // color fuerte (ícono, texto, barra lateral)
)

object ServicioEstilo {

    /** Estilo según el nombre del servicio (los 5 que se precargan en la BD). */
    fun deServicio(nombreServicio: String): Estilo {
        val nombre = nombreServicio.lowercase()
        return when {
            "vacun" in nombre -> Estilo(R.drawable.ic_servicio_vacuna, R.color.mantequilla_fondo, R.color.mantequilla)
            "baño" in nombre || "pelu" in nombre -> Estilo(R.drawable.ic_servicio_bano, R.color.celeste_fondo, R.color.celeste)
            "despara" in nombre -> Estilo(R.drawable.ic_servicio_desparasitacion, R.color.lavanda_fondo, R.color.lavanda)
            "cirug" in nombre -> Estilo(R.drawable.ic_servicio_cirugia, R.color.rosa_fondo, R.color.rosa)
            else -> Estilo(R.drawable.ic_servicio_consulta, R.color.durazno_fondo, R.color.durazno)
        }
    }

    /** Colores de la etiqueta de estado de una cita. */
    fun deEstado(estado: String): Estilo = when (estado) {
        Cita.ESTADO_COMPLETADA -> Estilo(0, R.color.fondo_suave, R.color.verde_accion)
        Cita.ESTADO_CANCELADA -> Estilo(0, R.color.rosa_fondo, R.color.rosa)
        Cita.ESTADO_CONFIRMADA -> Estilo(0, R.color.celeste_fondo, R.color.celeste)
        else -> Estilo(0, R.color.mantequilla_fondo, R.color.mantequilla)
    }

    /** Texto para mostrar del estado: "PENDIENTE" → "Pendiente". */
    fun textoEstado(estado: String): String =
        estado.lowercase().replaceFirstChar { it.uppercase() }
}
