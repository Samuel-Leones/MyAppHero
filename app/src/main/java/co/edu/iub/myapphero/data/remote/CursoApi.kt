package co.edu.iub.myapphero.data.remote

import co.edu.iub.myapphero.data.remote.dto.CursoDto
import co.edu.iub.myapphero.data.remote.request.CursoRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
interface CursoApi {
    @GET("cursos")
    suspend fun listar(): List<CursoDto>

    @GET("cursos/{id}")
    suspend fun obtener(
        @Path("id")
        id: Int
    ): CursoDto

    @POST("cursos")
    suspend fun crear(@Body curso: CursoRequest): CursoDto

    @PUT("cursos/{id}")
    suspend fun actualizar(
        @Path("id")
        id: Int,
        @Body curso: CursoRequest
    ): CursoDto

    @DELETE("cursos/{id}")
    suspend fun eliminar(
        @Path("id")
        id: Int
    ): Response<Unit>
}