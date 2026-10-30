package com.AlexYChristian.ProyectoSis145.data

import android.content.Context
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario

/**
 * Guarda la sesión del usuario con SharedPreferences (id y rol).
 */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE)

    fun iniciarSesion(usuarioId: Int, rol: String = Usuario.ROL_CLIENTE, recordar: Boolean = true) {
        prefs.edit()
            .putInt(CLAVE_USUARIO_ID, usuarioId)
            .putString(CLAVE_ROL, rol)
            .putBoolean(CLAVE_RECORDAR, recordar)
            .apply()
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    fun haySesionIniciada(): Boolean = obtenerUsuarioId() != SIN_SESION

    fun haySesionRecordada(): Boolean =
        haySesionIniciada() && prefs.getBoolean(CLAVE_RECORDAR, true)

    fun obtenerUsuarioId(): Int = prefs.getInt(CLAVE_USUARIO_ID, SIN_SESION)

    fun obtenerRol(): String = prefs.getString(CLAVE_ROL, Usuario.ROL_CLIENTE) ?: Usuario.ROL_CLIENTE

    companion object {
        private const val NOMBRE_PREFS = "sesion_veterinaria"
        private const val CLAVE_USUARIO_ID = "usuario_id"
        private const val CLAVE_ROL = "usuario_rol"
        private const val CLAVE_RECORDAR = "recordar"
        const val SIN_SESION = -1
    }
}
