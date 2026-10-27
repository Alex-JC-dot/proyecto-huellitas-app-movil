package com.AlexYChristian.ProyectoSis145.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla "servicios": catálogo de la veterinaria (consulta, vacunación, baño...).
 * Se llena automáticamente al crear la base de datos (ver VeterinariaDatabase).
 */
@Entity(tableName = "servicios")
data class Servicio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val precio: Double            // En Bs
)
