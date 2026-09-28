package com.example.stashly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stashly.data.StashType
import com.example.stashly.ui.AuthViewModel
import com.example.stashly.ui.StashViewModel
import com.example.stashly.ui.screens.AddEditScreen
import com.example.stashly.ui.screens.AuthScreen
import com.example.stashly.ui.screens.DetailScreen
import com.example.stashly.ui.screens.HomeScreen

object Screen {
    const val AUTH = "auth"
    const val HOME = "home"
    const val ADD_EDIT = "add_edit?itemId={itemId}&type={type}"
    const val DETAIL = "detail/{itemId}"

    fun createAddEditRoute(itemId: Long = 0L, type: StashType = StashType.NOTE): String {
        return "add_edit?itemId=$itemId&type=${type.name}"
    }

    fun createDetailRoute(itemId: Long): String {
        return "detail/$itemId"
    }
}

@Composable
fun AppNavigation(
    stashViewModel: StashViewModel,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()

    val startDestination = if (currentUser == null) Screen.AUTH else Screen.HOME

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            stashViewModel.initUserSession(user.uid)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.AUTH) {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.HOME) {
                        popUpTo(Screen.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.HOME) {
            val user = currentUser
            HomeScreen(
                viewModel = stashViewModel,
                userEmail = user?.email,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onAddItem = {
                    navController.navigate(Screen.createAddEditRoute())
                },
                onItemClick = { itemId ->
                    navController.navigate(Screen.createDetailRoute(itemId))
                }
            )
        }

        composable(
            route = Screen.ADD_EDIT,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = StashType.NOTE.name
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            val typeStr = backStackEntry.arguments?.getString("type") ?: StashType.NOTE.name
            val initialType = try {
                StashType.valueOf(typeStr)
            } catch (e: Exception) {
                StashType.NOTE
            }

            AddEditScreen(
                viewModel = stashViewModel,
                itemId = itemId,
                initialType = initialType,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DETAIL,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L

            DetailScreen(
                viewModel = stashViewModel,
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onEdit = { editItemId ->
                    navController.navigate(Screen.createAddEditRoute(itemId = editItemId))
                }
            )
        }
    }
}
