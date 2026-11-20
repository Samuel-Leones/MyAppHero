package co.edu.iub.myapphero.ui.cursos.listado

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
fun CursosRoute(onCursoClick: (Int) -> Unit, onCrear: () -> Unit) {
    val viewModel: CursosViewModel = hiltViewModel()
    // Al volver de crear/eliminar, se consulta la fuente de verdad otra vez.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.cargar() }
    val estado = viewModel.uiState
    CursosScreen(
        estado = estado,
        onCursoClick = { if (!estado.cargando) onCursoClick(it) },
        onCrearClick = onCrear,
        onReintentar = viewModel::cargar
    )
}
