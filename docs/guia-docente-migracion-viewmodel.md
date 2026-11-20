# Guía docente en vivo: del CRUD de cursos a ViewModel

## Cómo usar esta guía

Este documento es un **guion para el docente**: incluye qué decir, qué escribir,
qué preguntar y qué comprobar. Parte del MyAppHero que ya tenemos; no cambia
cursos por tareas ni crea otra aplicación.

**Resultado:** las cinco pantallas permanecen independientes. La lista, los IDs
y las operaciones crear/consultar/actualizar/eliminar pasan a un `CursosViewModel`
compartido. La navegación y los borradores de los formularios conservan su función.

**No incluimos:** StateFlow, corrutinas, Room, Retrofit, Hilt, repositorios,
factories personalizadas ni un ViewModel por pantalla. Queremos comprender el
cambio de responsabilidad antes de añadir más herramientas.

> El proyecto original NO se migra al crear este documento. Tú aplicarás el código
> durante la clase. Los bloques «Archivo completo» incluyen todos los imports y
> son la versión final. Los bloques pequeños son fragmentos para explicar o insertar.

### Orden de trabajo

| Paso | Qué haces | Cuándo comprobar |
|---|---|---|
| 0 | Mostrar dónde viven actualmente los datos | Ejecutar la app de partida |
| 1 | Explicar qué hace ViewModel | Preguntar antes de escribir |
| 2 | Agregar la dependencia de Compose | Sincronizar Gradle |
| 3 | Construir el ViewModel por partes | Compilar aunque aún no esté conectado |
| 4 | Conectar MainActivity y AppNavigation | Cambiar ambos antes de ejecutar |
| 5 | Seguir el recorrido de un dato y probar rotación | Ejecutar el CRUD |
| 6 | Consultar los archivos completos de las pantallas | No hace falta reescribirlos |
| 7 | Comprobar recorridos y cerrar con preguntas | Práctica manual |

Accesos rápidos: [Inicio](#paso-0) · [Concepto](#paso-1) · [Gradle](#paso-2) · [ViewModel](#paso-3) · [Conexión](#paso-4) · [Demostración](#paso-5) · [Pantallas completas](#paso-6) · [Cierre](#paso-7).

No empieces pegando todos los archivos a la vez. Usa los fragmentos para construir
el ViewModel y luego los archivos completos para comprobar que no faltó nada.

---

<a id="paso-0"></a>

## 0. Mostrar el problema antes de dar la solución

### Qué decir

> «La aplicación ya funciona. No vamos a cambiar sus pantallas. Vamos a separar
> responsabilidades: ahora el archivo que decide a dónde navegar también guarda
> y modifica nuestros cursos».

Abre el archivo de navegación:
`/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/navigation/AppNavigation.kt`.

Localiza la lista y el contador actuales:

```kotlin
val listaCursos = remember { mutableStateListOf<Curso>().apply { addAll(cursos) } }
var siguienteId by remember { mutableStateOf((cursos.maxOfOrNull { it.id } ?: 0) + 1) }
```

Busca también `listaCursos.add`, `listaCursos[indice]` y `listaCursos.removeAll`.
Esas instrucciones modifican registros: no son instrucciones para cambiar de pantalla.

### Demostración inicial

1. Ejecuta la aplicación de partida.
2. Crea «Prueba de rotación» y abre su detalle.
3. Pregunta: **«Si Android vuelve a crear la actividad, ¿seguirá el curso?»**
4. Rota con los controles del emulador; habilita la rotación automática si hace falta.

Si la actividad se recrea, la lista de partida vuelve a los cursos iniciales.
El detalle del curso nuevo puede mostrar «Curso no encontrado». Si no se recrea
la actividad, la demostración no reproduce ese caso: no concluyas que `remember`
ya conserva los datos ante recreación.

### Dos conceptos que no debemos confundir

- **Recomposición:** Compose vuelve a ejecutar partes de la interfaz al cambiar estado.
- **Recreación de Activity:** Android destruye una instancia y crea otra; puede suceder
  ante un cambio de configuración, como rotar el dispositivo.

`remember` conserva un valor entre recomposiciones. No lo conserva por sí solo
cuando la actividad que contiene esa composición se destruye y se crea de nuevo.

---

<a id="paso-1"></a>

## 1. Explicar ViewModel con el caso que ya conocen

### Situación cotidiana

> «Pensemos en una oficina. Las pantallas son las ventanillas y los cursos son
> los registros. Cambiar de ventanilla no debería significar crear otra colección
> de registros. Necesitamos un responsable compartido al que todas consulten».

### Definición para decir en clase

> «Un ViewModel mantiene el estado que necesita la interfaz y contiene las
> operaciones relacionadas con ese estado. No dibuja botones y no decide qué
> pantalla abrir».

Conserva su estado durante los cambios de configuración de su propietario. En
nuestro caso ese propietario será MainActivity. No es una base de datos y no
recupera automáticamente los cursos si termina el proceso de la aplicación.
[Fundamento oficial de ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel).

### Reparto de responsabilidades en este ejercicio

| Parte | Trabajo |
|---|---|
| `Curso` | Representar ID, título, descripción y estado |
| `CursosViewModel` | Mantener registros, asignar IDs, buscar y hacer CRUD con validación |
| `AppNavigation` | Leer argumentos, conectar datos y callbacks, navegar |
| Listado y detalle | Mostrar datos y emitir acciones |
| Formularios de crear y actualizar | Mantener el borrador y mostrar errores |
| Pantalla de eliminar | Mostrar confirmación y emitir confirmar o cancelar |

### Pregunta antes de avanzar

**«Si cancelo la actualización, ¿lo que escribí ya debería haber reemplazado el título?»**

Respuesta: no. El formulario es un borrador; el registro cambia cuando guardamos.
Por eso **no moveremos todos los `rememberSaveable` al ViewModel**.

---

<a id="paso-2"></a>

## 2. Agregar la dependencia, sin actualizar todo el proyecto

La función `viewModel()` de Compose se obtiene de `lifecycle-viewmodel-compose`.
Usaremos el catálogo existente y su referencia `lifecycleRuntimeKtx`, actualmente
`2.11.0`. Es la versión usada por este proyecto, no una recomendación de actualizar.

### 2.1. Catálogo de versiones

Abre `/Users/djruiz/AndroidStudioProjects/MyAppHero/gradle/libs.versions.toml`.
Dentro de `[libraries]` añade esta entrada; no crees otra sección con ese nombre:

```toml
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
```

### 2.2. Dependencias del módulo app

Abre `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/build.gradle.kts`.
Dentro de `dependencies` añade:

```kotlin
implementation(libs.androidx.lifecycle.viewmodel.compose)
```

### Qué decir

> «Esta dependencia permite obtener el ViewModel correctamente desde Compose.
> No contiene la lógica de nuestros cursos: esa clase la escribimos nosotros».

Pulsa **Sync Now** y espera a que termine. Si no resuelve la dependencia, revisa
el alias, la conexión y los repositorios antes de cambiar otras versiones.

**Pausa de comprobación:** la app todavía debe comportarse igual. No movimos datos.
Los dos archivos completos de Gradle están en el anexo A.

---

<a id="paso-3"></a>

## 3. Construir CursosViewModel paso a paso

Crea el paquete `viewmodel` dentro de `co.edu.iub.myapphero` y el archivo
`CursosViewModel.kt`. La ruta final completa aparece en la sección 3.6.

### 3.1. Primero, una clase vacía

Escribe esto para comenzar; puede compilar por sí solo:

```kotlin
package co.edu.iub.myapphero.viewmodel

import androidx.lifecycle.ViewModel

class CursosViewModel : ViewModel() {
}
```

### Explicar al escribir

- `class` declara una clase; `CursosViewModel` es el nombre que elegimos.
- `: ViewModel()` indica que hereda de ViewModel.
- No usamos `@Composable`: esta clase no dibuja.
- No guardaremos aquí un `NavController`, una Activity ni componentes visuales.

**Pregunta:** «¿Ya cambió la app por crear esta clase?» No: todavía nadie la usa.

### 3.2. Añadir estado observable y búsqueda

Añade estos imports:

```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.edu.iub.myapphero.model.Curso
```

Dentro de la clase empieza con una sola fila, para entender la forma:

```kotlin
var cursos by mutableStateOf(
    listOf(Curso(1, "Android con Kotlin", "Desarrollo de aplicaciones móviles."))
)
    private set

private var siguienteId = (cursos.maxOfOrNull { it.id } ?: 0) + 1

fun buscarCurso(cursoId: Int): Curso? {
    return cursos.find { it.id == cursoId }
}
```

Este ejemplo de una fila es **temporal**. El archivo completo del paso 3.6 contiene
los siete cursos originales que trasladaremos desde MainActivity.

### Explicación de la lista

- `listOf(...)` construye una lista que exponemos para lectura.
- `mutableStateOf(...)` hace observable el valor para Compose.
- `by` es delegación de propiedades: permite usar `cursos` en lugar de `.value`.
- Los imports `getValue` y `setValue` permiten usar ese delegado.
- `var` permite asignar una lista nueva a la propiedad.
- `private set` deja leer `cursos` desde fuera, pero asignarla solo dentro de la clase.

Antes teníamos `remember { mutableStateOf(...) }`. Aquí no escribimos `remember`:
el objeto ViewModel conserva la propiedad mientras viva su propietario.

> «El ViewModel conserva el dato; `mutableStateOf` permite que Compose observe el
> cambio. No son la misma función».

### Explicación del ID y la búsqueda

- `maxOfOrNull { it.id }` obtiene el mayor ID, o null si la lista está vacía.
- `?: 0` usa cero si el resultado es null; luego sumamos uno.
- El contador es privado: la pantalla no puede cambiarlo.
- No necesita estado observable porque la interfaz no muestra ese contador interno.
- `Curso?` permite devolver un curso o null si no existe.
- En `find`, `it` representa cada curso revisado.

**Predicción:** «¿Qué devuelve buscar el ID 999?» Null, si no hay un registro con ese ID.

**Pausa:** compila la clase. La app seguirá usando su lista antigua hasta conectarla.

### 3.3. Crear un curso

Añade este método **dentro de la clase**, antes de su última llave:

```kotlin
fun crearCurso(titulo: String, descripcion: String): Boolean {
    if (titulo.isBlank() || descripcion.isBlank()) {
        return false
    }
    val nuevoCurso = Curso(
        id = siguienteId,
        titulo = titulo.trim(),
        descripcion = descripcion.trim()
    )
    cursos = cursos + nuevoCurso
    siguienteId++
    return true
}
```

### Qué decir mientras lo escribes

> «Recibimos los textos. Si no sirven, terminamos sin agregar nada. Si sirven,
> construimos el registro, publicamos otra lista y preparamos el siguiente ID».

- `isBlank()` también rechaza textos que solo tienen espacios.
- `false` significa que no se realizó la operación; no se consume un ID.
- `trim()` retira espacios al principio y al final de un dato válido.
- No enviamos estado: `Curso` tiene `completado = false` por defecto.
- `cursos + nuevoCurso` produce otra lista. **Hay que asignarla** a `cursos`.
- Esa asignación cambia el estado observable que lee Compose.
- `siguienteId++` aumenta el contador. No usamos `cursos.size + 1`, que podría repetir IDs tras eliminar.
- `true` significa «se creó». El ViewModel no navega por devolver true.

**Pregunta:** «Si borro el último registro, ¿se reinicia el contador?» No, mientras
sigamos usando este mismo ViewModel.

### 3.4. Actualizar un curso por su ID

Añade este método dentro de la misma clase:

```kotlin
fun actualizarCurso(
    cursoId: Int,
    titulo: String,
    descripcion: String,
    completado: Boolean
): Boolean {
    if (titulo.isBlank() || descripcion.isBlank()) {
        return false
    }
    val indice = cursos.indexOfFirst { it.id == cursoId }
    if (indice == -1) {
        return false
    }
    val nuevaLista = cursos.toMutableList()
    nuevaLista[indice] = cursos[indice].copy(
        titulo = titulo.trim(),
        descripcion = descripcion.trim(),
        completado = completado
    )
    cursos = nuevaLista
    return true
}
```

### Explicarlo despacio

1. Validamos los textos antes de modificar registros.
2. `indexOfFirst` obtiene la posición del curso cuyo ID coincide.
3. `-1` significa «no existe»: devolvemos false sin cambiar la lista.
4. `toMutableList()` prepara una copia editable de la lista actual.
5. `copy(...)` crea otro Curso con los cambios, conservando su ID.
6. Reemplazamos una sola posición de la copia.
7. Asignamos la nueva lista a la propiedad observable.

**Pregunta:** «¿El curso de ID 12 tiene que estar en la posición 12?» No. Un ID
identifica un registro; un índice es la posición y puede cambiar al eliminar filas.

### 3.5. Eliminar por ID

Añade este último método:

```kotlin
fun eliminarCurso(cursoId: Int): Boolean {
    if (buscarCurso(cursoId) == null) {
        return false
    }
    cursos = cursos.filter { it.id != cursoId }
    return true
}
```

### Qué decir

> «Eliminar significa conservar los cursos cuyo ID sea distinto del que recibimos.
> No significa quitar el primero o el último de la lista».

`filter` construye una lista con los elementos que cumplen su condición. Usamos
`!=` para dejar fuera el registro elegido. Si no existe, devolvemos false.

**Predicción:** «¿Qué pasaría si usáramos `==`?» Conservaríamos justo el que
queríamos eliminar. No dejes ese cambio en la versión final.

### 3.6. Archivo completo: comprobar lo que acabamos de construir

Este bloque reemplaza el ejemplo temporal de una fila. Traslada los **siete cursos
originales**. No dejes dos propiedades `cursos` ni dos contadores al unir fragmentos.

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/viewmodel/CursosViewModel.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/viewmodel/CursosViewModel.kt -->
```kotlin
package co.edu.iub.myapphero.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import co.edu.iub.myapphero.model.Curso

class CursosViewModel : ViewModel() {
    var cursos by mutableStateOf(listOf(
        Curso(
            id = 1,
            titulo = "Android con Kotlin",
            descripcion = "Desarrollo de aplicaciones móviles."
        ),
        Curso(
            id = 2,
            titulo = "Spring Boot",
            descripcion = "Desarrollo de APIs REST."
        ),
        Curso(
            id = 3,
            titulo = "Angular",
            descripcion = "Desarrollo de aplicaciones web."
        ),
        Curso(
            id = 4,
            titulo = "Base de datos",
            descripcion = "Fundamentos de MySQL"
        ),
        Curso(
            id = 5,
            titulo = "Programación Web",
            descripcion = "Fundamentos de html, css  & js"
        ),
        Curso(
            id = 6,
            titulo = "Programación Web",
            descripcion = "Fundamentos de html, css  & js"
        ),
        Curso(
            id = 7,
            titulo = "Programación Web",
            descripcion = "Fundamentos de html, css  & js"
        )
    ))
        private set

    private var siguienteId = (cursos.maxOfOrNull { it.id } ?: 0) + 1

    fun buscarCurso(cursoId: Int): Curso? {
        return cursos.find { it.id == cursoId }
    }

    fun crearCurso(titulo: String, descripcion: String): Boolean {
        if (titulo.isBlank() || descripcion.isBlank()) {
            return false
        }
        val nuevoCurso = Curso(
            id = siguienteId,
            titulo = titulo.trim(),
            descripcion = descripcion.trim()
        )
        cursos = cursos + nuevoCurso
        siguienteId++
        return true
    }

    fun actualizarCurso(
        cursoId: Int,
        titulo: String,
        descripcion: String,
        completado: Boolean
    ): Boolean {
        if (titulo.isBlank() || descripcion.isBlank()) {
            return false
        }
        val indice = cursos.indexOfFirst { it.id == cursoId }
        if (indice == -1) {
            return false
        }
        val nuevaLista = cursos.toMutableList()
        nuevaLista[indice] = cursos[indice].copy(
            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            completado = completado
        )
        cursos = nuevaLista
        return true
    }

    fun eliminarCurso(cursoId: Int): Boolean {
        if (buscarCurso(cursoId) == null) {
            return false
        }
        cursos = cursos.filter { it.id != cursoId }
        return true
    }
}
```

**Pausa de compilación:** esta clase ya está completa y puede compilar sin estar
conectada. Crear el archivo no redirige automáticamente las operaciones antiguas.

---

<a id="paso-4"></a>

## 4. Conectar una misma instancia a las cinco pantallas

Este paso tiene dos cambios relacionados. **Cambia MainActivity y AppNavigation
antes de ejecutar**. Si solo cambias uno, puede haber un error de parámetros hasta
terminar el otro. Es un punto intermedio esperado, no un nuevo problema de navegación.

### 4.1. MainActivity obtiene y comparte el ViewModel

### Qué decir

> «No construiremos un ViewModel en cada pantalla. Pediremos una instancia al
> mecanismo de Android y compartiremos esa misma instancia con toda la navegación».

La línea clave es:

```kotlin
val cursosViewModel: CursosViewModel = viewModel()
```

El import correcto es `androidx.lifecycle.viewmodel.compose.viewModel`.
`viewModel()` obtiene la instancia existente o crea la del propietario correspondiente.
Aquí se llama **por encima del NavHost**, en el contenido de MainActivity, por lo
que el propietario es la actividad.

No reemplaces esa línea por `CursosViewModel()` ni por `remember { CursosViewModel() }`.
Eso construye un objeto sin obtenerlo del almacén de ViewModel de la actividad.
Tampoco lo pidas por separado dentro de cada destino esperando compartirlo: los
destinos pueden tener propietarios diferentes.
[Integración oficial de Compose y ViewModel](https://developer.android.com/develop/ui/compose/libraries#viewmodel).

La lista original desaparece de MainActivity porque ya está en el ViewModel.
Este archivo también retira el ejemplo que no usábamos de Greeting/GreetingPreview.

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/MainActivity.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/MainActivity.kt -->
```kotlin
package co.edu.iub.myapphero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.iub.myapphero.navigation.AppNavigation
import co.edu.iub.myapphero.ui.theme.MyAppHeroTheme
import co.edu.iub.myapphero.viewmodel.CursosViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyAppHeroTheme {
                val cursosViewModel: CursosViewModel = viewModel()
                AppNavigation(cursosViewModel = cursosViewModel)
            }
        }
    }
}
```

### 4.2. AppNavigation conecta eventos, no guarda los registros

Antes de pegar el archivo completo, explica los reemplazos:

| Antes | Ahora |
|---|---|
| Recibir `List<Curso>` | Recibir `CursosViewModel` |
| Declarar lista y contador con `remember` | Eliminar esos dos estados de navegación |
| Leer `listaCursos` | Leer `cursosViewModel.cursos` |
| Buscar en la lista | Llamar a `buscarCurso(cursoId)` |
| Agregar/reemplazar/eliminar directamente | Llamar a las operaciones del ViewModel |

No cambian las cinco rutas ni las cinco pantallas. Seguimos pasando el ID, no el
objeto entero. Para un argumento ausente usamos `-1` y la búsqueda no encuentra registro:

```kotlin
val cursoId = backStackEntry.arguments?.getInt("cursoId") ?: -1
val curso = cursosViewModel.buscarCurso(cursoId)
```

Las rutas mantienen `NavType.IntType`. Si el curso no existe se muestra un mensaje
y un botón para volver al listado.

### Guardar y después navegar

```kotlin
val guardado = cursosViewModel.crearCurso(titulo, descripcion)
if (guardado) {
    navController.popBackStack("cursos", false)
}
```

El ViewModel devuelve si se realizó la operación. **AppNavigation** decide volver.
Si la operación falla, no navegamos simulando éxito.

### Cancelar y eliminar no son la misma acción

Cancelar solo hace `popBackStack()`: no llama al CRUD.
Al confirmar eliminación conservamos `popBackStack("cursos", false)`, que retira
tanto la confirmación como el detalle del registro borrado. Un solo pop volvería
al detalle eliminado. El ViewModel no arregla automáticamente la pila de navegación.

### Archivo completo de navegación

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/navigation/AppNavigation.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/navigation/AppNavigation.kt -->
```kotlin
package co.edu.iub.myapphero.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.iub.myapphero.viewmodel.CursosViewModel
import co.edu.iub.myapphero.ui.screens.CursoActualizarScreen
import co.edu.iub.myapphero.ui.screens.CursoCrearScreen
import co.edu.iub.myapphero.ui.screens.CursoDetalleScreen
import co.edu.iub.myapphero.ui.screens.CursoEliminarScreen
import co.edu.iub.myapphero.ui.screens.CursoScreen

@Composable
fun AppNavigation(cursosViewModel: CursosViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "cursos") {
        composable("cursos") {
            CursoScreen(
                cursos = cursosViewModel.cursos,
                onCursoClick = { cursoId -> navController.navigate("curso_detalle/$cursoId") },
                onCrearClick = { navController.navigate("curso_crear") }
            )
        }

        composable("curso_crear") {
            CursoCrearScreen(
                onGuardar = { titulo, descripcion ->
                    val guardado = cursosViewModel.crearCurso(titulo, descripcion)
                    if (guardado) {
                        navController.popBackStack("cursos", false)
                    }
                },
                onCancelar = { navController.popBackStack() }
            )
        }

        composable(
            route = "curso_detalle/{cursoId}",
            arguments = listOf(navArgument("cursoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val cursoId = backStackEntry.arguments?.getInt("cursoId") ?: -1
            val curso = cursosViewModel.buscarCurso(cursoId)

            if (curso != null) {
                CursoDetalleScreen(
                    curso = curso,
                    onBack = { navController.popBackStack("cursos", false) },
                    onActualizarClick = { navController.navigate("curso_actualizar/${curso.id}") },
                    onEliminarClick = { navController.navigate("curso_eliminar/${curso.id}") }
                )
            } else {
                CursoNoEncontrado(onVolver = { navController.popBackStack("cursos", false) })
            }
        }

        composable(
            route = "curso_actualizar/{cursoId}",
            arguments = listOf(navArgument("cursoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val cursoId = backStackEntry.arguments?.getInt("cursoId") ?: -1
            val curso = cursosViewModel.buscarCurso(cursoId)

            if (curso != null) {
                CursoActualizarScreen(
                    curso = curso,
                    onGuardar = { titulo, descripcion, completado ->
                        val guardado = cursosViewModel.actualizarCurso(
                            cursoId = cursoId,
                            titulo = titulo,
                            descripcion = descripcion,
                            completado = completado
                        )
                        if (guardado) {
                            navController.popBackStack()
                        }
                    },
                    onCancelar = { navController.popBackStack() }
                )
            } else {
                CursoNoEncontrado(onVolver = { navController.popBackStack("cursos", false) })
            }
        }

        composable(
            route = "curso_eliminar/{cursoId}",
            arguments = listOf(navArgument("cursoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val cursoId = backStackEntry.arguments?.getInt("cursoId") ?: -1
            val curso = cursosViewModel.buscarCurso(cursoId)

            if (curso != null) {
                CursoEliminarScreen(
                    curso = curso,
                    onConfirmar = {
                        val eliminado = cursosViewModel.eliminarCurso(cursoId)
                        if (eliminado) {
                            // Retirar también el detalle del curso eliminado.
                            navController.popBackStack("cursos", false)
                        }
                    },
                    onCancelar = { navController.popBackStack() }
                )
            } else {
                CursoNoEncontrado(onVolver = { navController.popBackStack("cursos", false) })
            }
        }
    }
}

@Composable
private fun CursoNoEncontrado(onVolver: () -> Unit) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Curso no encontrado")
            Button(onClick = onVolver) { Text("Volver al listado") }
        }
    }
}
```

**Ahora sí ejecuta:** ambos archivos ya usan el mismo contrato. La app debe verse
igual. Cambiamos el dueño de los datos, no el diseño de la interfaz.

Prueba crear una fila, actualizar su estado y eliminarla. El resultado debe
reflejarse en el listado y detalle del ID correspondiente.

---

<a id="paso-5"></a>

## 5. Seguir el recorrido de un dato y demostrar la rotación

### Dibujar o leer este recorrido

```text
Usuario escribe título y descripción
            ↓
CursoCrearScreen mantiene un borrador local
            ↓ pulsa Guardar
Valida visualmente y emite onGuardar(titulo, descripcion)
            ↓
AppNavigation llama a crearCurso(...)
            ↓
CursosViewModel valida y asigna otra lista a cursos
            ↓
Compose detecta el cambio donde se lee ese estado
            ↓
La navegación vuelve al listado y CursoScreen recibe los cursos actuales
```

### Qué decir

> «El ViewModel no llama a CursoScreen ni busca botones para cambiar su texto.
> Cambia el estado. Compose vuelve a ejecutar lo necesario y la interfaz muestra
> los valores actuales».

No hay `collectAsState()` porque esta versión no usa Flow. La observación se basa
en `mutableStateOf`, que ya conocemos.

### ¿Por qué dejamos el borrador en las pantallas?

El formulario de actualización conserva estas variables:

```kotlin
var titulo by rememberSaveable(curso.id) { mutableStateOf(curso.titulo) }
var descripcion by rememberSaveable(curso.id) { mutableStateOf(curso.descripcion) }
var completado by rememberSaveable(curso.id) { mutableStateOf(curso.completado) }
```

Son el borrador, no los registros compartidos. Al escribir no llamamos a actualizar.
Al guardar enviamos los valores. Al cancelar salimos sin aplicarlos.
`rememberSaveable` permite restaurar ese estado local cuando Android puede guardar
y restaurar la instancia; no convierte el catálogo en almacenamiento permanente.

### ¿Por qué se valida dos veces?

- En la pantalla, para decidir cuándo y dónde mostrar los errores del formulario.
- En el ViewModel, para proteger el CRUD aunque otro código lo llame con datos inválidos.

Conservamos los mensajes visuales existentes. No añadimos un sistema nuevo de
errores para explicar esta primera migración.

### ¿Hace falta una clase llamada UiState?

No para este primer paso: `cursos` ya es estado que consume la interfaz. Después
podemos agrupar datos relacionados si el caso lo necesita. Hoy no inventaremos
carga, error de red ni corrutinas para datos que están en memoria.
[Ubicación del estado compartido y local](https://developer.android.com/develop/ui/compose/state-hoisting).

### Demostración después de migrar

1. Crea un curso de prueba y anota su ID.
2. Abre su detalle, rota y comprueba que sigue existiendo con el mismo ID.
3. Actualízalo, marca Completada, guarda y rota otra vez.
4. Comprueba los cambios en el detalle y listado.
5. Elimínalo, vuelve al listado y rota: no debe reaparecer.

Esto comprueba un cambio de configuración conservando el ViewModel de la actividad.
No lo confundas con terminar el proceso o iniciar otra sesión de la aplicación.

| Situación | Esta versión |
|---|---|
| Recomposición | Conserva y observa estado |
| Navegación entre nuestras cinco rutas | Comparte los mismos registros |
| Recreación por configuración de la actividad | Conserva el ViewModel del propietario |
| Muerte del proceso | No recupera automáticamente los registros en memoria |
| Forzar detención o crear una actividad con un ViewModel nuevo | Parte de los datos iniciales |

No prometas «ya guarda para siempre». Eso requerirá otra solución de persistencia,
fuera del objetivo de esta clase.

---

<a id="paso-6"></a>

## 6. Código completo que se conserva: modelo, tarjeta y cinco pantallas

**Estos archivos no necesitan cambios para la migración.** Se incluyen completos
para consultar todo desde el mismo Markdown. Durante la clase basta abrirlos y
mostrar sus datos/callbacks; no es necesario volver a escribirlos.

El tema, recursos, manifiesto y archivos raíz de Gradle siguen siendo los del
proyecto existente. Esta guía no es un proyecto nuevo desde cero.

### 6.1. Modelo Curso

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/model/Curso.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/model/Curso.kt -->
```kotlin
package co.edu.iub.myapphero.model

data class Curso(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val completado: Boolean = false
)
```

### 6.2. Tarjeta reutilizable

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/components/CursoCard.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/components/CursoCard.kt -->
```kotlin
package co.edu.iub.myapphero.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.iub.myapphero.model.Curso

@Composable
fun CursoCard(
    curso: Curso,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = curso.id.toString(),
            )
            Text(
                text = curso.titulo,
            )
            Text(
                text = curso.descripcion
            )
            Text(
                text = if (curso.completado) "Estado: Completada" else "Estado: Pendiente"
            )
        }
    }
}
```

### 6.3. Listado

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoScreen.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoScreen.kt -->
```kotlin
package co.edu.iub.myapphero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.iub.myapphero.model.Curso
import co.edu.iub.myapphero.ui.components.CursoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoScreen(
    cursos: List<Curso>,
    onCursoClick: (Int) -> Unit,
    onCrearClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Cursos") }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(onClick = onCrearClick) { Text("Crear curso") }
            }
            if (cursos.isEmpty()) {
                item { Text("No hay cursos registrados") }
            }
            items(items = cursos, key = { curso -> curso.id }) { curso ->
                CursoCard(curso = curso, onClick = { onCursoClick(curso.id) })
            }
        }
    }
}
```

### 6.4. Detalle

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoDetalleScreen.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoDetalleScreen.kt -->
```kotlin
package co.edu.iub.myapphero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.iub.myapphero.model.Curso

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoDetalleScreen(
    curso: Curso,
    onBack: () -> Unit,
    onActualizarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle del curso") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Identificador: ${curso.id}")
            Text(curso.titulo)
            Text(curso.descripcion)
            Text(if (curso.completado) "Estado: Completada" else "Estado: Pendiente")
            Button(onClick = onActualizarClick) { Text("Actualizar curso") }
            Button(onClick = onEliminarClick) { Text("Eliminar curso") }
            OutlinedButton(onClick = onBack) { Text("Volver al listado") }
        }
    }
}
```

### 6.5. Creación: vista independiente

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoCrearScreen.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoCrearScreen.kt -->
```kotlin
package co.edu.iub.myapphero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoCrearScreen(
    onGuardar: (String, String) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var mostrarErrores by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Crear curso") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                singleLine = true,
                isError = mostrarErrores && titulo.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (mostrarErrores && titulo.isBlank()) {
                Text("El título es obligatorio")
            }
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                isError = mostrarErrores && descripcion.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (mostrarErrores && descripcion.isBlank()) {
                Text("La descripción es obligatoria")
            }
            Text("Estado inicial: Pendiente")
            Button(onClick = {
                mostrarErrores = true
                if (titulo.isNotBlank() && descripcion.isNotBlank()) {
                    onGuardar(titulo.trim(), descripcion.trim())
                }
            }) { Text("Guardar") }
            OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
        }
    }
}
```

### 6.6. Actualización: vista independiente

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoActualizarScreen.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoActualizarScreen.kt -->
```kotlin
package co.edu.iub.myapphero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.iub.myapphero.model.Curso

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoActualizarScreen(
    curso: Curso,
    onGuardar: (String, String, Boolean) -> Unit,
    onCancelar: () -> Unit
) {
    // El formulario usa un borrador: cancelar no modifica el curso original.
    var titulo by rememberSaveable(curso.id) { mutableStateOf(curso.titulo) }
    var descripcion by rememberSaveable(curso.id) { mutableStateOf(curso.descripcion) }
    var completado by rememberSaveable(curso.id) { mutableStateOf(curso.completado) }
    var mostrarErrores by rememberSaveable(curso.id) { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Actualizar curso") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Identificador: ${curso.id}")
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                singleLine = true,
                isError = mostrarErrores && titulo.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (mostrarErrores && titulo.isBlank()) {
                Text("El título es obligatorio")
            }
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                isError = mostrarErrores && descripcion.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (mostrarErrores && descripcion.isBlank()) {
                Text("La descripción es obligatoria")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = completado, onCheckedChange = { completado = it })
                Text("Completada")
            }
            Button(onClick = {
                mostrarErrores = true
                if (titulo.isNotBlank() && descripcion.isNotBlank()) {
                    onGuardar(titulo.trim(), descripcion.trim(), completado)
                }
            }) { Text("Guardar") }
            OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
        }
    }
}
```

### 6.7. Eliminación: vista independiente

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoEliminarScreen.kt`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/main/java/co/edu/iub/myapphero/ui/screens/CursoEliminarScreen.kt -->
```kotlin
package co.edu.iub.myapphero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.iub.myapphero.model.Curso

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoEliminarScreen(
    curso: Curso,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Eliminar curso") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("¿Deseas eliminar este curso?")
            Text(curso.titulo)
            Text("Esta acción no se puede deshacer.")
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) { Text("Confirmar eliminación") }
            OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
        }
    }
}
```

---

<a id="paso-7"></a>

## 7. Comprobación manual, errores frecuentes y cierre

### Recorridos para hacer con los estudiantes

| Acción | Resultado esperado |
|---|---|
| Guardar con campos vacíos o solo espacios | Muestra errores; no agrega nada |
| Crear un curso válido | Regresa al listado; ID nuevo y estado pendiente |
| Crear dos con el mismo título | Tienen IDs diferentes: el título no es el identificador |
| Seleccionar el segundo creado | Muestra sus datos, no siempre los del primero |
| Editar y cancelar | Mantiene título, descripción y estado originales |
| Editar y guardar | Vuelve al detalle del mismo ID con cambios |
| Cancelar eliminación | Vuelve al detalle; el registro sigue existiendo |
| Confirmar eliminación | Vuelve al listado actualizado |
| Pulsar Atrás desde el listado tras eliminar | No reabre el detalle del curso eliminado |
| Eliminar todos los registros de práctica | Aparece «No hay cursos registrados» al quedar vacía la lista |
| Crear después de vaciar | Sigue usando el contador creciente de ese ViewModel |
| Rotar tras crear, editar o eliminar | Mantiene los cambios durante la recreación por configuración |

Para comprobar registro inexistente, cambia **temporalmente** la selección del
listado por `navController.navigate("curso_detalle/999")`. Repite con
`curso_actualizar/999` y `curso_eliminar/999`: mensaje y regreso al listado.
Restaura el callback por ID real al terminar; no entregues una ruta fija.

### Si algo falla durante la clase

| Síntoma | Revisa |
|---|---|
| No reconoce `viewModel()` | Alias, Sync Now e import de lifecycle.viewmodel.compose |
| Falla el delegado `by` | Imports `getValue` y `setValue` |
| Error en parámetros de AppNavigation | Cambiar MainActivity y navegación juntos |
| Setter privado no accesible | No asignar cursos desde la UI: llamar al método del ViewModel |
| La UI no se actualiza | Asignar la nueva lista a la propiedad observable |
| Cada ruta parece tener sus propios datos | No construir/pedir otro ViewModel dentro de cada destino |
| Guardar no navega | Revisar validación o ID inexistente: false no es éxito |
| Cancelar cambia registros | No actualizar el registro desde cada onValueChange del borrador |
| Eliminar vuelve al detalle borrado | Mantener popBackStack hasta cursos, no solo un pop |
| Se pierden datos al terminar proceso | Es el límite del almacenamiento en memoria |

### Nota sobre las pruebas instrumentadas que ya existen

El archivo `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/src/androidTest/java/co/edu/iub/myapphero/CursoNavigationTest.kt`
llamaba `AppNavigation(cursos)` con una lista de prueba. Esa firma cambia. Si
conservas esas pruebas sin adaptarlas, habrá un error de compilación en ese llamado.
No confundas ese cambio de contrato con un fallo del formulario.

Esta guía no introduce factories ni refactoriza esas pruebas: el foco de la
clase es la migración y la comprobación manual. `assembleDebug` compila la app,
no demuestra que las pruebas instrumentadas antiguas estén adaptadas.

### Preguntas de cierre y respuestas para el docente

1. **¿Quién es dueño de los cursos compartidos?** CursosViewModel.
2. **¿Qué hace private set?** Permite leer la propiedad desde fuera, pero asignarla solo desde la clase.
3. **¿Por qué viewModel() y no el constructor?** Para obtener la instancia gestionada por el propietario.
4. **¿Por qué el formulario mantiene su título local?** Es un borrador que podemos cancelar.
5. **¿Por qué seguimos pasando ID por navegación?** Para localizar el registro correcto.
6. **¿Quién decide regresar al listado tras eliminar?** AppNavigation, según el resultado del CRUD.
7. **¿ViewModel guarda para siempre?** No: mantiene memoria ante cambios de configuración, no ante toda pérdida del proceso.

### Práctica breve, sin nueva arquitectura

Pide agregar una operación para marcar todos los cursos como completados.
Antes de escribir pregunta: **«¿Qué debería pasar si la lista está vacía y qué
propiedades debemos conservar de cada curso?»** La pantalla solo emitirá el clic.
No es una nueva ruta obligatoria ni hay que introducir otra fuente de datos.

### Frase para cerrar

> «La interfaz muestra y recoge acciones. El ViewModel mantiene los registros y
> sus operaciones. La navegación conecta pantallas. Ordenar el código es separar
> esos trabajos, no mezclarlos dentro de una clase nueva».

---

## Anexo A. Archivos completos de Gradle

Son los archivos actuales con solo la dependencia nueva. Si ya añadiste las dos
líneas de la sección 2, no vuelvas a agregarlas.

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/gradle/libs.versions.toml`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/gradle/libs.versions.toml -->
```toml
[versions]
agp = "9.2.1"
coreKtx = "1.19.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.11.0"
activityCompose = "1.13.0"
kotlin = "2.2.10"
composeBom = "2026.02.01"
navigationCompose = "2.9.5"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }


[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

**Archivo completo:** `/Users/djruiz/AndroidStudioProjects/MyAppHero/app/build.gradle.kts`

Crea el archivo o reemplaza su contenido. No dupliques las funciones existentes.

<!-- archivo-final: /Users/djruiz/AndroidStudioProjects/MyAppHero/app/build.gradle.kts -->
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "co.edu.iub.myapphero"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "co.edu.iub.myapphero"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.navigation.compose)

}
```

## Anexo B. Comandos y verificación del material

Desde la raíz del proyecto:

```bash
cd /Users/djruiz/AndroidStudioProjects/MyAppHero
./gradlew :app:assembleDebug :app:testDebugUnitTest --console=plain
```

En el equipo del docente usamos `rtk proxy ./gradlew ...` para ejecutar comandos.
RTK no es una dependencia Android ni un requisito para los estudiantes.

- `assembleDebug` comprueba la compilación de la aplicación.
- `testDebugUnitTest` ejecuta las pruebas unitarias presentes; la prueba de ejemplo
  no demuestra por sí sola que todo el CRUD funcione.
- La comprobación de navegación y rotación es la actividad manual de la sección 7.
- Se conserva la advertencia previa AGP 9.2.1/SDK 37.1; no se cambian esas versiones
  como parte de una lección de ViewModel.

### Verificación de esta guía

Preparada sobre los archivos reales de MyAppHero el 28 de octubre de 2026.
Se extrajeron los **12 bloques de archivos completos de este Markdown** y se
aplicaron únicamente a una copia temporal del proyecto.

- `:app:assembleDebug`: correcto.
- `:app:testDebugUnitTest`: correcto, 7 pruebas y 0 fallos.
- De esas pruebas, 6 comprobaciones adicionales de CRUD se crearon solo en la
  copia temporal: búsqueda, validación, creación, actualización por ID,
  eliminación selectiva e IDs crecientes tras vaciar la lista. No son archivos
  adicionales que debas enseñar ni copiar.
- Se comprobó que los archivos fuente y dependencias del proyecto original no
  cambiaron; el único entregable añadido es esta guía.
- No se ejecutó la demostración de rotación en un dispositivo durante esta
  preparación: está indicada como comprobación manual para la clase.
- Las pruebas instrumentadas antiguas no se consideran verificadas con la nueva
  firma: requieren la adaptación indicada en la sección 7.

## Referencias oficiales del docente

- [ViewModel y su ciclo de vida](https://developer.android.com/topic/libraries/architecture/viewmodel).
- [Integración con Compose](https://developer.android.com/develop/ui/compose/libraries#viewmodel).
- [Estado compartido y local](https://developer.android.com/develop/ui/compose/state-hoisting).

No necesitas leer estas páginas en medio de la demostración. Son respaldo para
consultar los conceptos sin confundir conservación en memoria con persistencia.

