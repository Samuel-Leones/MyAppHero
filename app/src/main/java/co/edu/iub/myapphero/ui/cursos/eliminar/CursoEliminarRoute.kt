package co.edu.iub.myapphero.ui.cursos.eliminar

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import co.edu.iub.myapphero.ui.components.EstadoCurso

@Composable
fun CursoEliminarRoute(
    onEliminado: () -> Unit,
    onCancelar: () -> Unit,
    onVolverListado: () -> Unit
) {
    val viewModel: CursoEliminarViewModel = hiltViewModel()
    val estado = viewModel.uiState
    LaunchedEffect(estado.eliminado) {
        if (estado.eliminado) {
            onEliminado()
        }
    }
    when {
        estado.cargando -> EstadoCurso("Cargando curso...", onVolverListado, cargando = true)
        estado.noEncontrado -> EstadoCurso("Curso no encontrado", onVolverListado)
        estado.curso == null -> EstadoCurso(
            estado.error ?: "No se pudo cargar el curso.", onVolverListado,
            onReintentar = viewModel::cargar
        )
        else -> CursoEliminarScreen(estado, viewModel::confirmar, onCancelar)
    }
    BackHandler(enabled = estado.guardando || estado.eliminado) { }
}
