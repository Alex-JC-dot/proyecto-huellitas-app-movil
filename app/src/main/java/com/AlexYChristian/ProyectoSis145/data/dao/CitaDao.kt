package com.AlexYChristian.ProyectoSis145.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.AlexYChristian.ProyectoSis145.data.entity.Cita
import com.AlexYChristian.ProyectoSis145.data.model.CitaDetalle

@Dao
interface CitaDao {

    /** Agendar cita: devuelve el id generado. */
    @Insert
    suspend fun insertar(cita: Cita): Long

    @Delete
    suspend fun eliminar(cita: Cita)

    /** Cambia el estado de una cita. */
    @Query("UPDATE citas SET estado = :estado WHERE id = :citaId")
    suspend fun actualizarEstado(citaId: Int, estado: String)

    /** Asigna un veterinario a una cita. */
    @Query("UPDATE citas SET veterinarioId = :veterinarioId WHERE id = :citaId")
    suspend fun asignarVeterinario(citaId: Int, veterinarioId: Int)

    /**
     * Mis citas (Cliente): citas de las mascotas del usuario con detalle de servicio y mascota.
     */
    @Query(
        """
        SELECT c.id, c.fecha, c.hora, c.estado, c.notas,
               m.id AS mascotaId, m.nombre AS mascotaNombre,
               s.id AS servicioId, s.nombre AS servicioNombre, s.precio AS servicioPrecio,
               c.veterinarioId
        FROM citas c
        INNER JOIN mascotas m ON m.id = c.mascotaId
        INNER JOIN servicios s ON s.id = c.servicioId
        WHERE m.usuarioId = :usuarioId
        ORDER BY c.fecha, c.hora
        """
    )
    suspend fun obtenerPorUsuario(usuarioId: Int): List<CitaDetalle>

    /**
     * Panel del veterinario: sus citas asignadas + las que todavía no tienen veterinario
     * (cualquier veterinario puede tomarlas; al confirmarlas quedan asignadas a él).
     */
    @Query(
        """
        SELECT c.id, c.fecha, c.hora, c.estado, c.notas,
               m.id AS mascotaId, m.nombre AS mascotaNombre,
               s.id AS servicioId, s.nombre AS servicioNombre, s.precio AS servicioPrecio,
               c.veterinarioId
        FROM citas c
        INNER JOIN mascotas m ON m.id = c.mascotaId
        INNER JOIN servicios s ON s.id = c.servicioId
        WHERE c.veterinarioId = :veterinarioId OR c.veterinarioId IS NULL
        ORDER BY c.fecha, c.hora
        """
    )
    suspend fun obtenerPorVeterinario(veterinarioId: Int): List<CitaDetalle>

    /** Detalle de mascota: citas de una sola mascota. */
    @Query("SELECT * FROM citas WHERE mascotaId = :mascotaId ORDER BY fecha DESC, hora DESC")
    suspend fun obtenerPorMascota(mascotaId: Int): List<Cita>
}
