package dev.linguago.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.linguago.core.ui.components.LinguaIcons
import dev.linguago.core.ui.feedback.FeedbackCue
import dev.linguago.core.ui.feedback.LocalInteractionFeedback
import dev.linguago.core.ui.feedback.rememberInteractionFeedback
import dev.linguago.core.ui.theme.LinguaTokens
import dev.linguago.core.ui.theme.linguaExtendedColors
import dev.linguago.core.ui.util.rememberAnimationsEnabled
import dev.linguago.feature.home.HomeScreen
import dev.linguago.feature.lesson.LessonScreen
import dev.linguago.feature.onboarding.OnboardingScreen
import dev.linguago.feature.profile.ProfileScreen
import dev.linguago.feature.review.ReviewScreen
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
        BottomDestination(Routes.REVIEW_TAB, R.string.tab_review, LinguaIcons.CardsRes),
        BottomDestination(Routes.PROFILE, R.string.tab_profile, LinguaIcons.ProfileRes),
        BottomDestination(Routes.SETTINGS, R.string.tab_settings, LinguaIcons.SettingsRes),
    )

/**
 * Shell de la aplicación: NavHost + barra inferior de 4 destinos (ADR-048).
 * El destino inicial lo decide la preferencia de curso (onboarding u home).
 * El badge de Repaso refleja las tarjetas vencidas del curso activo.
 */
@Composable
fun LinguaApp(
    startDestination: String = Routes.ONBOARDING,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    shellViewModel: AppShellViewModel = hiltViewModel(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentBase = currentRoute?.substringBefore('?')
    val showBottomBar = BOTTOM_DESTINATIONS.any { destination -> destination.route.substringBefore('?') == currentBase }
    val dueReviews by shellViewModel.dueReviews.collectAsStateWithLifecycle()
    val feedback = rememberInteractionFeedback()
    val animations = rememberAnimationsEnabled()

    CompositionLocalProvider(LocalInteractionFeedback provides feedback) {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                if (showBottomBar) {
                    LinguaBottomBar(
                        currentRoute = currentRoute,
                        dueReviews = dueReviews,
                        onRouteSelected = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
                enterTransition = { tabEnter(animations) },
                exitTransition = { tabExit(animations) },
                popEnterTransition = { tabEnter(animations) },
                popExitTransition = { tabExit(animations) },
            ) {
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onCourseSelected = { _ ->
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(
                        onLessonClick = { courseId, lessonId ->
                            navController.navigate(Routes.lesson(courseId, lessonId))
                        },
                        onStartReview = { courseId ->
                            navController.navigate(Routes.review(courseId))
                        },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(
                    route = Routes.LESSON,
                    enterTransition = { pushEnter(animations) },
                    exitTransition = { pushExit(animations) },
                    popEnterTransition = { popEnter(animations) },
                    popExitTransition = { popExit(animations) },
                ) {
                    LessonScreen(
                        onExit = { navController.popBackStack() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onOpenSync = { navController.navigate(Routes.SYNC) },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(Routes.SYNC) {
                    SyncScreen(
                        onBack = { navController.popBackStack() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
                composable(
                    route = Routes.REVIEW,
                    arguments =
                        listOf(
                            navArgument("courseId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
                    enterTransition = { pushEnter(animations) },
                    exitTransition = { pushExit(animations) },
                    popEnterTransition = { popEnter(animations) },
                    popExitTransition = { popExit(animations) },
                ) {
                    ReviewScreen(
                        onExit = { navController.popBackStack() },
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

private fun tabEnter(animations: Boolean): EnterTransition = fadeIn(animationSpec = tween(durationMillis = if (animations) 220 else 0))

private fun tabExit(animations: Boolean): ExitTransition = fadeOut(animationSpec = tween(durationMillis = if (animations) 160 else 0))

private fun pushEnter(animations: Boolean): EnterTransition =
    if (animations) {
        slideInHorizontally(animationSpec = tween(durationMillis = 260)) { it } +
            fadeIn(animationSpec = tween(durationMillis = 260))
    } else {
        fadeIn(animationSpec = tween(durationMillis = 0))
    }

private fun pushExit(animations: Boolean): ExitTransition =
    if (animations) {
        slideOutHorizontally(animationSpec = tween(durationMillis = 220)) { -it / 4 } +
            fadeOut(animationSpec = tween(durationMillis = 180))
    } else {
        fadeOut(animationSpec = tween(durationMillis = 0))
    }

private fun popEnter(animations: Boolean): EnterTransition = tabEnter(animations)

private fun popExit(animations: Boolean): ExitTransition =
    if (animations) {
        slideOutHorizontally(animationSpec = tween(durationMillis = 240)) { it } +
            fadeOut(animationSpec = tween(durationMillis = 180))
    } else {
        fadeOut(animationSpec = tween(durationMillis = 0))
    }

/** Barra inferior con íconos de identidad propia (I5). */
@Composable
private fun LinguaBottomBar(
    currentRoute: String?,
    dueReviews: Int,
    onRouteSelected: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extended = linguaExtendedColors()
    val feedback = LocalInteractionFeedback.current
    val largeFont = LocalDensity.current.fontScale >= 1.5f

    NavigationBar(
        containerColor = colors.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        BOTTOM_DESTINATIONS.forEach { destination ->
            val selected = currentRoute?.substringBefore('?') == destination.route.substringBefore('?')
            val isReview = destination.route == Routes.REVIEW_TAB
            NavigationBarItem(
                selected = selected,
                onClick = {
                    feedback.play(FeedbackCue.TAP)
                    onRouteSelected(destination.route)
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (isReview && dueReviews > 0) {
                                Badge(containerColor = LinguaTokens.Destructive) {
                                    Text(
                                        text = dueReviews.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onError,
                                    )
                                }
                            }
                        },
                    ) {
                        Image(
                            painter = painterResource(destination.iconRes),
                            contentDescription = stringResource(destination.labelRes),
                            modifier = Modifier.size(24.dp),
                            colorFilter =
                                ColorFilter.tint(
                                    if (selected) extended.primaryInk else colors.onSurfaceVariant,
                                ),
                        )
                    }
                },
                label =
                    if (largeFont) {
                        null
                    } else {
                        {
                            Text(
                                stringResource(destination.labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) extended.primaryInk else colors.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    },
                alwaysShowLabel = !largeFont,
                colors =
                    NavigationBarItemDefaults.colors(
                        indicatorColor = colors.primary.copy(alpha = 0.12f),
                    ),
            )
        }
    }
}
