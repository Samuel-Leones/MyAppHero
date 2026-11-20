package co.edu.iub.myapphero.ui.cursos.eliminar

import co.edu.iub.myapphero.domain.model.Curso

data class CursoEliminarUiState(
    val curso: Curso? = null,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val noEncontrado: Boolean = false,
    val error: String? = null,
    val eliminado: Boolean = false
)
