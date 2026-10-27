package com.AlexYChristian.ProyectoSis145.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.AlexYChristian.ProyectoSis145.data.entity.Vacuna

@Dao
interface VacunaDao {

    @Insert
    suspend fun insertar(vacuna: Vacuna): Long

    @Delete
    suspend fun eliminar(vacuna: Vacuna)

    /** Detalle de mascota: historial de vacunas, la más reciente primero. */
    @Query("SELECT * FROM vacunas WHERE mascotaId = :mascotaId ORDER BY fechaAplicacion DESC")
    suspend fun obtenerPorMascota(mascotaId: Int): List<Vacuna>
}
