package com.AlexYChristian.ProyectoSis145.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.AlexYChristian.ProyectoSis145.data.dao.CitaDao
import com.AlexYChristian.ProyectoSis145.data.dao.HistorialMedicoDao
import com.AlexYChristian.ProyectoSis145.data.dao.MascotaDao
import com.AlexYChristian.ProyectoSis145.data.dao.ServicioDao
import com.AlexYChristian.ProyectoSis145.data.dao.UsuarioDao
import com.AlexYChristian.ProyectoSis145.data.dao.VacunaDao
import com.AlexYChristian.ProyectoSis145.data.entity.Cita
import com.AlexYChristian.ProyectoSis145.data.entity.HistorialMedico
import com.AlexYChristian.ProyectoSis145.data.entity.Mascota
import com.AlexYChristian.ProyectoSis145.data.entity.Servicio
import com.AlexYChristian.ProyectoSis145.data.entity.Usuario
import com.AlexYChristian.ProyectoSis145.data.entity.Vacuna

@Database(
    entities = [
        Usuario::class,
        Mascota::class,
        Servicio::class,
        Cita::class,
        Vacuna::class,
        HistorialMedico::class
    ],
    version = 3,   // v3: RBAC (roles, especialidad, coords, consultorio) + Cita.veterinarioId + HistorialMedico
    exportSchema = false
)
abstract class VeterinariaDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun mascotaDao(): MascotaDao
    abstract fun servicioDao(): ServicioDao
    abstract fun citaDao(): CitaDao
    abstract fun vacunaDao(): VacunaDao
    abstract fun historialMedicoDao(): HistorialMedicoDao

    companion object {
        private const val NOMBRE_BD = "veterinaria.db"

        @Volatile
        private var INSTANCIA: VeterinariaDatabase? = null

        fun getInstance(context: Context): VeterinariaDatabase {
            return INSTANCIA ?: synchronized(this) {
                INSTANCIA ?: Room.databaseBuilder(
                    context.applicationContext,
                    VeterinariaDatabase::class.java,
                    NOMBRE_BD
                )
                    .addMigrations(MIGRACION_1_2, MIGRACION_2_3)
                    // Si se instala una versión más vieja de la app encima (por ejemplo al cambiar
                    // de rama), se recrea la base en vez de cerrarse
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .addCallback(precargarDatosIniciales)
                    .build()
                    .also { INSTANCIA = it }
            }
        }

        /** Migración 1 -> 2: agrega la columna "foto" a mascotas. */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mascotas ADD COLUMN foto TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Migración 2 -> 3: agrega campos RBAC a usuarios, veterinarioId a citas y crea tabla historial_medico. */
        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Alterar tabla usuarios
                db.execSQL("ALTER TABLE usuarios ADD COLUMN rol TEXT NOT NULL DEFAULT 'CLIENTE'")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN especialidad TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN licencia TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN latitud REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN longitud REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN consultorio TEXT NOT NULL DEFAULT ''")

                // Alterar tabla citas
                db.execSQL("ALTER TABLE citas ADD COLUMN veterinarioId INTEGER DEFAULT NULL")
                // Room espera este índice (Cita.kt lo declara); sin él la app se cierra al migrar
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_citas_veterinarioId` ON `citas` (`veterinarioId`)")

                // Crear tabla historial_medico
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `historial_medico` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `mascotaId` INTEGER NOT NULL,
                        `veterinarioId` INTEGER NOT NULL,
                        `fecha` TEXT NOT NULL,
                        `diagnostico` TEXT NOT NULL,
                        `tratamiento` TEXT NOT NULL,
                        `peso` REAL NOT NULL,
                        `notas` TEXT NOT NULL,
                        FOREIGN KEY(`mascotaId`) REFERENCES `mascotas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_historial_medico_mascotaId` ON `historial_medico` (`mascotaId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_historial_medico_veterinarioId` ON `historial_medico` (`veterinarioId`)")

                // Insertar usuario Super Admin por defecto si no existe
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO usuarios (nombre, email, telefono, password, rol, especialidad, licencia, latitud, longitud, consultorio)
                    VALUES ('Super Administrador', 'admin@huellitas.com', '70000000', 'admin123', 'SUPER_ADMIN', 'Administración General', 'ADM-001', 0.0, 0.0, 'Oficina Central')
                    """
                )
            }
        }

        /** Precarga inicial de servicios y usuario Super Admin. */
        private val precargarDatosIniciales = object : Callback() {
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

                // Super Admin inicial
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO usuarios (nombre, email, telefono, password, rol, especialidad, licencia, latitud, longitud, consultorio)
                    VALUES ('Super Administrador', 'admin@huellitas.com', '70000000', 'admin123', 'SUPER_ADMIN', 'Administración General', 'ADM-001', 0.0, 0.0, 'Oficina Central')
                    """
                )
            }
        }
    }
}
