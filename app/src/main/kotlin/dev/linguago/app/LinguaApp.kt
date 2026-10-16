package dev.linguago.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.linguago.core.ui.components.LinguaIcons
import dev.linguago.feature.auth.AuthScreen
import dev.linguago.feature.home.HomeScreen
import dev.linguago.feature.lesson.LessonScreen
import dev.linguago.feature.onboarding.OnboardingScreen
import dev.linguago.feature.profile.ProfileScreen
import dev.linguago.feature.settings.SettingsScreen
import dev.linguago.feature.sync.SyncScreen

private data class BottomDestination(
    val route: String,
    val labelRes: Int,
    val iconRes: Int,
)

private val BOTTOM_DESTINATIONS =
    listOf(
        BottomDestination(Routes.HOME, R.string.tab_home, LinguaIcons.HomeRes),
        BottomDestination(Routes.PROFILE, R.string.tab_profile, LinguaIcons.ProfileRes),
        BottomDestination(Routes.SETTINGS, R.string.tab_settings, LinguaIcons.SettingsRes),
    )

/**
 * Shell de la aplicación: NavHost + barra inferior. Onboarding es el
 * destino inicial en Fase 0; el flujo final conectará la home cuando exista
 * progreso guardado.
 */
@Composable
fun LinguaApp(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in BOTTOM_DESTINATIONS.map { it.route }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                LinguaBottomBar(
                    currentRoute = currentRoute,
                    onRouteSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { inclusive = route == Routes.HOME }
                            launchSingleTop = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ONBOARDING,
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onCourseSelected = { courseId ->
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(Routes.HOME) {
                HomeScreen(streakDays = 0, modifier = Modifier.padding(innerPadding))
            }
            composable(Routes.LESSON) {
                LessonScreen(modifier = Modifier.padding(innerPadding))
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    streakDays = 0,
                    totalXp = 0,
                    lessonsCompleted = 0,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onOpenSync = { navController.navigate(Routes.SYNC) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(Routes.AUTH) {
                AuthScreen(modifier = Modifier.padding(innerPadding))
            }
            composable(Routes.SYNC) {
                SyncScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

/** Barra inferior con íconos de identidad propia (I5). */
@Composable
private fun LinguaBottomBar(
    currentRoute: String?,
    onRouteSelected: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(
        containerColor = colors.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        BOTTOM_DESTINATIONS.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onRouteSelected(destination.route) },
                icon = {
                    Image(
                        painter = painterResource(destination.iconRes),
                        contentDescription = stringResource(destination.labelRes),
                        modifier = Modifier.size(24.dp),
                        colorFilter =
                            ColorFilter.tint(
                                if (selected) colors.primary else colors.onSurfaceVariant,
                            ),
                    )
                },
                label = { Text(stringResource(destination.labelRes)) },
                alwaysShowLabel = true,
                colors =
                    NavigationBarItemDefaults.colors(
                        indicatorColor = colors.primary.copy(alpha = 0.12f),
                    ),
            )
        }
    }
}
