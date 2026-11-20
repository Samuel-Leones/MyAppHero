package co.edu.iub.myapphero.ui.cursos.crear

data class CursoCrearUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val mostrarErrores: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val creado: Boolean = false
)
