package com.AlexYChristian.ProyectoSis145.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario

@Dao
interface UsuarioDao {

    /** Registro: guarda un usuario y devuelve su id. Falla si el email ya existe. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    /** Login: devuelve el usuario si email y contraseña coinciden, o null si no. */
    @Query("SELECT * FROM usuarios WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): Usuario?

    /** Registro: revisa si el email ya está en uso antes de guardar. */
    @Query("SELECT COUNT(*) > 0 FROM usuarios WHERE email = :email")
    suspend fun existeEmail(email: String): Boolean

    /** Menú / Perfil: datos del usuario que inició sesión. */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Usuario?

    /** Perfil: guarda cambios en los datos del usuario. */
    @Update
    suspend fun actualizar(usuario: Usuario)
}
