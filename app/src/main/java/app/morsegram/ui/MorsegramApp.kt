package app.morsegram.ui

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.morsegram.AppGraph
import app.morsegram.data.FakeChatRepository
import app.morsegram.data.UserSettings
import app.morsegram.ui.auth.CodeScreen
import app.morsegram.ui.auth.LoginScreen
import app.morsegram.ui.auth.ProfileSetupScreen
import app.morsegram.ui.auth.RegisterScreen
import app.morsegram.ui.auth.WelcomeScreen
import app.morsegram.ui.chats.ChatScreen
import app.morsegram.ui.main.MainScreen
import app.morsegram.ui.settings.AppearanceScreen
import app.morsegram.ui.settings.DevicesScreen
import app.morsegram.ui.settings.NotificationsScreen
import app.morsegram.ui.settings.PrivacyScreen
import app.morsegram.ui.settings.ProfileEditScreen
import app.morsegram.ui.settings.ServerScreen
import app.morsegram.ui.settings.SettingsPage
import kotlinx.coroutines.launch

private object Routes {
    const val WELCOME = "welcome"
    const val REGISTER = "register"
    const val LOGIN = "login"
    const val CODE = "code/{email}"
    const val PROFILE_SETUP = "profile-setup"
    const val MAIN = "main"
    const val CHAT = "chat/{chatId}"
    const val SETTINGS = "settings/{page}"

    fun code(email: String) = "code/${Uri.encode(email)}"
    fun chat(chatId: String) = "chat/$chatId"
    fun settings(page: SettingsPage) = "settings/${page.name}"
}

private const val DURATION = 380

@Composable
fun MorsegramApp(settings: UserSettings) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    // Стартовый экран выбираем один раз, иначе навигация сбросится посреди регистрации.
    val start = remember { if (settings.loggedIn) Routes.MAIN else Routes.WELCOME }

    // Экраны въезжают справа и уезжают влево, «назад» — наоборот.
    NavHost(
        navController = navController,
        startDestination = start,
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
        composable(Routes.WELCOME) {
            WelcomeScreen(
                demo = settings.demo,
                onRegister = { navController.navigate(Routes.REGISTER) },
                onLogin = { navController.navigate(Routes.LOGIN) },
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onCodeSent = { email -> navController.navigate(Routes.code(email)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = { navController.goToMain() },
                onNeedsVerification = { email -> navController.navigate(Routes.code(email)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.CODE) { entry ->
            val email = entry.arguments?.getString("email").orEmpty()
            CodeScreen(
                email = email,
                onCodeConfirmed = {
                    navController.navigate(Routes.PROFILE_SETUP) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onChangeEmail = { navController.popBackStack() },
            )
        }
        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(settings, onDone = { navController.goToMain() })
        }
        composable(
            Routes.MAIN,
            enterTransition = { fadeIn(tween(DURATION)) + scaleIn(tween(DURATION), initialScale = 0.92f) },
        ) {
            MainScreen(
                settings = settings,
                onOpenChat = { userId ->
                    if (AppGraph.demo) FakeChatRepository.openChat(userId)
                    navController.navigate(Routes.chat(userId))
                },
                onOpenSettings = { page -> navController.navigate(Routes.settings(page)) },
                onLogOut = {
                    scope.launch { AppGraph.account.logout() }
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.CHAT) { entry ->
            ChatScreen(
                chatId = entry.arguments?.getString("chatId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) { entry ->
            val page = SettingsPage.valueOf(entry.arguments?.getString("page") ?: SettingsPage.PROFILE.name)
            val back: () -> Unit = { navController.popBackStack() }
            when (page) {
                SettingsPage.PROFILE -> ProfileEditScreen(settings, back)
                SettingsPage.APPEARANCE -> AppearanceScreen(settings, back)
                SettingsPage.PRIVACY -> PrivacyScreen(back)
                SettingsPage.DEVICES -> DevicesScreen(back)
                SettingsPage.NOTIFICATIONS -> NotificationsScreen(settings, back)
                SettingsPage.SERVER -> ServerScreen(settings, back)
            }
        }
    }
}

private fun NavHostController.goToMain() = navigate("main") {
    popUpTo(0) { inclusive = true }
}
