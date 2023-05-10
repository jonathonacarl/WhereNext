package hu.ait.wherenext.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hu.ait.wherenext.ui.screen.login.LoginScreen
import hu.ait.wherenext.ui.screen.main.MainScreen
import hu.ait.wherenext.ui.screen.writepin.WritePinScreen

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(onLoginSuccess = {
                // navigate to the main messages screen
                navController.navigate(Screen.Main.route)
            })
        }
        composable(Screen.Main.route) {
            MainScreen(
                onWriteNewPostClick = {
                    navController.navigate(Screen.WritePin.route)
                }
            )
        }
        composable(Screen.WritePin.route) {
            WritePinScreen(
                onWritePinSuccess = {
                    navController.popBackStack(
                        Screen.Main.route,
                        false
                    )
                }
            )
        }
    }
}