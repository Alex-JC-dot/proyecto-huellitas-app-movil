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

    fun iniciarSesion(usuarioId: Int) {
        prefs.edit().putInt(CLAVE_USUARIO_ID, usuarioId).apply()
    }

    fun cerrarSesion() {
        prefs.edit().remove(CLAVE_USUARIO_ID).apply()
    }

    fun haySesionIniciada(): Boolean = obtenerUsuarioId() != SIN_SESION

    /** Devuelve el id del usuario con sesión iniciada, o -1 si no hay sesión. */
    fun obtenerUsuarioId(): Int = prefs.getInt(CLAVE_USUARIO_ID, SIN_SESION)

    companion object {
        private const val NOMBRE_PREFS = "sesion_veterinaria"
        private const val CLAVE_USUARIO_ID = "usuario_id"
        const val SIN_SESION = -1
    }
}
