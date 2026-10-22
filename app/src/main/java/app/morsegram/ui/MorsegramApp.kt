package app.morsegram.ui

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.morsegram.ui.auth.CodeScreen
import app.morsegram.ui.auth.EmailScreen
import app.morsegram.ui.chats.ChatListScreen
import app.morsegram.ui.chats.ChatScreen

private object Routes {
    const val EMAIL = "email"
    const val CODE = "code/{email}"
    const val CHATS = "chats"
    const val CHAT = "chat/{chatId}"

    fun code(email: String) = "code/${Uri.encode(email)}"
    fun chat(chatId: String) = "chat/$chatId"
}

private const val DURATION = 380

@Composable
fun MorsegramApp() {
    val navController = rememberNavController()

    // Экраны въезжают справа и уезжают влево, «назад» — наоборот.
    NavHost(
        navController = navController,
        startDestination = Routes.EMAIL,
        enterTransition = {
            slideIntoContainer(SlideDirection.Start, tween(DURATION)) { it / 3 } + fadeIn(tween(DURATION))
        },
        exitTransition = {
            slideOutOfContainer(SlideDirection.Start, tween(DURATION)) { it / 3 } + fadeOut(tween(DURATION))
        },
        popEnterTransition = {
            slideIntoContainer(SlideDirection.End, tween(DURATION)) { it / 3 } + fadeIn(tween(DURATION))
        },
        popExitTransition = {
            slideOutOfContainer(SlideDirection.End, tween(DURATION)) { it / 3 } + fadeOut(tween(DURATION))
        },
    ) {
        composable(Routes.EMAIL) {
            EmailScreen(onCodeRequested = { email -> navController.navigate(Routes.code(email)) })
        }
        composable(Routes.CODE) { entry ->
            CodeScreen(
                email = entry.arguments?.getString("email").orEmpty(),
                onCodeConfirmed = {
                    navController.navigate(Routes.CHATS) {
                        popUpTo(Routes.EMAIL) { inclusive = true }
                    }
                },
                onChangeEmail = { navController.popBackStack() },
            )
        }
        composable(
            Routes.CHATS,
            enterTransition = { fadeIn(tween(DURATION)) + scaleIn(tween(DURATION), initialScale = 0.92f) },
        ) {
            ChatListScreen(onChatClick = { chatId -> navController.navigate(Routes.chat(chatId)) })
        }
        composable(Routes.CHAT) { entry ->
            ChatScreen(
                chatId = entry.arguments?.getString("chatId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
