package co.edu.iub.myapphero.ui.cursos.crear

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect

@Composable
fun CursoCrearRoute(onGuardado: () -> Unit, onCancelar: () -> Unit) {
    val viewModel: CursoCrearViewModel = hiltViewModel()
    val estado = viewModel.uiState
    LaunchedEffect(estado.creado) {
        if (estado.creado) {
            onGuardado()
        }
    }
    CursoCrearScreen(
        estado, viewModel::cambiarTitulo, viewModel::cambiarDescripcion,
        viewModel::guardar, onCancelar
    )
    BackHandler(enabled = estado.guardando || estado.creado) { }
}
