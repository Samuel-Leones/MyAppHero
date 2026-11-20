package co.edu.iub.myapphero.data.remote.request

// El servidor asigna el ID: nunca enviamos un contador local.
data class CursoRequest(
    val titulo: String,
    val descripcion: String,
    val completado: Boolean = false
)