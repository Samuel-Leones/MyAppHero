package co.edu.iub.myapphero.domain.repository

import co.edu.iub.myapphero.domain.model.Curso

interface CursoRepository { // Define que operaciones se pueden hacer pero no como se hacen, el que herede esta interfaz esta obligado a utilizar todos los metodos
    suspend fun listar(): List<Curso>
    suspend fun obtener(id: Int): Curso?
    suspend fun crear(titulo: String, descripcion: String): Curso
    suspend fun actualizar(id: Int, titulo: String, descripcion: String, completado: Boolean): Curso
    suspend fun eliminar(id: Int)
}