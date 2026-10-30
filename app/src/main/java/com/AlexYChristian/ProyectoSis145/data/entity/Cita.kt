package com.AlexYChristian.ProyectoSis145.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "citas": cita de UNA mascota para UN servicio, asignada opcionalmente a UN veterinario.
 */
@Entity(
    tableName = "citas",
    foreignKeys = [
        ForeignKey(
            entity = Mascota::class,
            parentColumns = ["id"],
            childColumns = ["mascotaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Servicio::class,
            parentColumns = ["id"],
            childColumns = ["servicioId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("mascotaId"), Index("servicioId"), Index("veterinarioId")]
)
data class Cita(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mascotaId: Int,
    val servicioId: Int,
    val fecha: String,                    // "yyyy-MM-dd", ej: "2026-11-05"
    val hora: String,                     // "HH:mm", ej: "15:30"
    val estado: String = ESTADO_PENDIENTE,
    val notas: String = "",
    
    // ID del veterinario (Usuario con rol VETERINARIO) asignado a la cita
    @ColumnInfo(defaultValue = "NULL")
    val veterinarioId: Int? = null
) {
    companion object {
        const val ESTADO_PENDIENTE = "PENDIENTE"
        const val ESTADO_CONFIRMADA = "CONFIRMADA"
        const val ESTADO_COMPLETADA = "COMPLETADA"
        const val ESTADO_CANCELADA = "CANCELADA"
    }
}
