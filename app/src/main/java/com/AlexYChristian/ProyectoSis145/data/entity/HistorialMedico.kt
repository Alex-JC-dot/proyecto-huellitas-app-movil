package com.AlexYChristian.ProyectoSis145.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "historial_medico": ficha clínica y pediátrica offline de cada mascota.
 * Registra diagnósticos, tratamientos, evolución de peso y notas médicas.
 */
@Entity(
    tableName = "historial_medico",
    foreignKeys = [
        ForeignKey(
            entity = Mascota::class,
            parentColumns = ["id"],
            childColumns = ["mascotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mascotaId"), Index("veterinarioId")]
)
data class HistorialMedico(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mascotaId: Int,
    val veterinarioId: Int,
    val fecha: String,               // "yyyy-MM-dd"
    val diagnostico: String,
    val tratamiento: String,
    val peso: Double,                // Peso registrado en la consulta (kg)
    val notas: String = ""
)
