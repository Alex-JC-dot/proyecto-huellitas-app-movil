package com.AlexYChristian.ProyectoSis145.util

import android.util.Log
import com.AlexYChristian.ProyectoSis145.BuildConfig
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import com.AlexYChristian.ProyectoSis145.data.entity.Servicio
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Envía eventos de la app al webhook de Make (make.com) en formato JSON.
 * En Make, el escenario recibe el JSON y puede, por ejemplo, mandar un correo,
 * un WhatsApp o guardar la cita en Google Sheets.
 *
 * - La URL se toma de BuildConfig.MAKE_WEBHOOK_URL (definida en local.properties).
 * - El envío es en segundo plano con enqueue(): no congela la pantalla y sigue
 *   aunque la pantalla se cierre.
 * - Si no hay internet o falla, la app sigue funcionando normal (solo se registra en Logcat).
 *
 * Eventos que se envían:
 *   "usuario_registrado"  → al crear una cuenta
 *   "cita_agendada"       → al confirmar una cita
 *   "cita_actualizada"    → al completar o cancelar una cita
 */
object MakeWebhook {

    private const val TAG = "MakeWebhook"   // filtrar por esto en Logcat
    private val JSON = "application/json; charset=utf-8".toMediaType()

    // Un solo cliente para toda la app (OkHttp recomienda reutilizarlo)
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /** Envía un evento con sus datos. No hace nada si no hay URL configurada. */
    fun enviar(evento: String, datos: JSONObject) {
        val url = BuildConfig.MAKE_WEBHOOK_URL
        if (url.isBlank()) {
            Log.w(TAG, "No hay MAKE_WEBHOOK_URL en local.properties: no se envía \"$evento\"")
            return
        }

        val cuerpo = JSONObject()
            .put("evento", evento)
            .put("app", "Huellitas")
            .put("enviado_en", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
            .put("datos", datos)

        val peticion = Request.Builder()
            .url(url)
            .post(cuerpo.toString().toRequestBody(JSON))
            .build()

        cliente.newCall(peticion).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "No se pudo enviar \"$evento\": ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) Log.i(TAG, "\"$evento\" enviado (HTTP ${it.code})")
                    else Log.e(TAG, "Make respondió HTTP ${it.code} para \"$evento\"")
                }
            }
        })
    }

    // ---------- Eventos de la app ----------

    fun usuarioRegistrado(usuario: Usuario) {
        enviar("usuario_registrado", usuarioJson(usuario))
    }

    fun citaAgendada(
        citaId: Long,
        usuario: Usuario?,
        mascota: Mascota,
        servicio: Servicio,
        fecha: String,
        hora: String,
        notas: String
    ) {
        val datos = JSONObject()
            .put("cita", JSONObject()
                .put("id", citaId)
                .put("fecha", fecha)
                .put("fecha_legible", MascotaUtils.fechaParaMostrar(fecha))
                .put("hora", hora)
                .put("estado", "PENDIENTE")
                .put("notas", notas))
            .put("mascota", JSONObject()
                .put("nombre", mascota.nombre)
                .put("especie", mascota.especie)
                .put("raza", mascota.raza)
                .put("sexo", mascota.sexo))
            .put("servicio", JSONObject()
                .put("nombre", servicio.nombre)
                .put("precio", servicio.precio))
        if (usuario != null) datos.put("usuario", usuarioJson(usuario))
        enviar("cita_agendada", datos)
    }

    fun citaActualizada(
        citaId: Int,
        nuevoEstado: String,
        mascotaNombre: String,
        servicioNombre: String,
        fecha: String,
        hora: String,
        usuario: Usuario?
    ) {
        val datos = JSONObject()
            .put("cita", JSONObject()
                .put("id", citaId)
                .put("estado", nuevoEstado)
                .put("fecha", fecha)
                .put("fecha_legible", MascotaUtils.fechaParaMostrar(fecha))
                .put("hora", hora))
            .put("mascota", JSONObject().put("nombre", mascotaNombre))
            .put("servicio", JSONObject().put("nombre", servicioNombre))
        if (usuario != null) datos.put("usuario", usuarioJson(usuario))
        enviar("cita_actualizada", datos)
    }

    // Nunca se envía la contraseña
    private fun usuarioJson(u: Usuario) = JSONObject()
        .put("id", u.id)
        .put("nombre", u.nombre)
        .put("email", u.email)
        .put("telefono", u.telefono)
}
