package com.romnan.chillax.presentation.composable.moods

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.romnan.chillax.R
import com.romnan.chillax.domain.model.Mood
import com.romnan.chillax.domain.model.PlayerPhase
import com.romnan.chillax.presentation.composable.component.DefaultDialog
import com.romnan.chillax.presentation.composable.component.ScreenTitle
import com.romnan.chillax.presentation.composable.moods.component.MoodItem
import com.romnan.chillax.presentation.composable.theme.spacing
import com.romnan.chillax.presentation.util.asString

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
@Destination
@RootNavGraph(start = true)
fun MoodsScreen(
    viewModel: MoodsViewModel
) {
    val scaffoldState = rememberScaffoldState()

    val state by viewModel.state.collectAsState()

    Scaffold(scaffoldState = scaffoldState) { scaffoldPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(horizontal = MaterialTheme.spacing.small),
        ) {
            item(span = { GridItemSpan(currentLineSpan = 2) }) {
                ScreenTitle(
                    text = { stringResource(id = R.string.moods) },
                    paddingValues = PaddingValues(
                        start = MaterialTheme.spacing.small,
                        top = MaterialTheme.spacing.large,
                        end = MaterialTheme.spacing.small,
                        bottom = MaterialTheme.spacing.medium,
                    ),
                )
            }

            items(
                items = state.moods,
                key = { mood: Mood -> mood.id },
            ) { mood: Mood ->
                MoodItem(
                    mood = { mood },
                    isPlaying = state.player?.phase == PlayerPhase.PLAYING && state.player?.playingMood?.id == mood.id,
                    onClickPlayOrPause = viewModel::onClickPlayOrPause,
                    onClickDelete = viewModel::onClickDeleteMood,
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(MaterialTheme.spacing.small)
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(20.dp),
                            clip = true,
                        ),
                )
            }

            item(span = { GridItemSpan(currentLineSpan = 2) }) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }
        }

        state.customMoodToDelete?.let { mood: Mood ->
            DefaultDialog(
                title = {
                    stringResource(
                        R.string.delete_x,
                        mood.readableName.asString(),
                    )
                },
                onDismissRequest = viewModel::onDismissDeleteMoodDialog,
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                Text(
                    text = stringResource(
                        R.string.are_you_sure_you_want_to_delete_x,
                        mood.readableName.asString(),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

                Button(
                    onClick = { viewModel.onClickConfirmDeleteMood(mood = mood) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .height(48.dp),
                    shape = RoundedCornerShape(100),
                ) {
                    Text(
                        text = stringResource(R.string.yes).uppercase(),
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                TextButton(
                    onClick = viewModel::onDismissDeleteMoodDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .height(48.dp),
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = MaterialTheme.colors.onSurface
                            .copy(alpha = 0.1f),
                    )
                ) {
                    Text(
                        text = stringResource(R.string.cancel).uppercase(),
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }
        }
    }
}