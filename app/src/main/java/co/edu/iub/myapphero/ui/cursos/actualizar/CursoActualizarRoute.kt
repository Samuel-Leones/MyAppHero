package co.edu.iub.myapphero.ui.cursos.actualizar

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import co.edu.iub.myapphero.ui.components.EstadoCurso

@Composable
fun CursoActualizarRoute(
    onGuardado: () -> Unit,
    onCancelar: () -> Unit,
    onVolverListado: () -> Unit
) {
    val viewModel: CursoActualizarViewModel = hiltViewModel()
    val estado = viewModel.uiState
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) {
            onGuardado()
        }
    }
    when {
        estado.cargando -> EstadoCurso("Cargando curso...", onVolverListado, cargando = true)
        estado.noEncontrado -> EstadoCurso("Curso no encontrado", onVolverListado)
        estado.curso == null -> EstadoCurso(
            estado.error ?: "No se pudo cargar el curso.", onVolverListado,
            onReintentar = viewModel::cargar
        )
        else -> CursoActualizarScreen(
            estado, viewModel::cambiarTitulo, viewModel::cambiarDescripcion,
            viewModel::cambiarCompletado, viewModel::guardar, onCancelar
        )
    }
    BackHandler(enabled = estado.guardando || estado.guardado) { }
}
