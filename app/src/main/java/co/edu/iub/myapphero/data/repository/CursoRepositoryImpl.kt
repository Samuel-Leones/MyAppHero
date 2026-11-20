package co.edu.iub.myapphero.data.repository

import co.edu.iub.myapphero.data.remote.CursoApi
import co.edu.iub.myapphero.data.remote.request.CursoRequest
import co.edu.iub.myapphero.domain.model.Curso
import co.edu.iub.myapphero.domain.repository.CursoException
import co.edu.iub.myapphero.domain.repository.CursoNoEncontradoException
import co.edu.iub.myapphero.domain.repository.CursoRepository
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CursoRepositoryImpl @Inject constructor(private val api: CursoApi) : CursoRepository {

    override suspend fun listar(): List<Curso> = solicitar {
        api.listar().map { it.toCurso() }
    }

    override suspend fun obtener(id: Int): Curso? = try {
        solicitar { api.obtener(id).toCurso() }
    } catch (error: CursoNoEncontradoException) {
        null
    }

    override suspend fun crear(
        titulo: String,
        descripcion: String
    ): Curso = solicitar {
        val request = CursoRequest(
            titulo = titulo,
            descripcion = descripcion,
            completado = false
        )
        api.crear(request).toCurso()
    }

    override suspend fun actualizar(
        id: Int,
        titulo: String,
        descripcion: String,
        completado: Boolean
    ): Curso = solicitar {
        val request = CursoRequest(
            titulo = titulo,
            descripcion = descripcion,
            completado = completado
        )
        api.actualizar(id, request).toCurso()
    }

    override suspend fun eliminar(id: Int) = solicitar {
        val respuesta = api.eliminar(id)
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
    }

    private suspend fun <T> solicitar(accion: suspend () -> T): T = try {
        accion()
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (error: HttpException) {
        if (error.code() == 404) throw CursoNoEncontradoException()
        throw CursoException("El servidor no pudo completar la solicitud (HTTP ${error.code()}).", error)
    } catch (error: IOException) {
        throw CursoException("No se pudo conectar. Revisa tu conexión e inténtalo de nuevo.", error)
    } catch (error: Exception) {
        throw CursoException("No se pudo procesar la respuesta del servidor.", error)
    }
}