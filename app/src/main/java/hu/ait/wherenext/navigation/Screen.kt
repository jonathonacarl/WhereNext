package hu.ait.wherenext.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Main : Screen("main")
    object WritePin : Screen("writepin")
    
    object Messages : Screen("messages")
}