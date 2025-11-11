package com.snappet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.snappet.core.PetController
import com.snappet.media.MediaController
import com.snappet.persistence.PersistenceRepository
import com.snappet.store.StoreRepository
import com.snappet.ui.screens.*
import com.snappet.utils.Logger

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Selector : Screen("selector")
    object Shop : Screen("shop")
    object Inventory : Screen("inventory")
    object Settings : Screen("settings")
    object Debug : Screen("debug")
}

@Composable
fun SnapPetNavigation(
    petController: PetController,
    storeRepository: StoreRepository,
    mediaController: MediaController,
    persistenceRepository: PersistenceRepository,
    logger: Logger
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                petController = petController,
                mediaController = mediaController,
                persistenceRepository = persistenceRepository
            )
        }

        composable(Screen.Selector.route) {
            SelectorScreen(
                navController = navController,
                petController = petController,
                persistenceRepository = persistenceRepository
            )
        }

        composable(Screen.Shop.route) {
            ShopScreen(
                navController = navController,
                storeRepository = storeRepository,
                persistenceRepository = persistenceRepository
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(
                navController = navController,
                storeRepository = storeRepository
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(Screen.Debug.route) {
            DebugScreen(
                navController = navController,
                logger = logger
            )
        }
    }
}
