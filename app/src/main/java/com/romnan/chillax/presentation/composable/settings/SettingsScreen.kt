package com.romnan.chillax.presentation.composable.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.ramcosta.composedestinations.annotation.Destination
import com.romnan.chillax.BuildConfig
import com.romnan.chillax.R
import com.romnan.chillax.domain.model.ThemeMode
import com.romnan.chillax.presentation.composable.component.ScreenTitle
import com.romnan.chillax.presentation.composable.settings.component.BasicPreference
import com.romnan.chillax.presentation.composable.component.DefaultDialog
import com.romnan.chillax.presentation.composable.settings.component.SwitchPreference
import com.romnan.chillax.presentation.composable.settings.component.ThemeChooserDialog
import com.romnan.chillax.presentation.composable.settings.component.TimePickerDialog
import com.romnan.chillax.presentation.composable.theme.spacing
import com.romnan.chillax.presentation.constant.IntentConstants
import com.romnan.chillax.presentation.util.UIEvent
import com.romnan.chillax.presentation.util.asString
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar

@Composable
@Destination
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UIEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = event.uiText.asString(context)
                    )
                }

                else -> {}
            }
        }
    }

    Scaffold(scaffoldState = scaffoldState) { scaffoldPadding ->
        val scrollState = rememberScrollState()
        val themeMode = viewModel.themeMode.collectAsState()

        Column(
            modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize()
                .verticalScroll(state = scrollState)
        ) {
            ScreenTitle(text = { stringResource(id = R.string.settings) })

            BasicPreference(
                icon = {
                    when (themeMode.value) {
                        ThemeMode.System -> Icons.Filled.BrightnessMedium
                        ThemeMode.Light -> Icons.Filled.LightMode
                        ThemeMode.Dark -> Icons.Filled.DarkMode
                    }
                },
                title = { stringResource(R.string.pref_title_theme) },
                description = { themeMode.value.readableName.asString() },
                onClick = viewModel::showThemeChooser,
            )

            val bedtime = viewModel.bedtime.collectAsState()
            SwitchPreference(
                icon = {
                    if (bedtime.value.isActivated) Icons.Filled.NotificationsActive
                    else Icons.Filled.Notifications
                },
                title = { stringResource(R.string.pref_title_bedtime_reminder) },
                description = { bedtime.value.readableTime?.asString() },
                checked = { bedtime.value.isActivated },
                onClick = {
                    if (!bedtime.value.isActivated) TimePickerDialog(
                        context = context,
                        initHourOfDay = bedtime.value.calendar[Calendar.HOUR_OF_DAY],
                        initMinute = bedtime.value.calendar[Calendar.MINUTE],
                        onPicked = { hourOfDay: Int, minute: Int ->
                            viewModel.onBedtimePicked(hourOfDay = hourOfDay, minute = minute)
                        },
                    ).show() else viewModel.onTurnOffBedtime()
                },
                onCheckedChange = {
                    if (!bedtime.value.isActivated) TimePickerDialog(
                        context = context,
                        initHourOfDay = bedtime.value.calendar[Calendar.HOUR_OF_DAY],
                        initMinute = bedtime.value.calendar[Calendar.MINUTE],
                        onPicked = { hourOfDay: Int, minute: Int ->
                            viewModel.onBedtimePicked(hourOfDay = hourOfDay, minute = minute)
                        },
                    ).show() else viewModel.onTurnOffBedtime()
                },
            )

            Spacer(modifier = Modifier.padding(MaterialTheme.spacing.small))

            BasicPreference(
                icon = { Icons.Filled.ThumbUp },
                title = { stringResource(R.string.pref_title_rate_app) },
                description = { stringResource(R.string.pref_desc_rate_app) },
                onClick = {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(context.getString(R.string.url_app_listing))
                    }.let { context.startActivity(it) }
                },
            )

            BasicPreference(
                icon = { Icons.Filled.Share },
                title = { stringResource(R.string.pref_title_share_app) },
                description = { stringResource(R.string.pref_desc_share_app) },
                onClick = {
                    Intent(Intent.ACTION_SEND).apply {
                        type = IntentConstants.TYPE_PLAIN_TEXT
                        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text))
                    }.let { context.startActivity(it) }
                },
            )

            BasicPreference(
                icon = { Icons.Filled.Mail },
                title = { stringResource(R.string.pref_title_contact_support) },
                description = { stringResource(R.string.pref_desc_contact_support) },
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse(IntentConstants.TYPE_EMAIL)
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            context.getString(R.string.contact_subject),
                        )
                    }
                    context.startActivity(intent)
                },
            )

            Spacer(modifier = Modifier.padding(MaterialTheme.spacing.small))

            BasicPreference(
                icon = { Icons.Filled.Help },
                title = { stringResource(R.string.pref_title_instructions) },
                onClick = { viewModel.showAppInstructions() },
            )

            BasicPreference(
                icon = { Icons.Filled.Badge },
                title = { stringResource(R.string.pref_title_attributions) },
                onClick = { viewModel.showAttributions() },
            )

            BasicPreference(
                icon = { Icons.Filled.VerifiedUser },
                title = { stringResource(R.string.pref_title_privacy_policy) },
                onClick = {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(context.getString(R.string.url_privacy_policy))
                    }.let { context.startActivity(it) }
                },
            )

            BasicPreference(
                icon = { Icons.Filled.Info },
                title = { stringResource(R.string.pref_title_version) },
                description = { BuildConfig.VERSION_NAME },
                onClick = viewModel::onClickAppVersion,
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }

        if (viewModel.isThemeChooserVisible.collectAsState().value) ThemeChooserDialog(
            currentTheme = { themeMode.value },
            onThemeChoose = viewModel::onThemeModeChange,
            onDismissRequest = viewModel::hideThemeChooser,
        )

        if (viewModel.isAppInstructionsVisible.collectAsState().value) DefaultDialog(
            title = { stringResource(id = R.string.pref_title_instructions) },
            onDismissRequest = viewModel::hideAppInstructions
        ) {
            Text(
                text = stringResource(R.string.app_instructions),
                modifier = Modifier.padding(MaterialTheme.spacing.medium)
            )
        }

        if (viewModel.isAttributionsVisible.collectAsState().value) DefaultDialog(
            title = { stringResource(id = R.string.pref_title_attributions) },
            onDismissRequest = viewModel::hideAttributions
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(R.string.attributions_text),
                    modifier = Modifier.padding(MaterialTheme.spacing.medium),
                )
            }
        }
    }
}