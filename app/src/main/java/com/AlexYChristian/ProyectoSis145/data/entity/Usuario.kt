package com.AlexYChristian.ProyectoSis145.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "usuarios": dueños de mascotas (CLIENTE), veterinarios (VETERINARIO)
 * y administradores del sistema (SUPER_ADMIN).
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    val telefono: String,
    val password: String,
    
    // Rol del usuario: ROL_CLIENTE, ROL_VETERINARIO, ROL_SUPER_ADMIN
    @ColumnInfo(defaultValue = ROL_CLIENTE)
    val rol: String = ROL_CLIENTE,

    // Campos adicionales para Veterinarios y Consultorios
    @ColumnInfo(defaultValue = "")
    val especialidad: String = "",

    @ColumnInfo(defaultValue = "")
    val licencia: String = "",

    @ColumnInfo(defaultValue = "0.0")
    val latitud: Double = 0.0,

    @ColumnInfo(defaultValue = "0.0")
    val longitud: Double = 0.0,

    @ColumnInfo(defaultValue = "")
    val consultorio: String = ""
) {
    companion object {
        const val ROL_SUPER_ADMIN = "SUPER_ADMIN"
        const val ROL_VETERINARIO = "VETERINARIO"
        const val ROL_CLIENTE = "CLIENTE"
    }
}
