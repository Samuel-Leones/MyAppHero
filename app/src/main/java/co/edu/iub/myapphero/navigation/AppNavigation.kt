package co.edu.iub.myapphero.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.iub.myapphero.ui.cursos.actualizar.CursoActualizarRoute
import co.edu.iub.myapphero.ui.cursos.crear.CursoCrearRoute
import co.edu.iub.myapphero.ui.cursos.detalle.CursoDetalleRoute
import co.edu.iub.myapphero.ui.cursos.eliminar.CursoEliminarRoute
import co.edu.iub.myapphero.ui.cursos.listado.CursosRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "cursos") {
        composable("cursos") {
            CursosRoute(
                onCursoClick = { navController.navigate("curso_detalle/$it") },
                onCrear = { navController.navigate("curso_crear") }
            )
        }
        composable("curso_crear") {
            CursoCrearRoute(
                onGuardado = { navController.popBackStack("cursos", false) },
                onCancelar = { navController.popBackStack() }
            )
        }
        composable(
            "curso_detalle/{cursoId}",
            arguments = listOf(navArgument("cursoId") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("cursoId") ?: -1
            CursoDetalleRoute(
                onVolver = { navController.popBackStack("cursos", false) },
                onActualizar = { navController.navigate("curso_actualizar/$id") },
                onEliminar = { navController.navigate("curso_eliminar/$id") }
            )
        }
        composable(
            "curso_actualizar/{cursoId}",
            arguments = listOf(navArgument("cursoId") { type = NavType.IntType })
        ) {
            CursoActualizarRoute(
                onGuardado = { navController.popBackStack() },
                onCancelar = { navController.popBackStack() },
                onVolverListado = { navController.popBackStack("cursos", false) }
            )
        }
        composable(
            "curso_eliminar/{cursoId}",
            arguments = listOf(navArgument("cursoId") { type = NavType.IntType })
        ) {
            CursoEliminarRoute(
                onEliminado = { navController.popBackStack("cursos", false) },
                onCancelar = { navController.popBackStack() },
                onVolverListado = { navController.popBackStack("cursos", false) }
            )
        }
    }
}
