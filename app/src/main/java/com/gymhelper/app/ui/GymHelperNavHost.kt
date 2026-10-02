package com.gymhelper.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymhelper.app.ui.home.HomeScreen
import com.gymhelper.app.ui.interval.IntervalDayEditScreen
import com.gymhelper.app.ui.interval.IntervalProgramDetailScreen
import com.gymhelper.app.ui.interval.IntervalProgramsScreen
import com.gymhelper.app.ui.interval.IntervalSessionScreen
import com.gymhelper.app.ui.weight.WeightDayEditScreen
import com.gymhelper.app.ui.weight.WeightProgramDetailScreen
import com.gymhelper.app.ui.weight.WeightProgramsScreen
import com.gymhelper.app.ui.weight.WeightSessionScreen

@Composable
fun GymHelperNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavRoutes.Home) {
        composable(NavRoutes.Home) {
            HomeScreen(
                onIntervalTraining = { navController.navigate(NavRoutes.IntervalPrograms) },
                onWeightTraining = { navController.navigate(NavRoutes.WeightPrograms) },
            )
        }
        composable(NavRoutes.IntervalPrograms) {
            IntervalProgramsScreen(
                onBack = { navController.popBackStack() },
                onOpenProgram = { navController.navigate(NavRoutes.intervalProgram(it)) },
            )
        }
        composable(
            route = NavRoutes.IntervalProgram,
            arguments = listOf(navArgument("programId") { type = NavType.LongType }),
        ) { entry ->
            val programId = entry.arguments?.getLong("programId") ?: return@composable
            IntervalProgramDetailScreen(
                programId = programId,
                onBack = { navController.popBackStack() },
                onEditDay = { navController.navigate(NavRoutes.intervalDayEdit(it)) },
                onStartSession = { navController.navigate(NavRoutes.intervalSession(it)) },
            )
        }
        composable(
            route = NavRoutes.IntervalDayEdit,
            arguments = listOf(navArgument("dayId") { type = NavType.LongType }),
        ) { entry ->
            val dayId = entry.arguments?.getLong("dayId") ?: return@composable
            IntervalDayEditScreen(dayId = dayId, onBack = { navController.popBackStack() })
        }
        composable(
            route = NavRoutes.IntervalSession,
            arguments = listOf(navArgument("dayId") { type = NavType.LongType }),
        ) { entry ->
            val dayId = entry.arguments?.getLong("dayId") ?: return@composable
            IntervalSessionScreen(dayId = dayId, onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.WeightPrograms) {
            WeightProgramsScreen(
                onBack = { navController.popBackStack() },
                onOpenProgram = { navController.navigate(NavRoutes.weightProgram(it)) },
            )
        }
        composable(
            route = NavRoutes.WeightProgram,
            arguments = listOf(navArgument("programId") { type = NavType.LongType }),
        ) { entry ->
            val programId = entry.arguments?.getLong("programId") ?: return@composable
            WeightProgramDetailScreen(
                programId = programId,
                onBack = { navController.popBackStack() },
                onEditDay = { navController.navigate(NavRoutes.weightDayEdit(it)) },
                onStartSession = { navController.navigate(NavRoutes.weightSession(it)) },
            )
        }
        composable(
            route = NavRoutes.WeightDayEdit,
            arguments = listOf(navArgument("dayId") { type = NavType.LongType }),
        ) { entry ->
            val dayId = entry.arguments?.getLong("dayId") ?: return@composable
            WeightDayEditScreen(dayId = dayId, onBack = { navController.popBackStack() })
        }
        composable(
            route = NavRoutes.WeightSession,
            arguments = listOf(navArgument("dayId") { type = NavType.LongType }),
        ) { entry ->
            val dayId = entry.arguments?.getLong("dayId") ?: return@composable
            WeightSessionScreen(dayId = dayId, onBack = { navController.popBackStack() })
        }
    }
}
