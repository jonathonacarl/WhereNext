package hu.ait.wherenext.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import hu.ait.wherenext.ui.screen.login.LoginScreen
import hu.ait.wherenext.ui.screen.main.MainScreen
import hu.ait.wherenext.ui.screen.messages.MessagesScreen
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
                navController = navController,
                onWriteNewPostClick = {
                    navController.navigate(Screen.WritePin.route +"/${0.0}/${0.0}")
                }
            )
        }
        
        composable(Screen.Messages.route) {
            MessagesScreen(navController = navController, onWriteNewPostClick = {
                navController.navigate(Screen.WritePin.route +"/${0.0}/${0.0}")
            })
        }
        
        composable(Screen.WritePin.route + "/{latitude}/{longitude}",
            arguments = listOf(
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType }
            )
        ) {

            val latitude = it.arguments?.getLong("latitude")
            val longitude = it.arguments?.getLong("longitude")
            if (latitude != null && longitude != null) {
                WritePinScreen(
                    latitude = latitude.toDouble(),
                    longitude = longitude.toDouble(),
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
}