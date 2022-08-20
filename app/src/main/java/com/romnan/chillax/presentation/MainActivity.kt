package com.romnan.chillax.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.romnan.chillax.data.service.PlayerService
import com.romnan.chillax.domain.model.PlayerPhase
import com.romnan.chillax.presentation.component.BottomBar
import com.romnan.chillax.presentation.component.PlayerBottomSheet
import com.romnan.chillax.presentation.destinations.MoodsScreenDestination
import com.romnan.chillax.presentation.destinations.SettingsScreenDestination
import com.romnan.chillax.presentation.destinations.SoundsScreenDestination
import com.romnan.chillax.presentation.moods.MoodsScreen
import com.romnan.chillax.presentation.settings.SettingsScreen
import com.romnan.chillax.presentation.sounds.SoundsScreen
import com.romnan.chillax.presentation.theme.ChillaxTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            viewModel.playerState.collectLatest {
                // TODO: put this inside a use case
                Intent(this@MainActivity, PlayerService::class.java).also { intent ->

                    when (it.phase) {
                        PlayerPhase.PLAYING -> ContextCompat
                            .startForegroundService(this@MainActivity, intent)

                        PlayerPhase.PAUSED -> ContextCompat
                            .startForegroundService(this@MainActivity, intent)

                        PlayerPhase.STOPPED -> stopService(intent)
                    }
                }
            }
        }

        setContent {
            ChillaxTheme(darkTheme = true) {
                val engine = rememberNavHostEngine()
                val navController = engine.rememberNavController()

                val scope = rememberCoroutineScope()
                val scaffoldState = rememberScaffoldState()
                val bsState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
                val bsScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bsState)

                val playerState = viewModel.playerState.collectAsState().value

                LaunchedEffect(key1 = 1) {
                    viewModel.playerState.collectLatest {
                        if (it.phase == PlayerPhase.STOPPED) {
                            bsState.collapse()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    scaffoldState = scaffoldState,
                    bottomBar = { BottomBar(navController = navController) }
                ) { scaffoldPadding ->

                    val peekHeight = animateDpAsState(
                        targetValue = if (playerState.phase == PlayerPhase.STOPPED) 0.dp else 80.dp
                    )

                    BottomSheetScaffold(
                        scaffoldState = bsScaffoldState,
                        sheetPeekHeight = peekHeight.value,
                        sheetBackgroundColor = Color.Transparent,
                        modifier = Modifier
                            .padding(scaffoldPadding)
                            .fillMaxSize(),
                        sheetContent = {
                            PlayerBottomSheet(
                                playerState = playerState,
                                peekHeight = peekHeight.value,
                                isCollapsed = bsState.isCollapsed,
                                onPeekClick = { scope.launch { if (bsState.isCollapsed) bsState.expand() } },
                                onPlayPauseClick = viewModel::onPlayPauseClicked,
                                onStopClick = viewModel::onStopClicked,
                            )
                        }) { bsScaffoldPadding ->

                        DestinationsNavHost(
                            engine = engine,
                            navController = navController,
                            navGraph = NavGraphs.root,
                            modifier = Modifier.padding(bsScaffoldPadding)
                        ) {
                            composable(MoodsScreenDestination) {
                                MoodsScreen(viewModel = viewModel)
                            }

                            composable(SoundsScreenDestination) {
                                SoundsScreen(viewModel = viewModel)
                            }

                            composable(SettingsScreenDestination) {
                                SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}