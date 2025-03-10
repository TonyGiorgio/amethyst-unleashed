package com.vitorpamplona.amethyst.ui.screen.loggedIn

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.*
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vitorpamplona.amethyst.ui.buttons.ChannelFabColumn
import com.vitorpamplona.amethyst.ui.buttons.NewCommunityNoteButton
import com.vitorpamplona.amethyst.ui.buttons.NewImageButton
import com.vitorpamplona.amethyst.ui.buttons.NewNoteButton
import com.vitorpamplona.amethyst.ui.navigation.*
import com.vitorpamplona.amethyst.ui.navigation.AccountSwitchBottomSheet
import com.vitorpamplona.amethyst.ui.navigation.AppBottomBar
import com.vitorpamplona.amethyst.ui.navigation.AppNavigation
import com.vitorpamplona.amethyst.ui.navigation.AppTopBar
import com.vitorpamplona.amethyst.ui.navigation.DrawerContent
import com.vitorpamplona.amethyst.ui.navigation.Route
import com.vitorpamplona.amethyst.ui.note.UserReactionsViewModel
import com.vitorpamplona.amethyst.ui.screen.AccountState
import com.vitorpamplona.amethyst.ui.screen.AccountStateViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrChatroomListKnownFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrChatroomListNewFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrDiscoverChatFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrDiscoverCommunityFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrDiscoverLiveFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrHomeFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrHomeRepliesFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NostrVideoFeedViewModel
import com.vitorpamplona.amethyst.ui.screen.NotificationViewModel
import com.vitorpamplona.amethyst.ui.screen.ThemeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    accountViewModel: AccountViewModel,
    accountStateViewModel: AccountStateViewModel,
    themeViewModel: ThemeViewModel
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmValueChange = { it != ModalBottomSheetValue.HalfExpanded },
        skipHalfExpanded = true
    )

    val navController = rememberNavController()
    val navState = navController.currentBackStackEntryAsState()

    val nav = remember(navController) {
        { route: String ->
            scope.launch {
                if (getRouteWithArguments(navController) != route) {
                    navController.navigate(route)
                }
            }
            Unit
        }
    }

    val followLists: FollowListViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "FollowListViewModel",
        factory = FollowListViewModel.Factory(accountViewModel.account)
    )

    // Avoids creating ViewModels for performance reasons (up to 1 second delays)
    val homeFeedViewModel: NostrHomeFeedViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "NostrHomeFeedViewModel",
        factory = NostrHomeFeedViewModel.Factory(accountViewModel.account)
    )

    val repliesFeedViewModel: NostrHomeRepliesFeedViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "NostrHomeRepliesFeedViewModel",
        factory = NostrHomeRepliesFeedViewModel.Factory(accountViewModel.account)
    )

    val videoFeedViewModel: NostrVideoFeedViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "NostrVideoFeedViewModel",
        factory = NostrVideoFeedViewModel.Factory(accountViewModel.account)
    )

    val discoveryLiveFeedViewModel: NostrDiscoverLiveFeedViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "NostrDiscoveryLiveFeedViewModel",
        factory = NostrDiscoverLiveFeedViewModel.Factory(accountViewModel.account)
    )

    val discoveryCommunityFeedViewModel: NostrDiscoverCommunityFeedViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "NostrDiscoveryCommunityFeedViewModel",
        factory = NostrDiscoverCommunityFeedViewModel.Factory(accountViewModel.account)
    )

    val discoveryChatFeedViewModel: NostrDiscoverChatFeedViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "NostrDiscoveryChatFeedViewModel",
        factory = NostrDiscoverChatFeedViewModel.Factory(accountViewModel.account)
    )

    val notifFeedViewModel: NotificationViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "NotificationViewModel",
        factory = NotificationViewModel.Factory(accountViewModel.account)
    )

    val userReactionsStatsModel: UserReactionsViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "UserReactionsViewModel",
        factory = UserReactionsViewModel.Factory(accountViewModel.account)
    )

    val knownFeedViewModel: NostrChatroomListKnownFeedViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "NostrChatroomListKnownFeedViewModel",
        factory = NostrChatroomListKnownFeedViewModel.Factory(accountViewModel.account)
    )

    val newFeedViewModel: NostrChatroomListNewFeedViewModel = viewModel(
        key = accountViewModel.userProfile().pubkeyHex + "NostrChatroomListNewFeedViewModel",
        factory = NostrChatroomListNewFeedViewModel.Factory(accountViewModel.account)
    )

    val navBottomRow = remember(navController) {
        { route: Route, selected: Boolean ->
            if (!selected) {
                navController.navigate(route.base) {
                    popUpTo(Route.Home.route)
                    launchSingleTop = true
                }
            } else {
                // deals with scroll to top here to avoid passing as parameter
                // and having to deal with all recompositions with scroll to top true
                when (route.base) {
                    Route.Home.base -> {
                        homeFeedViewModel.sendToTop()
                        repliesFeedViewModel.sendToTop()
                    }
                    Route.Video.base -> {
                        videoFeedViewModel.sendToTop()
                    }
                    Route.Discover.base -> {
                        discoveryLiveFeedViewModel.sendToTop()
                        discoveryCommunityFeedViewModel.sendToTop()
                        discoveryChatFeedViewModel.sendToTop()
                    }
                    Route.Notification.base -> {
                        notifFeedViewModel.invalidateDataAndSendToTop()
                    }
                }

                navController.navigate(route.route) {
                    popUpTo(route.route)
                    launchSingleTop = true
                }
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            AccountSwitchBottomSheet(accountViewModel = accountViewModel, accountStateViewModel = accountStateViewModel)
        }
    ) {
        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colors.primaryVariant)
                .statusBarsPadding(),
            bottomBar = {
                AppBottomBar(accountViewModel, navState, navBottomRow)
            },
            topBar = {
                AppTopBar(followLists, navState, scaffoldState, accountViewModel, nav = nav)
            },
            drawerContent = {
                DrawerContent(nav, scaffoldState, sheetState, accountViewModel)
                BackHandler(enabled = scaffoldState.drawerState.isOpen) {
                    scope.launch { scaffoldState.drawerState.close() }
                }
            },
            floatingActionButton = {
                FloatingButtons(navState, accountViewModel, accountStateViewModel, nav)
            },
            scaffoldState = scaffoldState
        ) {
            Column(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) {
                AppNavigation(
                    homeFeedViewModel = homeFeedViewModel,
                    repliesFeedViewModel = repliesFeedViewModel,
                    knownFeedViewModel = knownFeedViewModel,
                    newFeedViewModel = newFeedViewModel,
                    videoFeedViewModel = videoFeedViewModel,
                    discoveryLiveFeedViewModel = discoveryLiveFeedViewModel,
                    discoveryCommunityFeedViewModel = discoveryCommunityFeedViewModel,
                    discoveryChatFeedViewModel = discoveryChatFeedViewModel,
                    notifFeedViewModel = notifFeedViewModel,
                    userReactionsStatsModel = userReactionsStatsModel,
                    navController = navController,
                    accountViewModel = accountViewModel,
                    themeViewModel = themeViewModel
                )
            }
        }
    }
}

@Composable
fun FloatingButtons(
    navEntryState: State<NavBackStackEntry?>,
    accountViewModel: AccountViewModel,
    accountStateViewModel: AccountStateViewModel,
    nav: (String) -> Unit
) {
    val accountState by accountStateViewModel.accountContent.collectAsState()

    Crossfade(targetState = accountState, animationSpec = tween(durationMillis = 100)) { state ->
        when (state) {
            is AccountState.LoggedInViewOnly -> {
                // Does nothing.
            }
            is AccountState.LoggedOff -> {
                // Does nothing.
            }
            is AccountState.LoggedIn -> {
                WritePermissionButtons(navEntryState, accountViewModel, nav)
            }
        }
    }
}

@Composable
private fun WritePermissionButtons(
    navEntryState: State<NavBackStackEntry?>,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit
) {
    val currentRoute by remember(navEntryState.value) {
        derivedStateOf {
            navEntryState.value?.destination?.route?.substringBefore("?")
        }
    }

    when (currentRoute) {
        Route.Home.base -> NewNoteButton(accountViewModel, nav)
        Route.Message.base -> ChannelFabColumn(accountViewModel, nav)
        Route.Video.base -> NewImageButton(accountViewModel, nav)
        Route.Community.base -> {
            val communityId by remember(navEntryState.value) {
                derivedStateOf {
                    navEntryState.value?.arguments?.getString("id")
                }
            }

            communityId?.let {
                NewCommunityNoteButton(it, accountViewModel, nav)
            }
        }
    }
}
