package com.rk.taskmanager.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import com.rk.components.compose.preferences.base.PreferenceTemplate
import kotlin.math.roundToInt
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rk.commons.settings.Settings
import com.rk.components.SettingsToggle
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.commons.strings

var pullToRefresh_procs by mutableStateOf(Settings.pullToRefresh_procs)

@Composable
fun ProcSettings(modifier: Modifier = Modifier) {
    PreferenceLayout(label = stringResource(strings.procs)) {
        PreferenceGroup() {
            SettingsToggle(label = stringResource(strings.pull_to_refresh), description = stringResource(strings.pull_to_refresh_desc), default = Settings.pullToRefresh_procs, showSwitch = true, sideEffect = {
                Settings.pullToRefresh_procs = it
                pullToRefresh_procs = it
            })
            SettingsToggle(label = stringResource(strings.confirm_stop), description = stringResource(strings.confirm_stop_desc), default = Settings.confirmkill, showSwitch = true, sideEffect = {
                Settings.confirmkill = it
            })
            SettingsToggle(label = stringResource(strings.default_to_process), description = stringResource(strings.default_to_process_desc), default = Settings.defaultToProcessScreen, showSwitch = true, sideEffect = {
                Settings.defaultToProcessScreen = it
            })
            SettingsToggle(label = stringResource(strings.stable_order), description = stringResource(strings.stable_order_desc), default = Settings.procStableOrder, showSwitch = true, sideEffect = {
                Settings.procStableOrder = it
            })
            SettingsToggle(label = stringResource(strings.auto_refresh), description = stringResource(strings.auto_refresh_desc), default = Settings.procAutoRefresh
                , showSwitch = true, sideEffect = {
                Settings.procAutoRefresh = it
            })

            var seconds by rememberSaveable {
                mutableFloatStateOf((Settings.procRefreshInterval / 1000f).coerceIn(1f, 10f))
            }
            PreferenceTemplate(title = {
                Text(stringResource(strings.proc_refresh_rate))
            }) {
                Text(stringResource(strings.seconds_unit, seconds.roundToInt()))
            }
            PreferenceTemplate(title = {}) {
                Slider(
                    value = seconds,
                    onValueChange = { seconds = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    onValueChangeFinished = { Settings.procRefreshInterval = seconds.roundToInt() * 1000 }
                )
            }
        }
    }
}