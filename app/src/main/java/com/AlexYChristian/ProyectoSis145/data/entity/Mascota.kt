package com.AlexYChristian.ProyectoSis145.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "mascotas". Cada mascota pertenece a un usuario (usuarioId).
 * Si se elimina el usuario, se eliminan sus mascotas (CASCADE).
 * El campo foto se agregó en la versión 2 de la base de datos.
 */
@Entity(
    tableName = "mascotas",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class Mascota(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val especie: String,          // Perro, Gato, Ave...
    val raza: String,
    val sexo: String,             // Macho / Hembra
    val fechaNacimiento: String,  // Formato "yyyy-MM-dd", ej: "2023-05-14"
    val peso: Double,             // En kg
    // Ruta de la foto guardada en la carpeta privada de la app ("" = sin foto).
    // defaultValue debe coincidir con el DEFAULT de la migración 1 → 2.
    @ColumnInfo(defaultValue = "")
    val foto: String = ""
)
