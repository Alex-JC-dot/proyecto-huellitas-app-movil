package com.AlexYChristian.ProyectoSis145.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.AlexYChristian.ProyectoSis145.data.entity.HistorialMedico

@Dao
interface HistorialMedicoDao {

    @Insert
    suspend fun insertar(historial: HistorialMedico): Long

    @Delete
    suspend fun eliminar(historial: HistorialMedico)

    /** Historial médico completo de una mascota ordenada por fecha más reciente primero. */
    @Query("SELECT * FROM historial_medico WHERE mascotaId = :mascotaId ORDER BY fecha DESC, id DESC")
    suspend fun obtenerPorMascota(mascotaId: Int): List<HistorialMedico>
}
