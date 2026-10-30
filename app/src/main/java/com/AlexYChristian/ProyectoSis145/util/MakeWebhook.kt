package com.AlexYChristian.ProyectoSis145.util

import android.util.Log
import com.AlexYChristian.ProyectoSis145.BuildConfig
import com.AlexYChristian.ProyectoSis145.data.entity.Cita
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import com.AlexYChristian.ProyectoSis145.data.entity.Servicio
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.AlexYChristian.ProyectoSis145.data.entity.Vacuna
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
 * Envía los eventos de la app al escenario de Make "🐾 Huellitas · App → IA → Sheets → Gmail".
 *
 * Formato (el que espera el escenario): todo en la raíz del JSON
 *   {
 *     "evento": "cita_agendada",
 *     "clave_app": "...",            ← el filtro 🔐 de Make solo deja pasar esta clave
 *     "enviado_en": "2026-10-30T10:15:00-04:00",
 *     "app": { "nombre": "Huellitas", "version": "1.0", "plataforma": "Android" },
 *     "usuario": {...}, "mascota": {...}, "servicio": {...}, "cita": {...}, "vacuna": {...}
 *   }
 *
 * Eventos: usuario_registrado, mascota_registrada, cita_agendada, cita_estado, vacuna_registrada.
 *
 * - La URL y la clave se leen de local.properties (MAKE_WEBHOOK_URL y MAKE_CLAVE_APP),
 *   que NO se sube a GitHub. Gradle las pasa a BuildConfig.
 * - Se envía en segundo plano con OkHttp (enqueue): no congela la pantalla y no se corta
 *   aunque la pantalla se cierre. Si no hay internet, la app sigue funcionando igual.
 * - Nunca se envía la contraseña ni la ruta de la foto.
 * - Para ver qué pasa: Logcat → filtrar por "MakeWebhook".
 */
object MakeWebhook {

    private const val TAG = "MakeWebhook"
    private const val VERSION_APP = "1.0"
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private val cliente = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    // ------------------------------------------------------------------ Eventos

    fun usuarioRegistrado(usuario: Usuario) =
        enviar("usuario_registrado", JSONObject().put("usuario", usuarioJson(usuario)))

    fun mascotaRegistrada(usuario: Usuario, mascota: Mascota) =
        enviar(
            "mascota_registrada",
            JSONObject()
                .put("usuario", usuarioJson(usuario))
                .put("mascota", mascotaJson(mascota))
        )

    fun citaAgendada(usuario: Usuario, mascota: Mascota, servicio: Servicio, cita: Cita) =
        enviar("cita_agendada", datosCita(usuario, mascota, servicio, cita))

    /** La cita pasó a COMPLETADA o CANCELADA (cita.estado ya tiene el estado nuevo). */
    fun citaCambioEstado(usuario: Usuario, mascota: Mascota, servicio: Servicio, cita: Cita) =
        enviar("cita_estado", datosCita(usuario, mascota, servicio, cita))

    fun vacunaRegistrada(usuario: Usuario, mascota: Mascota, vacuna: Vacuna) =
        enviar(
            "vacuna_registrada",
            JSONObject()
                .put("usuario", usuarioJson(usuario))
                .put("mascota", mascotaJson(mascota))
                .put(
                    "vacuna", JSONObject()
                        .put("id", vacuna.id)
                        .put("nombre", vacuna.nombre)
                        .put("fechaAplicacion", vacuna.fechaAplicacion)
                        .put("proximaDosis", vacuna.proximaDosis)
                )
        )

    // ------------------------------------------------------------------ JSON

    private fun usuarioJson(u: Usuario): JSONObject = JSONObject()
        .put("id", u.id)
        .put("nombre", u.nombre)
        .put("email", u.email)
        .put("telefono", u.telefono)
    // u.password NO se envía nunca

    private fun mascotaJson(m: Mascota): JSONObject = JSONObject()
        .put("id", m.id)
        .put("nombre", m.nombre)
        .put("especie", m.especie)
        .put("raza", m.raza)
        .put("sexo", m.sexo)
        .put("fechaNacimiento", m.fechaNacimiento)
        .put("peso", m.peso)
    // m.foto es una ruta dentro del celular: fuera de la app no sirve, no se envía

    private fun datosCita(u: Usuario, m: Mascota, s: Servicio, c: Cita): JSONObject = JSONObject()
        .put("usuario", usuarioJson(u))
        .put("mascota", mascotaJson(m))
        .put(
            "servicio", JSONObject()
                .put("id", s.id)
                .put("nombre", s.nombre)
                .put("precio", s.precio)
        )
        .put(
            "cita", JSONObject()
                .put("id", c.id)
                .put("fecha", c.fecha)
                .put("hora", c.hora)
                .put("estado", c.estado)
                .put("notas", c.notas)
        )

    // ------------------------------------------------------------------ Envío

    private fun enviar(evento: String, datos: JSONObject) {
        val url = BuildConfig.MAKE_WEBHOOK_URL
        if (url.isBlank()) {
            Log.w(TAG, "Falta MAKE_WEBHOOK_URL en local.properties: no se envía \"$evento\"")
            return
        }
        if (BuildConfig.MAKE_CLAVE_APP.isBlank()) {
            Log.w(TAG, "Falta MAKE_CLAVE_APP en local.properties: Make va a rechazar \"$evento\"")
        }

        datos.put("evento", evento)
            .put("clave_app", BuildConfig.MAKE_CLAVE_APP)
            .put("enviado_en", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date()))
            .put(
                "app", JSONObject()
                    .put("nombre", "Huellitas")
                    .put("version", VERSION_APP)
                    .put("plataforma", "Android")
            )

        val peticion = Request.Builder()
            .url(url)
            .post(datos.toString().toRequestBody(JSON))
            .build()

        cliente.newCall(peticion).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "No se pudo enviar \"$evento\" a Make: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) Log.i(TAG, "\"$evento\" enviado (HTTP ${it.code})")
                    else Log.e(TAG, "Make respondió HTTP ${it.code} para \"$evento\"")
                }
            }
        })
    }
}
