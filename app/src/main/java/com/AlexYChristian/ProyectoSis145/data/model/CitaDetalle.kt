package com.AlexYChristian.ProyectoSis145.data.model

/**
 * Resultado de unir (JOIN) citas + mascotas + servicios (+ veterinarioId).
 */
data class CitaDetalle(
    val id: Int,
    val fecha: String,
    val hora: String,
    val estado: String,
    val notas: String,
    val mascotaId: Int,
    val mascotaNombre: String,
    val servicioId: Int,
    val servicioNombre: String,
    val servicioPrecio: Double,
    val veterinarioId: Int? = null
)
