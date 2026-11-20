package co.edu.iub.myapphero.ui.cursos.listado

import co.edu.iub.myapphero.domain.model.Curso

data class CursosUiState(
    val cursos: List<Curso> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)
