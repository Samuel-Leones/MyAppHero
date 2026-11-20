package co.edu.iub.myapphero.ui.cursos.actualizar

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
class CursoActualizarViewModel @Inject constructor(
    private val repository: CursoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Es el argumento cursoId declarado en la ruta de navegación.
    private val cursoId: Int = savedStateHandle.get<Int>("cursoId") ?: -1

    var uiState by mutableStateOf(CursoActualizarUiState())
        private set

    init { cargar() }

    fun cargar() {
        if (uiState.cargando || uiState.guardando || uiState.guardado) return
        uiState = uiState.copy(cargando = true, error = null)
        viewModelScope.launch {
            try {
                val curso = repository.obtener(cursoId)
                uiState = if (curso == null) {
                    uiState.copy(curso = null, noEncontrado = true)
                } else {
                    uiState.copy(
                        curso = curso, titulo = curso.titulo, descripcion = curso.descripcion,
                        completado = curso.completado, noEncontrado = false
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                uiState = uiState.copy(error = mensajeCursoError(error))
            } finally {
                uiState = uiState.copy(cargando = false)
            }
        }
    }

    fun cambiarTitulo(valor: String) {
        if (!uiState.guardando && !uiState.guardado) uiState = uiState.copy(titulo = valor, error = null)
    }

    fun cambiarDescripcion(valor: String) {
        if (!uiState.guardando && !uiState.guardado) uiState = uiState.copy(descripcion = valor, error = null)
    }

    fun cambiarCompletado(valor: Boolean) {
        if (!uiState.guardando && !uiState.guardado) uiState = uiState.copy(completado = valor, error = null)
    }

    fun guardar() {
        if (uiState.guardando || uiState.cargando || uiState.curso == null || uiState.guardado) return
        uiState = uiState.copy(mostrarErrores = true, error = null)
        if (uiState.titulo.isBlank() || uiState.descripcion.isBlank()) return
        val datos = uiState
        uiState = uiState.copy(guardando = true)
        viewModelScope.launch {
            try {
                repository.actualizar(
                    cursoId, datos.titulo.trim(), datos.descripcion.trim(), datos.completado
                )
                uiState = uiState.copy(guardado = true)
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
