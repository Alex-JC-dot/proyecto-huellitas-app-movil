package com.AlexYChristian.ProyectoSis145.data

import android.content.Context

/**
 * Guarda la sesión del usuario con SharedPreferences, para que la app
 * recuerde quién inició sesión aunque se cierre.
 *
 *     val session = SessionManager(this)
 *     session.iniciarSesion(usuario.id)
 *     val id = session.obtenerUsuarioId()
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE)

    /**
     * @param recordar si es true, la próxima vez que se abra la app entra directo al menú.
     *                 Si es false, la sesión sirve mientras la app esté abierta y luego pide login.
     */
    fun iniciarSesion(usuarioId: Int, recordar: Boolean = true) {
        prefs.edit()
            .putInt(CLAVE_USUARIO_ID, usuarioId)
            .putBoolean(CLAVE_RECORDAR, recordar)
            .apply()
    }

    fun cerrarSesion() {
        prefs.edit().remove(CLAVE_USUARIO_ID).remove(CLAVE_RECORDAR).apply()
    }

    fun haySesionIniciada(): Boolean = obtenerUsuarioId() != SIN_SESION

    /** true si hay sesión y el usuario marcó "Recordarme" (se usa al abrir la app). */
    fun haySesionRecordada(): Boolean =
        haySesionIniciada() && prefs.getBoolean(CLAVE_RECORDAR, true)

    /** Devuelve el id del usuario con sesión iniciada, o -1 si no hay sesión. */
    fun obtenerUsuarioId(): Int = prefs.getInt(CLAVE_USUARIO_ID, SIN_SESION)

    companion object {
        private const val NOMBRE_PREFS = "sesion_veterinaria"
        private const val CLAVE_USUARIO_ID = "usuario_id"
        private const val CLAVE_RECORDAR = "recordar"
        const val SIN_SESION = -1
    }
}
