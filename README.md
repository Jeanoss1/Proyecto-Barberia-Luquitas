# Proyecto-Barberia-Luquitas
Aplicación móvil para la gestión de citas y servicios de barberías, desarrollada para el curso de Desarrollo de Aplicaciones Móviles.

Empezó como la app de **Barbería Luquitas** y ahora admite **varias barberías**: el cliente elige dónde atenderse y cada administrador gestiona solo la suya.

Kotlin · Jetpack Compose · Navigation Compose · Firebase Auth · Cloud Firestore.

## Requerimientos

| Entrega | Requerimiento | Dónde está |
|---|---|---|
| 1 | RF01 Registro de cliente | `ui/pantallas/auth/RegisterScreen.kt` |
| 1 | RF02 Inicio de sesión (+ RF03 recuperación) | `ui/pantallas/auth/LoginScreen.kt` |
| 1 | RF04 Catálogo de servicios | `ui/pantallas/cliente/ServiciosScreen.kt` |
| 2 | RF05 Disponibilidad de barberos | `ui/pantallas/cliente/SeleccionBarberoScreen.kt` |
| 2 | RF06 Reserva de cita (fecha y hora) | `ui/pantallas/cliente/SeleccionHorarioScreen.kt` |
| 2 | RF07 Confirmación de reserva | `ui/pantallas/cliente/ConfirmacionReservaScreen.kt` |
| 2 | RF08 Historial de citas · RF09 Cancelación | `ui/pantallas/cliente/MisCitasScreen.kt` |
| 2 | RF15 Agenda general | `ui/pantallas/admin/AgendaAdminScreen.kt` |
| 3 | RF11 Acceso protegido por rol | `ui/pantallas/admin/AccesoAdministrador.kt` + `firestore.rules` |
| 3 | RF12 Dashboard | `ui/pantallas/admin/AdminHomeScreen.kt` |
| 3 | RF13 Gestión de servicios | `ui/pantallas/admin/GestionServiciosScreen.kt`, `EditarServicioScreen.kt` |
| 3 | RF14 Gestión de barberos | `ui/pantallas/admin/GestionBarberosScreen.kt`, `EditarBarberoScreen.kt` |
| 3 | RF16 Gestión de estado de citas | `ui/pantallas/admin/AccionesEstadoCita.kt` (en la agenda) |
| 3 | RF17 Horarios de atención | `ui/pantallas/admin/HorarioAtencionScreen.kt` |
| 3 | RF19 Reporte de ingresos | `ui/pantallas/admin/ReporteIngresosScreen.kt` |
| + | Varias barberías | `ui/pantallas/cliente/ElegirBarberiaContenido.kt`, `data/repositorio/BarberiaRepository.kt` |

## Estructura del código

```
app/src/main/java/com/example/barberialuquitas/
├── MainActivity.kt              Solo arranca el tema y la navegación
├── data/
│   ├── modelo/                  Clases de datos (Barberia, Barbero, Servicio, Cita, Usuario) y constantes
│   ├── repositorio/             Acceso a Firebase: un repositorio por colección
│   │   ├── Conversiones.kt      Cómo se lee/guarda cada modelo en Firestore
│   │   └── FirestoreUtils.kt    Ayudas comunes (escuchar en tiempo real, manejo de errores)
│   └── local/                   SharedPreferences (barbería elegida por el cliente)
├── ui/
│   ├── navegacion/              Rutas.kt (nombres) y NavegacionApp.kt (todas las pantallas)
│   ├── componentes/             Piezas reutilizables: tarjetas, chips, formularios, estados de carga
│   ├── pantallas/
│   │   ├── auth/                Login, registro
│   │   ├── cliente/             Flujo del cliente
│   │   └── admin/               Flujo del administrador
│   └── theme/                   Colores y tipografía
└── util/                        Lógica pura y probada: horarios, reportes, validaciones
```

Reglas que sigue el código:
- **Las pantallas no hablan con Firebase directamente**: llaman a un repositorio, que devuelve `Result` (o un `Flow<Result>` en tiempo real).
- **Los cálculos van en `util/`**: horarios, disponibilidad y reportes son funciones puras con pruebas en `app/src/test`.
- **Cargas con un solo patrón**: `ContenidoSegunResultado` muestra cargando, error con "Reintentar", vacío o contenido.
- **Colores solo del tema** (`ui/theme/Color.kt`).

## Modelo de datos (Firestore)

| Colección | Campos principales |
|---|---|
| `barberias` | `nombre`, `direccion`, `telefono`, `fotoUrl`, `activa`, `horaApertura`, `horaCierre`, `diasLaborables` (1 = lunes … 7 = domingo), `diasNoLaborables` (`yyyy-MM-dd`), `creadaPor`, `migrada` |
| `usuarios` | `nombre`, `correo`, `telefono`, `rol` (`cliente`/`administrador`), `barberiaId` (solo administradores) |
| `servicios` | `barberiaId`, `nombre`, `descripcion`, `precio`, `duracionMinutos`, `fotoUrl`, `activo` |
| `barberos` | `barberiaId`, `nombre`, `especialidad`, `fotoUrl`, `activo`, `horaInicio`, `horaFin`, `diasLaborables` |
| `citas` | `barberiaId`, `barberiaNombre`, cliente, servicio, barbero, `fecha`, `hora`, `bloques`, `precio`, `estado` |
| `horarios_ocupados` | Un "candado" por bloque de 30 min (`barberoId_fecha_hora`) para que nadie reserve dos veces el mismo horario |

Estados de una cita: `reservada` → `atendida` / `no_asistio` / `cancelada`.
- Las citas `completada` de la segunda entrega cuentan como atendidas.
- Una cita reservada que ya terminó se muestra como **"Por confirmar"** hasta que el administrador la marque.

Se reserva solo dentro del **horario efectivo**, es decir, la parte común entre:
- el horario de la barbería (RF17),
- el horario del barbero,
- y quitando los días no laborables de la barbería.

Se calcula en `util/HorarioAtencion.kt` y Firestore lo vuelve a validar.

## Puesta en marcha

1. Abre el proyecto en Android Studio y coloca `app/google-services.json` (no se sube al repositorio).
2. **Publica las reglas y el índice de Firestore** (obligatorio para esta versión):
   - Con Firebase CLI: `firebase deploy --only firestore` desde la raíz del proyecto.
   - O desde la consola de Firebase:
     - Pega `firestore.rules` en *Firestore → Reglas*.
     - Crea el índice compuesto `citas`: `barberiaId` ↑ + `fecha` ↑. Si falta, el reporte muestra el error y Logcat trae el enlace para crearlo.
3. **Migración (solo una vez)**: entra con la cuenta de administrador actual de Luquitas.
   - La app pide registrar la barbería; esa queda como `luquitas`.
   - Al abrir el panel, los servicios, barberos y citas antiguos pasan a esa barbería automáticamente.
   - **Mientras esto no se haga, los clientes no ven barberías para reservar.**
4. **Nueva barbería con su administrador**:
   - El dueño se registra en la app como cliente.
   - En la consola de Firebase se cambia su `usuarios/{uid}.rol` a `administrador`.
   - Al iniciar sesión, la app le pide registrar su barbería.
   - Después configura su horario, servicios y barberos.

## Pruebas

```
./gradlew testDebugUnitTest
```

Cubren bloques de horario, horario efectivo barbería/barbero, días no laborables, "abierto ahora", estados visibles y cálculos del reporte.
