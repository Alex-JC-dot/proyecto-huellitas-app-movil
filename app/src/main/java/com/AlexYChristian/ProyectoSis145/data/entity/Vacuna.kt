package com.AlexYChristian.ProyectoSis145.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "vacunas": historial de vacunas de cada mascota.
 */
@Entity(
    tableName = "vacunas",
    foreignKeys = [
        ForeignKey(
            entity = Mascota::class,
            parentColumns = ["id"],
            childColumns = ["mascotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mascotaId")]
)
data class Vacuna(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mascotaId: Int,
    val nombre: String,              // Rabia, Parvovirus...
    val fechaAplicacion: String,     // "yyyy-MM-dd"
    val proximaDosis: String = ""    // "yyyy-MM-dd" o vacío si no aplica
)
