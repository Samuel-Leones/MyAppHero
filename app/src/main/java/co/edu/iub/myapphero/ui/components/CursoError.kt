package co.edu.iub.myapphero.ui.components



import co.edu.iub.myapphero.domain.repository.CursoException

fun mensajeCursoError(error: Exception): String =
    if (error is CursoException) error.message ?: "No se pudo completar la operación."
    else "No se pudo completar la operación. Inténtalo de nuevo."
