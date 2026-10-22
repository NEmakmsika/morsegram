package app.morsegram.ui

import android.net.Uri
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

@Composable
fun MorsegramApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.EMAIL) {
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
        composable(Routes.CHATS) {
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
