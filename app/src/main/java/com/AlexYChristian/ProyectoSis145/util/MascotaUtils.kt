package com.AlexYChristian.ProyectoSis145.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Funciones de ayuda para mostrar los datos de una mascota.
 * Se usan en la lista, en el detalle y en el formulario.
 */
object MascotaUtils {

    // Valores que se guardan en la base de datos
    const val ESPECIE_PERRO = "Perro"
    const val ESPECIE_GATO = "Gato"
    const val ESPECIE_AVE = "Ave"
    const val ESPECIE_OTRO = "Otro"

    const val SEXO_MACHO = "Macho"
    const val SEXO_HEMBRA = "Hembra"

    // Las fechas se guardan como "yyyy-MM-dd" y se muestran como "dd/MM/yyyy"
    private val formatoBD = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val formatoVista = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    /** Emoji que se muestra cuando la mascota no tiene foto. */
    fun emoji(especie: String): String = when (especie) {
        ESPECIE_PERRO -> "🐶"
        ESPECIE_GATO -> "🐱"
        ESPECIE_AVE -> "🐦"
        else -> "🐰"
    }

    /** ♂ para macho y ♀ para hembra. */
    fun simboloSexo(sexo: String): String = if (sexo == SEXO_MACHO) "♂" else "♀"

    /** "2023-05-14" → "14/05/2023" */
    fun fechaParaMostrar(fechaBD: String): String = try {
        formatoVista.format(formatoBD.parse(fechaBD)!!)
    } catch (e: Exception) {
        fechaBD
    }

    /**
     * Calcula la edad a partir de la fecha de nacimiento ("yyyy-MM-dd").
     * Ej: "2 años", "1 año", "8 meses", "1 mes", "Menos de 1 mes".
     */
    fun calcularEdad(fechaNacimiento: String): String {
        val nacimiento = try {
            Calendar.getInstance().apply { time = formatoBD.parse(fechaNacimiento)!! }
        } catch (e: Exception) {
            return ""
        }
        val hoy = Calendar.getInstance()

        var meses = (hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)) * 12 +
                (hoy.get(Calendar.MONTH) - nacimiento.get(Calendar.MONTH))
        // Si todavía no llegó el día del mes en que nació, ese mes no cuenta completo
        if (hoy.get(Calendar.DAY_OF_MONTH) < nacimiento.get(Calendar.DAY_OF_MONTH)) meses--

        val anios = meses / 12
        return when {
            anios >= 2 -> "$anios años"
            anios == 1 -> "1 año"
            meses >= 2 -> "$meses meses"
            meses == 1 -> "1 mes"
            else -> "Menos de 1 mes"
        }
    }

    /** 12.0 → "12 kg", 2.5 → "2.5 kg" */
    fun pesoParaMostrar(peso: Double): String =
        if (peso % 1.0 == 0.0) "${peso.toInt()} kg" else "$peso kg"
}
