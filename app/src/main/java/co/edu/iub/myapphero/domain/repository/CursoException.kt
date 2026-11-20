package co.edu.iub.myapphero.domain.repository

open class CursoException(message: String, cause: Throwable? = null) : Exception(message, cause)

class CursoNoEncontradoException : CursoException("El curso ya no existe.")