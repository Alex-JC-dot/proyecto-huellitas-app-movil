# 🐾 Documentación Completa: Ecosistema Veterinario Huellitas

## 📌 Visión General del Proyecto
**Huellitas** es una plataforma móvil nativa desarrollada en Kotlin para Android que transforma la gestión médica de mascotas en un **Ecosistema Veterinario Profesional Multi-Rol de Alto Nivel (RBAC)**. 

La aplicación integra la potencia de **Google Maps SDK** para la ubicación física de consultorios, automatización de eventos asíncronos mediante **Webhooks HTTP POST a Make.com (Integromat)** y persistencia de datos local/offline utilizando **Room ORM**.

---

## 🛡️ 1. Control de Acceso Basado en Roles (RBAC)

La aplicación cuenta con una separación estricta de interfaces y permisos según el rol del usuario autenticado:

### A. Super Administrador (`SUPER_ADMIN`)
* **Credenciales por Defecto**:
  * **Email**: `admin@huellitas.com`
  * **Contraseña**: `admin123`
* **Funcionalidades**:
  * Acceso exclusivo al **Panel de Administración** (`AdminVeterinariosActivity`).
  * Lista detallada de todos los veterinarios y consultorios registrados.
  * Formulario de registro de profesionales (`RegistrarVeterinarioActivity`): asignación de credenciales únicas (email/contraseña), teléfono, especialidad, licencia profesional, consultorio y ubicación geográfica (latitud y longitud).
  * **Disparo Automático de Webhook**: Al guardar un nuevo veterinario, la app envía un payload JSON a Make.com para notificar las credenciales de acceso vía Email / Telegram / WhatsApp.

### B. Veterinario (`VETERINARIO`)
* **Funcionalidades**:
  * Panel de Atenciones exclusivo (`VetPanelActivity`).
  * Filtro de citas en pestañas (*Pendientes*, *Atendidas*, *Todas*).
  * Gestión de estado de las citas asignadas:
    * `CONFIRMADA`: Confirma la recepción del paciente.
    * `COMPLETADA` (botón *Atender*): marca la consulta como atendida y abre la **Ficha Médica Offline**.
    * `CANCELADA`: Cancela la cita informando el evento a Make.com.
  * Registro e historial médico offline por mascota.

### C. Cliente / Dueño de Mascota (`CLIENTE`)
* **Funcionalidades**:
  * Menú dinámico de usuario (`MenuActivity`).
  * Registro e inspección de mascotas con fotos y ficha técnica.
  * Agendamiento de citas asociadas a servicios y consultorios específicos (`AgendarCitaActivity`).
  * Mapa interactivo de consultorios veterinarios con cálculo de distancias y rutas.

---

## 🗺️ 2. Integración de Google Maps SDK & Geolocalización

* **Clave API**: se configura en `local.properties` como `MAPS_API_KEY=...` (no se sube a GitHub). Gradle la inserta en el `AndroidManifest.xml` mediante `manifestPlaceholders`.
* **Visualización de Consultorios (`MapsActivity`)**:
  * Muestra marcadores personalizados (`🏥 Consultorio`) para cada veterinario registrado en la base de datos local.
  * **Diseño Ergónomico**: Al tocar un marcador, se despliega una tarjeta flotante inferior con la información del médico, especialidad y distancia estimada en kilómetros desde la ubicación del usuario o el centro urbano.
  * **Navegación Trazada**: El botón **"📍 Trazar Ruta"** envía un Intent directo a la aplicación oficial de Google Maps (`google.navigation:q=lat,lng`) para guiar al usuario turno a turno.
* **Modo Selector de Mapa ("Map Picker")**:
  * Al registrar un consultorio desde el panel de administración, el Super Admin puede abrir el mapa y tocar cualquier punto geográfico para definir las coordenadas de latitud y longitud.

---

## 🔄 3. Integración con Make.com (Webhook)

* **Configuración** (`local.properties`, no se sube a GitHub): `MAKE_WEBHOOK_URL` y `MAKE_CLAVE_APP`. Ver `local.properties.example`.
* **Implementación**: `util/MakeWebhook.kt` con **OkHttp** (`enqueue`, en segundo plano). Si no hay internet, la app sigue funcionando.
* **Seguridad**: cada envío lleva `clave_app`; el filtro 🔐 del escenario de Make solo acepta esa clave. Nunca se envían contraseñas.

### Eventos que se envían
| Evento | Cuándo |
|---|---|
| `usuario_registrado` | Un cliente crea su cuenta |
| `mascota_registrada` | Se registra una mascota |
| `cita_agendada` | El cliente agenda una cita |
| `cita_estado` | El cliente o el veterinario confirma, completa o cancela una cita |
| `vacuna_registrada` | Se completa una cita de vacunación y se registra la vacuna |

Todos tienen el mismo formato: `evento`, `clave_app`, `enviado_en`, `app` y los objetos `usuario`, `mascota`, `servicio`, `cita` o `vacuna` en la raíz del JSON.

---

## 🐶 4. Capa de Datos Híbrida & Room ORM (Migración v2 ➔ v3)

* **Base de Datos**: `VeterinariaDatabase` (Versión 3).
* **Esquema de Tablas**:
  * `usuarios`: id, nombre, email, telefono, password, `rol`, `especialidad`, `licencia`, `latitud`, `longitud`, `consultorio`.
  * `mascotas`: id, usuarioId, nombre, especie, raza, sexo, fechaNacimiento, peso, foto.
  * `servicios`: id, nombre, descripcion, precio.
  * `citas`: id, mascotaId, servicioId, fecha, hora, estado, notas, `veterinarioId`.
  * `vacunas`: id, mascotaId, nombre, fechaAplicacion, proximaDosis.
  * `historial_medico`: id, mascotaId, veterinarioId, fecha, diagnostico, tratamiento, peso, notas.

* **Migración Automática `MIGRACION_2_3`**:
  Añade quirúrgicamente las nuevas columnas RBAC en la tabla `usuarios`, el puntero `veterinarioId` en `citas`, la tabla `historial_medico` y realiza el seeding del usuario `SUPER_ADMIN`.

---

## 📂 5. Estructura y Arquitectura del Código

El código está organizado bajo el paquete principal `com.AlexYChristian.ProyectoSis145` separado por módulos funcionales:

```
com.AlexYChristian.ProyectoSis145
├── data                      --> Entidades Room, DAOs, Migraciones y SessionManager
│   ├── dao
│   ├── entity
│   └── model
├── ui                        --> Capa de Interfaz de Usuario
│   ├── admin                 --> Panel de Administración y Registro de Vets
│   ├── auth                  --> Pantallas de Login y Registro
│   ├── citas                 --> Agendamiento y Lista de Citas
│   ├── maps                  --> Google Maps SDK y Map Picker
│   ├── mascotas              --> Mascotas, Ficha Médica e Historial Pediátrico
│   ├── menu                  --> Menú Principal Cliente
│   ├── servicios             --> Catálogo de Servicios
│   └── vet                   --> Panel del Veterinario
└── util                      --> Utilidades de Formato, Fotos y Estilos
```

---

## 🚀 6. Instrucciones de Compilación e Instalación

1. **Requisitos**:
   * Android Studio Ladybug / 2024.2+ o superior.
   * JDK 11 / Kotlin 2.x.
   * Dispositivo Android o Emulador con Android 7.0 (API 24) o superior.

2. **Ejecución desde Terminal**:
   ```bash
   ./gradlew app:assembleDebug
   ```

3. **Instalación Directa en Celular Conectado por USB**:
   ```bash
   ./gradlew app:installDebug
   adb shell am start -n com.AlexYChristian.ProyectoSis145/.MainActivity
   ```

---
*Desarrollado y optimizado por Luis Fernando.*
