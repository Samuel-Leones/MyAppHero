package co.edu.iub.myapphero.ui.cursos.eliminar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.edu.iub.myapphero.domain.repository.CursoRepository
import co.edu.iub.myapphero.ui.components.mensajeCursoError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import co.edu.iub.myapphero.domain.repository.CursoNoEncontradoException

@HiltViewModel
class CursoEliminarViewModel @Inject constructor(
    private val repository: CursoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Es el argumento cursoId declarado en la ruta de navegación.
    private val cursoId: Int = savedStateHandle.get<Int>("cursoId") ?: -1

    var uiState by mutableStateOf(CursoEliminarUiState())
        private set

    init { cargar() }

    fun cargar() {
        if (uiState.cargando || uiState.guardando || uiState.eliminado) return
        uiState = uiState.copy(cargando = true, error = null)
        viewModelScope.launch {
            try {
                val curso = repository.obtener(cursoId)
                uiState = uiState.copy(curso = curso, noEncontrado = curso == null)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                uiState = uiState.copy(error = mensajeCursoError(error))
            } finally {
                uiState = uiState.copy(cargando = false)
            }
        }
    }

    fun confirmar() {
        if (uiState.guardando || uiState.cargando || uiState.curso == null || uiState.eliminado) return
        uiState = uiState.copy(guardando = true, error = null)
        viewModelScope.launch {
            try {
                repository.eliminar(cursoId)
                uiState = uiState.copy(eliminado = true)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: CursoNoEncontradoException) {
                uiState = uiState.copy(curso = null, noEncontrado = true)
            } catch (error: Exception) {
                uiState = uiState.copy(error = mensajeCursoError(error))
            } finally {
                uiState = uiState.copy(guardando = false)
            }
        }
    }

}
