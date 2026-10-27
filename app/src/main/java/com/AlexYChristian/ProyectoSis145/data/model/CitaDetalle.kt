package com.AlexYChristian.ProyectoSis145.data.model

/**
 * No es una tabla: es el resultado de unir (JOIN) citas + mascotas + servicios,
 * para mostrar en la lista "Mis citas" el nombre de la mascota y del servicio
 * sin hacer varias consultas.
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
    val servicioPrecio: Double
)
