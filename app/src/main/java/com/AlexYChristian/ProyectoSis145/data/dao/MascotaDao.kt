package com.AlexYChristian.ProyectoSis145.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota

@Dao
interface MascotaDao {

    /** Registrar mascota: devuelve el id generado. */
    @Insert
    suspend fun insertar(mascota: Mascota): Long

    @Update
    suspend fun actualizar(mascota: Mascota)

    /** Elimina la mascota y, por CASCADE, sus citas y vacunas. */
    @Delete
    suspend fun eliminar(mascota: Mascota)

    /** Mis mascotas: todas las mascotas del usuario, ordenadas por nombre. */
    @Query("SELECT * FROM mascotas WHERE usuarioId = :usuarioId ORDER BY nombre")
    suspend fun obtenerPorUsuario(usuarioId: Int): List<Mascota>

    /** Detalle de mascota. */
    @Query("SELECT * FROM mascotas WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Mascota?
}
