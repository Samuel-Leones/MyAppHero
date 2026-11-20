package co.edu.iub.myapphero

import androidx.compose.ui.test.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import co.edu.iub.myapphero.domain.model.Curso
import co.edu.iub.myapphero.navigation.AppNavigation
import co.edu.iub.myapphero.ui.theme.MyAppHeroTheme
import org.junit.Rule
import org.junit.Test

class CursoNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private fun abrir(cursos: List<Curso> = listOf(
        Curso(4, "Kotlin", "Aprender Kotlin"),
        Curso(12, "Angular", "Aprender Angular")
    )) {
        compose.setContent { MyAppHeroTheme { AppNavigation(cursos) } }
    }

    private fun pulsar(texto: String) {
        compose.onNodeWithText(texto).performScrollTo().performClick()
    }

    @Test
    fun crearValidaCamposYRegresaAlListadoConIdUnico() {
        abrir()
        pulsar("Crear curso")
        pulsar("Guardar")
        compose.onNodeWithText("El título es obligatorio").assertIsDisplayed()
        compose.onNodeWithText("La descripción es obligatoria").assertIsDisplayed()
        compose.onNodeWithText("Título").performTextInput("   ")
        compose.onNodeWithText("Descripción").performTextInput("   ")
        pulsar("Guardar")
        compose.onNodeWithText("El título es obligatorio").assertIsDisplayed()
        compose.onNodeWithText("Título").performTextReplacement("Nuevo curso")
        compose.onNodeWithText("Descripción").performTextReplacement("Nueva descripción")
        pulsar("Guardar")
        pulsar("Nuevo curso")
        compose.onNodeWithText("Identificador: 13").assertIsDisplayed()
        compose.onNodeWithText("Nueva descripción").assertIsDisplayed()
        compose.onNodeWithText("Estado: Pendiente").assertIsDisplayed()
    }

    @Test
    fun cancelarCreacionNoAgregaCurso() {
        abrir(emptyList())
        pulsar("Crear curso")
        compose.onNodeWithText("Título").performTextInput("Descartado")
        pulsar("Cancelar")
        compose.onNodeWithText("No hay cursos registrados").assertIsDisplayed()
        compose.onNodeWithText("Descartado").assertDoesNotExist()
    }

    @Test
    fun actualizarUsaElIdYGuardaEstadoSinModificarOtroCurso() {
        abrir()
        pulsar("Angular")
        pulsar("Actualizar curso")
        compose.onNodeWithText("Título").assertTextContains("Angular")
        compose.onNodeWithText("Descripción").assertTextContains("Aprender Angular")
        compose.onNodeWithText("Título").performTextReplacement("  ")
        compose.onNodeWithText("Descripción").performTextReplacement("")
        pulsar("Guardar")
        compose.onNodeWithText("El título es obligatorio").assertIsDisplayed()
        compose.onNodeWithText("La descripción es obligatoria").assertIsDisplayed()
        compose.onNodeWithText("Título").performTextReplacement("Angular actualizado")
        compose.onNodeWithText("Descripción").performTextReplacement("Descripción actualizada")
        compose.onNode(isToggleable()).performClick()
        pulsar("Guardar")
        compose.onNodeWithText("Detalle del curso").assertIsDisplayed()
        compose.onNodeWithText("Identificador: 12").assertIsDisplayed()
        compose.onNodeWithText("Angular actualizado").assertIsDisplayed()
        compose.onNodeWithText("Estado: Completada").assertIsDisplayed()
        pulsar("Volver al listado")
        pulsar("Kotlin")
        compose.onNodeWithText("Aprender Kotlin").assertIsDisplayed()
        compose.onNodeWithText("Estado: Pendiente").assertIsDisplayed()
    }

    @Test
    fun cancelarEdicionConservaDatosOriginales() {
        abrir()
        pulsar("Angular")
        pulsar("Actualizar curso")
        compose.onNodeWithText("Título").performTextReplacement("Descartado")
        compose.onNode(isToggleable()).performClick()
        pulsar("Cancelar")
        compose.onNodeWithText("Angular").assertIsDisplayed()
        compose.onNodeWithText("Estado: Pendiente").assertIsDisplayed()
        pulsar("Actualizar curso")
        compose.onNodeWithText("Título").assertTextContains("Angular")
        compose.onNode(isToggleable()).assertIsOff()
    }

    @Test
    fun eliminacionTienePantallaPropiaCancelarConservaYConfirmarEliminaSoloSeleccionado() {
        // Registra la salida antes del NavHost: solo la recibe si ya no hay
        // destinos anteriores. No dependemos de si Android finaliza o minimiza.
        var salidaSolicitada = false
        compose.activity.onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { salidaSolicitada = true }
            }
        )
        abrir()
        pulsar("Angular")
        pulsar("Eliminar curso")
        compose.onNodeWithText("¿Deseas eliminar este curso?").assertIsDisplayed()
        compose.onNodeWithText("Angular").assertIsDisplayed()
        pulsar("Cancelar")
        compose.onNodeWithText("Detalle del curso").assertIsDisplayed()
        pulsar("Eliminar curso")
        pulsar("Confirmar eliminación")
        compose.onNodeWithText("Cursos").assertIsDisplayed()
        compose.onNodeWithText("Angular").assertDoesNotExist()
        pulsar("Kotlin")
        compose.onNodeWithText("Identificador: 4").assertIsDisplayed()
        pulsar("Volver al listado")
        // Atrás desde el listado debe salir, no volver al detalle eliminado.
        val actividad = compose.activity
        compose.runOnIdle { actividad.onBackPressedDispatcher.onBackPressed() }
        compose.waitUntil(timeoutMillis = 5_000) { salidaSolicitada }
        compose.onNodeWithText("Cursos").assertIsDisplayed()
        compose.onNodeWithText("Curso no encontrado").assertDoesNotExist()
    }

    @Test
    fun eliminarUltimoCursoMuestraListadoVacioYCrearNoReutilizaId() {
        abrir(listOf(Curso(4, "Último", "Descripción")))
        pulsar("Último")
        pulsar("Eliminar curso")
        pulsar("Confirmar eliminación")
        compose.onNodeWithText("No hay cursos registrados").assertIsDisplayed()
        pulsar("Crear curso")
        compose.onNodeWithText("Título").performTextInput("Otro")
        compose.onNodeWithText("Descripción").performTextInput("Otra descripción")
        pulsar("Guardar")
        pulsar("Otro")
        compose.onNodeWithText("Identificador: 5").assertIsDisplayed()
    }
}
