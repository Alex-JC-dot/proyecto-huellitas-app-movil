package com.AlexYChristian.ProyectoSis145.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import java.io.File
import java.io.FileOutputStream

/**
 * Manejo de las fotos de las mascotas.
 *
 * La foto que el usuario elige de la galería se COPIA a la carpeta privada de la app
 * (filesDir/fotos), reducida de tamaño. En la base de datos solo se guarda la ruta
 * de ese archivo, no la imagen: las bases de datos no son buenas guardando archivos grandes.
 */
object FotoUtils {

    private const val LADO_MAXIMO = 800   // px: suficiente para verse bien y pesar poco

    /**
     * Copia la foto elegida a la carpeta de la app. Devuelve la ruta del archivo nuevo,
     * o "" si algo falla. Es una tarea pesada: llamarla con withContext(Dispatchers.IO).
     */
    fun guardarFoto(context: Context, uri: Uri): String {
        return try {
            val resolver = context.contentResolver

            // 1. Leer solo el tamaño de la imagen (sin cargarla entera en memoria)
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }

            // 2. Calcular cuánto reducirla (inSampleSize: 2 = la mitad, 4 = un cuarto...)
            var reduccion = 1
            while (limites.outWidth / (reduccion * 2) >= LADO_MAXIMO &&
                limites.outHeight / (reduccion * 2) >= LADO_MAXIMO
            ) {
                reduccion *= 2
            }

            // 3. Cargar la imagen ya reducida
            val opciones = BitmapFactory.Options().apply { inSampleSize = reduccion }
            var bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opciones)
            } ?: return ""

            // 4. Girarla si la cámara la guardó "acostada" (dato EXIF de la foto)
            val grados = resolver.openInputStream(uri)?.use { leerRotacion(it) } ?: 0
            if (grados != 0) {
                val matriz = Matrix().apply { postRotate(grados.toFloat()) }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
            }

            // 5. Guardarla como JPG en la carpeta privada de la app
            val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
            val archivo = File(carpeta, "mascota_${System.currentTimeMillis()}.jpg")
            FileOutputStream(archivo).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            archivo.absolutePath
        } catch (e: Exception) {
            ""
        }
    }

    private fun leerRotacion(entrada: java.io.InputStream): Int =
        when (ExifInterface(entrada).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    /** Carga la foto desde su ruta, o null si no tiene foto o el archivo no existe. */
    fun cargarFoto(ruta: String): Bitmap? =
        if (ruta.isNotEmpty() && File(ruta).exists()) BitmapFactory.decodeFile(ruta) else null

    /** Borra el archivo de la foto (al eliminar una mascota). */
    fun borrarFoto(ruta: String) {
        if (ruta.isNotEmpty()) File(ruta).delete()
    }

    /**
     * Muestra la foto de la mascota en ivFoto.
     * Si no tiene foto, la oculta y muestra en ivSilueta la silueta de su especie
     * (perro, gato o una huella para las demás). El color de la silueta lo da el XML (app:tint).
     */
    fun mostrar(ivFoto: ImageView, ivSilueta: ImageView, mascota: Mascota) {
        val foto = cargarFoto(mascota.foto)
        if (foto != null) {
            ivFoto.setImageBitmap(foto)
            ivSilueta.visibility = View.GONE
        } else {
            ivFoto.setImageDrawable(null)
            ivSilueta.setImageResource(MascotaUtils.silueta(mascota.especie))
            ivSilueta.visibility = View.VISIBLE
        }
    }
}
