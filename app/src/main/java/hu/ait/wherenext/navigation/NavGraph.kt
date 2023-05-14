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
                navController.navigate(Screen.Main.route + "/${0.0}/${0.0}")
            })
        }
        composable(Screen.Main.route + "/{latitude}/{longitude}",
            arguments = listOf(
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType }
            )

        ) {
            val latitude = it.arguments?.getFloat("latitude")
            val longitude = it.arguments?.getFloat("longitude")

            if (latitude != null && longitude != null) {
                MainScreen(
                    navController = navController,
                    latitude = latitude.toDouble(),
                    longitude = longitude.toDouble()
                )
            }


        }

        composable(Screen.Messages.route + "/{currentLocationPressed}",
            arguments = listOf(
                navArgument("currentLocationPressed") { type = NavType.BoolType }
            )

            ) {

            val currentLocationPressed = it.arguments?.getBoolean("currentLocationPressed")

            if (currentLocationPressed != null) {
                MessagesScreen(
                    navController = navController,
                    currentLocationPressed = currentLocationPressed
                )
            }


        }

        composable(Screen.WritePin.route + "/{latitude}/{longitude}/{currentLocationPressed}",
            arguments = listOf(
                navArgument("latitude") { type = NavType.FloatType },
                navArgument("longitude") { type = NavType.FloatType },
                navArgument("currentLocationPressed") { type = NavType.BoolType }

            )
        ) {

            val latitude = it.arguments?.getFloat("latitude")
            val longitude = it.arguments?.getFloat("longitude")
            val currentLocationPressed = it.arguments?.getBoolean("currentLocationPressed")
            if (latitude != null && longitude != null && currentLocationPressed != null) {
                WritePinScreen(
                    latitude = latitude.toDouble(),
                    longitude = longitude.toDouble(),
                    currLocationPressed = currentLocationPressed,
                    onWritePinSuccess = {
                        navController.popBackStack(
                            Screen.Main.route + "/${latitude.toDouble()}/${longitude.toDouble()}",
                            false
                        )
                    },
                    navController = navController
                )
            }
        }
    }
}