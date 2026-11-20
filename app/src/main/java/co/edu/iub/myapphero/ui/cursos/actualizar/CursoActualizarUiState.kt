package co.edu.iub.myapphero.ui.cursos.actualizar

import co.edu.iub.myapphero.domain.model.Curso

data class CursoActualizarUiState(
    val curso: Curso? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val completado: Boolean = false,
    val mostrarErrores: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val noEncontrado: Boolean = false,
    val error: String? = null,
    val guardado: Boolean = false
)
