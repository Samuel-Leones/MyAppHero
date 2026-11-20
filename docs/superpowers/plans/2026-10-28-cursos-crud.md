# Tres pantallas CRUD para cursos

Objetivo aprobado: conservar Curso/listado/detalle; añadir CursoCrearScreen,
CursoActualizarScreen y CursoEliminarScreen, cada una con ruta propia.
Sin ViewModel, repositorios, base de datos ni nuevas dependencias.

1. Escribir pruebas Compose de creación con validación/cancelación, edición por
   ID con estado y cancelación, eliminación con confirmación/cancelación,
   listado vacío y registro ausente. Ejecutarlas antes del cambio cuando haya dispositivo.
2. Mantener lista observable con remember en AppNavigation y contador de IDs
   creciente. Añadir completado=false a Curso. Datos solo en memoria, como antes.
3. Añadir tres archivos de pantalla, con Scaffold, campos/botones Material3 y
   rememberSaveable para el borrador. No modificar el original hasta guardar.
4. Conectar rutas con cursoId Int, buscar curso para cada destino, volver al detalle
   al actualizar/cancelar y limpiar hasta cursos al eliminar. Mensaje con regreso
   al listado para IDs ausentes.
5. Añadir botón crear, estado y vacío al listado; estado y acciones al detalle.
6. Compilar APK y tests, ejecutar pruebas disponibles, revisar archivos cambiados.

Revisión: espacios en blanco no válidos; IDs no reutilizados; formularios
independientes; Atrás después de eliminar no abre registro borrado; teclado y
pantalla pequeña permiten desplazarse. No convertir cursos en tareas.

## Ejecución
- Pruebas nuevas ejecutadas antes del cambio: 6 fallos esperados por botones/rutas ausentes.
- Implementadas tres pantallas, estado observable, validación y rutas por ID.
- Primera ejecución instrumentada: 7/7 correctas; APK y prueba unitaria correctos.
- Revisión independiente: sin fallos funcionales; se añadió aserción explícita de
  Atrás tras eliminar. Mutación temporal de limpieza de pila detectada por la prueba
  y restaurada; ejecución final correcta: 7/7 instrumentadas, prueba unitaria y APK.
- Prueba de Atrás sincronizada con runOnIdle: runOnUiThread puede adelantarse a
  las transiciones del NavHost; isFinishing no es una expectativa válida de salida
  en Android moderno.
- Se mantiene el almacenamiento solo en memoria y la estructura del proyecto.

- Revisión visual en Pixel_5: crear, actualizar con datos existentes y eliminar
  muestran correctamente sus formularios/confirmación; capturas en
  /tmp/myapphero-qa. Se cancelaron todas las acciones de la revisión visual.
