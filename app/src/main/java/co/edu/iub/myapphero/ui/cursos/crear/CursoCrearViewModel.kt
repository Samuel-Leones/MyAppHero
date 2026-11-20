package co.edu.iub.myapphero.ui.cursos.crear

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.iub.myapphero.domain.repository.CursoRepository
import co.edu.iub.myapphero.ui.components.mensajeCursoError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@HiltViewModel
class CursoCrearViewModel @Inject constructor(private val repository: CursoRepository) : ViewModel() {
    var uiState by mutableStateOf(CursoCrearUiState())
        private set

    fun cambiarTitulo(valor: String) {
        if (!uiState.guardando && !uiState.creado) uiState = uiState.copy(titulo = valor, error = null)
    }

    fun cambiarDescripcion(valor: String) {
        if (!uiState.guardando && !uiState.creado) uiState = uiState.copy(descripcion = valor, error = null)
    }

    fun guardar() {
        if (uiState.guardando || uiState.creado) return
        uiState = uiState.copy(mostrarErrores = true, error = null)
        if (uiState.titulo.isBlank() || uiState.descripcion.isBlank()) return
        val datos = uiState
        uiState = uiState.copy(guardando = true)
        viewModelScope.launch {
            try {
                repository.crear(datos.titulo.trim(), datos.descripcion.trim())
                uiState = uiState.copy(creado = true)
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                uiState = uiState.copy(error = mensajeCursoError(error))
            } finally {
                uiState = uiState.copy(guardando = false)
            }
        }
    }

}
