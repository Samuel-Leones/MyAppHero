package co.edu.iub.myapphero.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EstadoCurso(
    mensaje: String,
    onVolver: () -> Unit,
    cargando: Boolean = false,
    onReintentar: (() -> Unit)? = null
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (cargando) CircularProgressIndicator()
            Text(mensaje)
            if (onReintentar != null) {
                Button(onClick = onReintentar) { Text("Reintentar") }
            }
            Button(onClick = onVolver) { Text("Volver al listado") }
        }
    }
}

