package co.edu.iub.myapphero.di
// di inyeccion de dependencias

import co.edu.iub.myapphero.data.repository.CursoRepositoryImpl
import co.edu.iub.myapphero.domain.repository.CursoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCursoRepository(implementation: CursoRepositoryImpl): CursoRepository
}
