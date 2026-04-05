package com.example.tutorflow.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tutorflow.ui.screens.batches.BatchDetailScreen
import com.example.tutorflow.ui.screens.batches.BatchesScreen
import com.example.tutorflow.ui.screens.fees.FeesScreen
import com.example.tutorflow.ui.screens.home.HomeScreen
import com.example.tutorflow.ui.screens.reports.ReportsScreen
import com.example.tutorflow.viewmodel.BatchViewModel
import com.example.tutorflow.viewmodel.FeeViewModel
import com.example.tutorflow.viewmodel.HomeViewModel
import com.example.tutorflow.viewmodel.ReportViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Rounded.Home)
    data object Batches : Screen("batches", "Batches", Icons.Rounded.Groups)
    data object Fees : Screen("fees", "Fees", Icons.Rounded.Receipt)
    data object Reports : Screen("reports", "Reports", Icons.Rounded.Assessment)
}

private val bottomNavItems = listOf(Screen.Home, Screen.Batches, Screen.Fees, Screen.Reports)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Shared ViewModels scoped to activity
    val homeViewModel: HomeViewModel = viewModel()
    val batchViewModel: BatchViewModel = viewModel()
    val feeViewModel: FeeViewModel = viewModel()
    val reportViewModel: ReportViewModel = viewModel()

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    tonalElevation = 4.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(screen.icon, contentDescription = screen.label)
                            },
                            label = {
                                Text(
                                    text = screen.label,
                                    fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = homeViewModel)
            }
            composable(Screen.Batches.route) {
                BatchesScreen(
                    viewModel = batchViewModel,
                    onBatchClick = { batchId ->
                        navController.navigate("batch_detail/$batchId")
                    }
                )
            }
            composable(Screen.Fees.route) {
                FeesScreen(viewModel = feeViewModel)
            }
            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = reportViewModel)
            }
            composable(
                route = "batch_detail/{batchId}",
                arguments = listOf(navArgument("batchId") { type = NavType.LongType })
            ) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getLong("batchId") ?: return@composable
                BatchDetailScreen(
                    batchId = batchId,
                    viewModel = batchViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
