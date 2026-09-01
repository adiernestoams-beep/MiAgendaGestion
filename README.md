# Mi Agenda Gestión — Android

Aplicación Android diseñada a partir de los requerimientos definidos en la conversación y precargada con los dos documentos vigentes de la Gestión 2026.

## Funciones incluidas

- Tres vistas principales sincronizadas sobre una sola base de datos: **Calendario**, **Próximos** y **Prioridades**.
- Cualquier edición se refleja automáticamente en las tres vistas.
- Ficha editable por actividad: título, detalles, fecha inicial/final, hora, lugar, responsable, prioridad, estado, notas, fuente y recordatorio.
- Prioridad manual: **Urgente**, **Se acerca**, **Puede esperar**.
- Estado de la actividad: **Pendiente** (rojo) o **Completado** (verde).
- Estado general de **Tareas de preparación**: **Pendiente** (rojo), **Casi listo** (amarillo) o **Completado** (verde).
- Tareas específicas de preparación: cada una solo tiene **Pendiente** (rojo) o **Completado** (verde), sin porcentajes.
- Agregar, editar o eliminar tareas específicas.
- Agregar actividades manualmente en cualquier fecha.
- Gestión multiaño: 2026, 2027, 2028, etc.; las gestiones anteriores se conservan.
- Sección de elementos **Sin fecha / por organizar**.
- Historial, búsqueda y detección de coincidencias de fechas.
- Resumen de próximos pendientes y bloque **¿Qué hago ahora?**.
- Recordatorios locales configurables.
- Exportación/importación de respaldo JSON.
- Importación del formato JSON que ChatGPT puede generar a partir de nuevos PDF/Word/Excel/PPT.
- Sincronización con el calendario configurado en Android. Si el teléfono tiene una cuenta de Google con Calendar sincronizado, los eventos pasan a Google Calendar usando los permisos del dispositivo, sin pedir la contraseña de Google.

## Datos 2026 precargados

Se incluye `app/src/main/assets/seed_events.json`, generado a partir de:

1. `PAT ACADÉMICO 2026 OFICIAL ok ok(1).pdf`
2. `Aniversario UPeU Actividades.pdf`

La actividad **Decoración del campus** del segundo documento no trae fecha en la fuente, por lo que permanece en **Sin fecha / por organizar** en lugar de inventar una fecha.

## Flujo recomendado para 2027

1. Crear **Gestión 2027** desde la app.
2. Subir los PDF/Word/Excel/PPT a ChatGPT.
3. Pedir un JSON compatible usando `PROMPT_ACTUALIZAR_GESTION.txt`.
4. En la app: menú → **Importar gestión desde ChatGPT/JSON**.
5. Elegir **Agregar** o **Reemplazar** la gestión.
6. Revisar prioridades manualmente y sincronizar con Google Calendar.

La integración directa y automática entre una conversación de ChatGPT Plus y una APK independiente no está incluida porque requeriría una integración/API autorizada separada. El formato JSON evita guardar contraseñas o claves de API dentro de la app y permite actualizar la gestión sin rehacer la aplicación.

## Compilar en Android Studio

1. Abrir esta carpeta en Android Studio.
2. Instalar Android SDK 35 si Android Studio lo solicita.
3. Esperar la sincronización de Gradle.
4. `Build` → `Build APK(s)`.
5. Instalar el APK generado en el teléfono.

El proyecto usa únicamente APIs del sistema Android y no depende de librerías externas de interfaz.

## Compilación automática para TECNO MegaPad Pro 12

Se añadió `.github/workflows/build-apk.yml`. GitHub Actions compila una APK Debug usando Java 17, Gradle 8.10.2, Android SDK 35 y Build Tools 35.0.0. El APK generado se publica como artifact con el nombre `MiAgendaGestion-TecnoMegaPadPro12.apk`.

La app mantiene orientación libre y actividad redimensionable para aprovechar la pantalla grande de tablet. No se limita a un único modelo: seguirá funcionando en otros dispositivos Android compatibles con minSdk 26 o superior.

Consulta `COMPILAR_APK_EN_GITHUB.md` para los pasos exactos.
