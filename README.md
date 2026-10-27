# Proyecto SIS 145 · App Móvil

Prototipo de aplicación Android desarrollado en **Kotlin** con interfaces en **XML (Views)** para la materia **SIS 145**. Es un proyecto colaborativo que aplica buenas prácticas de desarrollo móvil, control de versiones con Git y trabajo en equipo mediante ramas y Pull Requests.

> 🚧 **Estado:** en desarrollo. La descripción de la funcionalidad y las pantallas se completará a medida que avance el prototipo.

---

## 📱 Descripción

_Por completar: qué problema resuelve la app, para quién es y qué puede hacer el usuario._

### Pantallas

| Pantalla | Descripción | Responsable | Estado |
|---|---|---|---|
| _Por definir_ | | | ⏳ Pendiente |

---

## 🛠️ Tecnologías

| Herramienta | Versión |
|---|---|
| Lenguaje | Kotlin |
| Interfaz | XML (Views) + ConstraintLayout |
| Diseño | Material Design 3 (`Theme.Material3.DayNight`) |
| Android Gradle Plugin | 9.4.1 |
| Gradle | 9.6.0 (Kotlin DSL) |
| SDK mínimo | API 24 (Android 7.0) |
| SDK objetivo / compilación | API 37 |
| Java | 11 |

---

## 📂 Estructura del proyecto

```
proyecto-sis145-app-movil/
├── app/
│   └── src/main/
│       ├── java/com/AlexYChristian/ProyectoSis145/   ← código Kotlin (Activities, lógica)
│       ├── res/
│       │   ├── layout/        ← pantallas en XML
│       │   ├── values/        ← colores, textos (strings) y temas
│       │   └── drawable/      ← íconos e imágenes
│       └── AndroidManifest.xml ← registro de Activities y permisos
├── gradle/libs.versions.toml   ← versiones de dependencias
└── build.gradle.kts
```

---

## 🚀 Cómo ejecutar el proyecto

1. Instalar la versión más reciente de **Android Studio**.
2. Clonar el repositorio desde Android Studio: **File → New → Project from Version Control** y pegar la URL:
   ```
   https://github.com/Alex-JC-dot/proyecto-sis145-app-movil
   ```
   O desde la terminal:
   ```bash
   git clone https://github.com/Alex-JC-dot/proyecto-sis145-app-movil.git
   ```
3. Esperar a que termine la sincronización de Gradle.
4. Elegir un emulador o un celular conectado y presionar **Run ▶**.

---

## 🤝 Flujo de trabajo en equipo

La rama `main` siempre debe tener una versión que **compile y funcione**. Nadie trabaja directamente en `main`.

### 1. Antes de empezar una tarea
```bash
git checkout main
git pull
git checkout -b feature/nombre-de-la-tarea
```

### 2. Nombres de ramas

| Prefijo | Uso | Ejemplo |
|---|---|---|
| `feature/` | Nueva funcionalidad o pantalla | `feature/pantalla-login` |
| `fix/` | Corrección de un error | `fix/boton-guardar` |
| `docs/` | Documentación | `docs/readme` |
| `refactor/` | Reorganizar código sin cambiar su comportamiento | `refactor/ordenar-paquetes` |
| `chore/` | Configuración y mantenimiento | `chore/actualizar-dependencias` |

Siempre en minúsculas, con guiones y **una rama por tarea**.

### 3. Mensajes de commit
Se sigue el formato [Conventional Commits](https://www.conventionalcommits.org/es/):

```
feat: agrega diseño de la pantalla de login
fix: corrige cierre de la app al guardar
docs: actualiza el README
```

### 4. Integrar los cambios
1. Subir la rama: `git push -u origin feature/nombre-de-la-tarea`
2. Abrir un **Pull Request** hacia `main` en GitHub.
3. El otro integrante revisa que compile y funcione, y lo aprueba.
4. Se hace **Merge** y se borra la rama.
5. Todos actualizan su `main` con `git pull`.

### 5. Archivos compartidos
Avisar al equipo antes de modificar archivos que usan todos, para evitar conflictos:
`AndroidManifest.xml`, `strings.xml`, `colors.xml`, `themes.xml`, `build.gradle.kts` y `libs.versions.toml`.

---

## 👥 Equipo

| Integrante | GitHub |
|---|---|
| Alex | [@Alex-JC-dot](https://github.com/Alex-JC-dot) |
| Christian | _por agregar_ |

---

Proyecto académico · SIS 145
