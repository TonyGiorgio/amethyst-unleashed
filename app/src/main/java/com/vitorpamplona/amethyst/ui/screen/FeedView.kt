package com.vitorpamplona.amethyst.ui.screen

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.ui.note.NoteCompose
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@Composable
fun RefresheableFeedView(
    viewModel: FeedViewModel,
    routeForLastRead: String?,
    enablePullRefresh: Boolean = true,
    scrollStateKey: String? = null,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit
) {
    RefresheableView(viewModel, enablePullRefresh) {
        SaveableFeedState(viewModel, routeForLastRead, scrollStateKey, accountViewModel, nav)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RefresheableView(
    viewModel: InvalidatableViewModel,
    enablePullRefresh: Boolean = true,
    content: @Composable () -> Unit
) {
    var refreshing by remember { mutableStateOf(false) }
    val refresh = { refreshing = true; viewModel.invalidateData(); refreshing = false }
    val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh = refresh)

    val modifier = remember {
        if (enablePullRefresh) {
            Modifier.pullRefresh(pullRefreshState)
        } else {
            Modifier
        }
    }

    Box(modifier) {
        Column {
            content()
        }

        if (enablePullRefresh) {
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = remember {
                    Modifier.align(Alignment.TopCenter)
                }
            )
        }
    }
}

@Composable
private fun SaveableFeedState(
    viewModel: FeedViewModel,
    routeForLastRead: String?,
    scrollStateKey: String? = null,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit
) {
    SaveableFeedState(viewModel, scrollStateKey) { listState ->
        RenderFeed(viewModel, accountViewModel, listState, nav, routeForLastRead)
    }
}

@Composable
fun SaveableFeedState(
    viewModel: FeedViewModel,
    scrollStateKey: String? = null,
    content: @Composable (LazyListState) -> Unit
) {
    val listState = if (scrollStateKey != null) {
        rememberForeverLazyListState(scrollStateKey)
    } else {
        rememberLazyListState()
    }

    WatchScrollToTop(viewModel, listState)

    content(listState)
}

@Composable
private fun RenderFeed(
    viewModel: FeedViewModel,
    accountViewModel: AccountViewModel,
    listState: LazyListState,
    nav: (String) -> Unit,
    routeForLastRead: String?
) {
    val feedState by viewModel.feedContent.collectAsState()

    Crossfade(
        targetState = feedState,
        animationSpec = tween(durationMillis = 100)
    ) { state ->
        when (state) {
            is FeedState.Empty -> {
                FeedEmpty {
                    viewModel.invalidateData()
                }
            }

            is FeedState.FeedError -> {
                FeedError(state.errorMessage) {
                    viewModel.invalidateData()
                }
            }

            is FeedState.Loaded -> {
                FeedLoaded(
                    state = state,
                    listState = listState,
                    routeForLastRead = routeForLastRead,
                    accountViewModel = accountViewModel,
                    nav = nav
                )
            }

            is FeedState.Loading -> {
                LoadingFeed()
            }
        }
    }
}

@Composable
private fun WatchScrollToTop(
    viewModel: FeedViewModel,
    listState: LazyListState
) {
    val scrollToTop by viewModel.scrollToTop.collectAsState()

    LaunchedEffect(scrollToTop) {
        if (scrollToTop > 0 && viewModel.scrolltoTopPending) {
            listState.scrollToItem(index = 0)
            viewModel.sentToTop()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalTime::class)
@Composable
private fun FeedLoaded(
    state: FeedState.Loaded,
    listState: LazyListState,
    routeForLastRead: String?,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit
) {
    val baseModifier = remember {
        Modifier
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = 10.dp,
            bottom = 10.dp
        ),
        state = listState
    ) {
        itemsIndexed(state.feed.value, key = { _, item -> item.idHex }) { _, item ->
            val (value, elapsed) = measureTimedValue {
                val defaultModifier = remember {
                    Modifier.fillMaxWidth().animateItemPlacement()
                }

                Row(defaultModifier) {
                    NoteCompose(
                        item,
                        routeForLastRead = routeForLastRead,
                        modifier = baseModifier,
                        isBoostedNote = false,
                        showHidden = state.showHidden.value,
                        accountViewModel = accountViewModel,
                        nav = nav
                    )
                }
            }

            Log.d("Rendering Metrics", "Complete: ${item.event?.content()?.split("\n")?.getOrNull(0)?.take(15)}.. $elapsed")
        }
    }
}

@Composable
fun LoadingFeed() {
    Column(
        Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.loading_feed))
    }
}

@Composable
fun FeedError(errorMessage: String, onRefresh: () -> Unit) {
    Column(
        Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("${stringResource(R.string.error_loading_replies)} $errorMessage")
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = onRefresh
        ) {
            Text(text = stringResource(R.string.try_again))
        }
    }
}

@Composable
fun FeedEmpty(onRefresh: () -> Unit) {
    Column(
        Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.feed_is_empty))
        OutlinedButton(onClick = onRefresh) {
            Text(text = stringResource(R.string.refresh))
        }
    }
}
