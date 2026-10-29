package com.AlexYChristian.ProyectoSis145.util

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import com.AlexYChristian.ProyectoSis145.R
import com.AlexYChristian.ProyectoSis145.data.entity.Servicio
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Ayudas para fechas de citas y para dibujar un servicio (item_servicio.xml). */
object CitaUtils {

    private val formatoBD = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /** Fecha de hoy como se guarda en la BD: "yyyy-MM-dd". */
    fun hoyBD(): String = formatoBD.format(Calendar.getInstance().time)

    /** Hora actual: "HH:mm". */
    fun horaActual(): String = SimpleDateFormat("HH:mm", Locale.US).format(Calendar.getInstance().time)

    /** Días que faltan desde hoy hasta la fecha (negativo si ya pasó). */
    fun diasHasta(fechaBD: String): Int {
        return try {
            val hoy = formatoBD.parse(hoyBD())!!.time
            val fecha = formatoBD.parse(fechaBD)!!.time
            Math.round((fecha - hoy) / 86_400_000.0).toInt()
        } catch (e: Exception) {
            0
        }
    }

    /** "Hoy", "Mañana", "Faltan 3 días", o null si la fecha ya pasó. */
    fun textoCuentaRegresiva(context: Context, fechaBD: String): String? {
        val dias = diasHasta(fechaBD)
        return when {
            dias < 0 -> null
            dias == 0 -> context.getString(R.string.citas_hoy)
            dias == 1 -> context.getString(R.string.citas_manana)
            else -> context.getString(R.string.citas_faltan, dias)
        }
    }

    /** 80.0 → "Bs 80", 75.5 → "Bs 75.5" */
    fun precio(monto: Double): String =
        if (monto % 1.0 == 0.0) "Bs ${monto.toInt()}" else "Bs $monto"

    /** Pinta el cuadro del ícono con los colores del servicio. */
    fun pintarIcono(fondo: View, icono: ImageView, nombreServicio: String) {
        val estilo = ServicioEstilo.deServicio(nombreServicio)
        val ctx = fondo.context
        ViewCompat.setBackgroundTintList(fondo, ColorStateList.valueOf(ContextCompat.getColor(ctx, estilo.fondo)))
        icono.setImageResource(estilo.icono)
        ImageViewCompat.setImageTintList(icono, ColorStateList.valueOf(ContextCompat.getColor(ctx, estilo.color)))
    }

    /** Llena una vista item_servicio.xml con los datos de un servicio. */
    fun llenarServicio(vista: View, servicio: Servicio) {
        pintarIcono(
            vista.findViewById<FrameLayout>(R.id.fondoIcono),
            vista.findViewById(R.id.ivIcono),
            servicio.nombre
        )
        vista.findViewById<TextView>(R.id.tvNombreServicio).text = servicio.nombre
        vista.findViewById<TextView>(R.id.tvDescripcionServicio).text = servicio.descripcion
        val tvPrecio = vista.findViewById<TextView>(R.id.tvPrecioServicio)
        tvPrecio.text = precio(servicio.precio)
        tvPrecio.setTextColor(ContextCompat.getColor(vista.context, ServicioEstilo.deServicio(servicio.nombre).color))
    }
}
