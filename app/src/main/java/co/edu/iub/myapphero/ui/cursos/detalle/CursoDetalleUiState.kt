package co.edu.iub.myapphero.ui.cursos.detalle

import co.edu.iub.myapphero.domain.model.Curso

data class CursoDetalleUiState(
    val curso: Curso? = null,
    val cargando: Boolean = false,
    val noEncontrado: Boolean = false,
    val error: String? = null
)
