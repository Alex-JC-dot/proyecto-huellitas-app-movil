package com.AlexYChristian.ProyectoSis145.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "usuarios": los dueños de mascotas que se registran en la app.
 * El email es único porque se usa para iniciar sesión.
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
    // Prototipo: se guarda en texto plano. En una app real se guardaría un hash.
    val password: String
)
