package com.AlexYChristian.ProyectoSis145.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.AlexYChristian.ProyectoSis145.data.dao.CitaDao
import com.AlexYChristian.ProyectoSis145.data.dao.MascotaDao
import com.AlexYChristian.ProyectoSis145.data.dao.ServicioDao
import com.AlexYChristian.ProyectoSis145.data.dao.UsuarioDao
import com.AlexYChristian.ProyectoSis145.data.dao.VacunaDao
import com.AlexYChristian.ProyectoSis145.data.entity.Cita
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import com.AlexYChristian.ProyectoSis145.data.entity.Servicio
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.AlexYChristian.ProyectoSis145.data.entity.Vacuna

/**
 * Base de datos de la app. Se usa así desde cualquier Activity:
 *
 *     val db = VeterinariaDatabase.getInstance(this)
 *     lifecycleScope.launch {
 *         val mascotas = db.mascotaDao().obtenerPorUsuario(usuarioId)
 *     }
 */
@Database(
    entities = [Usuario::class, Mascota::class, Servicio::class, Cita::class, Vacuna::class],
    version = 1,
    exportSchema = false
)
abstract class VeterinariaDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun mascotaDao(): MascotaDao
    abstract fun servicioDao(): ServicioDao
    abstract fun citaDao(): CitaDao
    abstract fun vacunaDao(): VacunaDao

    companion object {
        private const val NOMBRE_BD = "veterinaria.db"

        // Una sola instancia para toda la app (patrón Singleton).
        @Volatile
        private var INSTANCIA: VeterinariaDatabase? = null

        fun getInstance(context: Context): VeterinariaDatabase {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    VeterinariaDatabase::class.java,
                    NOMBRE_BD
                )
                    .addCallback(precargarServicios)
                    .build()
                    .also { INSTANCIA = it }
            }
        }

        /** Se ejecuta UNA sola vez, cuando la base de datos se crea por primera vez. */
        private val precargarServicios = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val servicios = listOf(
                    Triple("Consulta general", "Revisión completa del estado de salud de la mascota", 80.0),
                    Triple("Vacunación", "Aplicación de vacunas según el calendario de la mascota", 60.0),
                    Triple("Baño y peluquería", "Baño, corte de pelo y limpieza de oídos", 70.0),
                    Triple("Desparasitación", "Tratamiento contra parásitos internos y externos", 50.0),
                    Triple("Cirugía", "Procedimientos quirúrgicos con anestesia", 500.0)
                )
                servicios.forEach { (nombre, descripcion, precio) ->
                    db.execSQL(
                        "INSERT INTO servicios (nombre, descripcion, precio) VALUES (?, ?, ?)",
                        arrayOf<Any>(nombre, descripcion, precio)
                    )
                }
            }
        }
    }
}
