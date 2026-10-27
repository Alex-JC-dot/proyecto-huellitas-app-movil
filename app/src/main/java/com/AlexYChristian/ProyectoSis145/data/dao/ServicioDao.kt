package com.AlexYChristian.ProyectoSis145.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.AlexYChristian.ProyectoSis145.data.entity.Servicio

@Dao
interface ServicioDao {

    @Insert
    suspend fun insertarTodos(servicios: List<Servicio>)

    /** Servicios / Agendar cita: catálogo completo. */
    @Query("SELECT * FROM servicios ORDER BY nombre")
    suspend fun obtenerTodos(): List<Servicio>

    @Query("SELECT * FROM servicios WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Servicio?
}
