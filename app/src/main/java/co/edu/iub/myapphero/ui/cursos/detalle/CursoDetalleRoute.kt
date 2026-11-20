package co.edu.iub.myapphero.ui.cursos.detalle

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import co.edu.iub.myapphero.ui.components.EstadoCurso

@Composable
fun CursoDetalleRoute(
    onVolver: () -> Unit,
    onActualizar: () -> Unit,
    onEliminar: () -> Unit
) {
    val viewModel: CursoDetalleViewModel = hiltViewModel()
    // Este destino se reanuda al guardar/cancelar la actualización.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.cargar() }
    val estado = viewModel.uiState
    when {
        estado.cargando -> EstadoCurso("Cargando curso...", onVolver, cargando = true)
        estado.noEncontrado -> EstadoCurso("Curso no encontrado", onVolver)
        estado.curso == null -> EstadoCurso(
            estado.error ?: "No se pudo cargar el curso.", onVolver,
            onReintentar = viewModel::cargar
        )
        else -> CursoDetalleScreen(
            estado, onVolver, onActualizar, onEliminar, viewModel::cargar
        )
    }
}
