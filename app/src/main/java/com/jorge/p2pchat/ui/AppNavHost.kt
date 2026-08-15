package com.jorge.p2pchat.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorge.p2pchat.settings.SettingsStore

/**
 * Transiciones nativas de Compose (sin librerías externas de animación,
 * son baratas en CPU/batería — importante para gama baja).
 *
 * - Lista de chats -> chat individual: desliza de derecha a izquierda
 * - Cualquier pantalla -> ajustes: desliza de abajo hacia arriba (como
 *   un modal), con fade suave
 */
private const val ANIM_DURATION = 260

private fun slideInFromRight() = slideInHorizontally(
    animationSpec = tween(ANIM_DURATION),
    initialOffsetX = { fullWidth -> fullWidth }
) + fadeIn(tween(ANIM_DURATION))

private fun slideOutToLeft() = slideOutHorizontally(
    animationSpec = tween(ANIM_DURATION),
    targetOffsetX = { fullWidth -> -fullWidth / 3 }
) + fadeOut(tween(ANIM_DURATION))

private fun slideInFromBottom() = slideInVertically(
    animationSpec = tween(ANIM_DURATION),
    initialOffsetY = { fullHeight -> fullHeight }
) + fadeIn(tween(ANIM_DURATION))

private fun slideOutToBottom() = slideOutVertically(
    animationSpec = tween(ANIM_DURATION),
    targetOffsetY = { fullHeight -> fullHeight }
) + fadeOut(tween(ANIM_DURATION))

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController(), settingsStore: SettingsStore) {
    NavHost(navController = navController, startDestination = "chatList") {

        composable(
            route = "chatList",
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromRight() }
        ) {
            ChatListScreen(
                onOpenChat = { chatId -> navController.navigate("chat/$chatId") },
                onOpenSettings = { navController.navigate("settings") }
            )
        }

        composable(
            route = "chat/{chatId}",
            enterTransition = { slideInFromRight() },
            popExitTransition = { slideOutToLeft() }
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(chatId = chatId, onBack = { navController.popBackStack() })
        }

        composable(
            route = "settings",
            enterTransition = { slideInFromBottom() },
            exitTransition = { slideOutToBottom() }
        ) {
            SettingsScreen(settingsStore = settingsStore)
        }
    }
}
