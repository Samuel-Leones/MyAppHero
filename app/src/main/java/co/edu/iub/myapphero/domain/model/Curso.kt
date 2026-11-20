package co.edu.iub.myapphero.domain.model

data class  Curso( // Estructura de datos pura logica de negocio
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val completado: Boolean = false
)
