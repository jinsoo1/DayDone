package com.jsworld.android.daydone.presentation.notification

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.BuildConfig
import com.jsworld.android.daydone.R
import com.jsworld.android.daydone.domain.model.NotificationSettings
import com.jsworld.android.daydone.ui.component.DayDoneTopBar

@Composable
fun NotificationSettingsRoute(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val testResult by viewModel.testResult.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler(onBack = onBack)

    // 권한은 **켤 때만** 물어본다. 거부하면 스위치를 되돌린다.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) viewModel.onPermissionDenied()
    }

    fun needsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    /** 하나라도 켜는 순간에만 권한 요청 */
    fun onEnable(enabled: Boolean, apply: (Boolean) -> Unit) {
        apply(enabled)
        if (enabled && needsPermission()) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    NotificationSettingsScreen(
        settings = settings,
        onBack = onBack,
        onMorningChange = { onEnable(it, viewModel::onMorningChange) },
        onEveningChange = { onEnable(it, viewModel::onEveningChange) },
        onUpcomingDeductionChange = { onEnable(it, viewModel::onUpcomingDeductionChange) },
        onHeldChange = { onEnable(it, viewModel::onHeldChange) },
        onReportChange = { onEnable(it, viewModel::onReportChange) },
        onBigSpendChange = { onEnable(it, viewModel::onBigSpendChange) },
        onMorningHourChange = viewModel::onMorningHourChange,
        onEveningHourChange = viewModel::onEveningHourChange,
        onSendTestClick = viewModel::onSendTestNotifications
    )

    testResult?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::onTestResultShown,
            title = { Text(stringResource(R.string.noti_settings_teseuteu_alrim)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::onTestResultShown) { Text(stringResource(R.string.noti_settings_hwagin)) }
            }
        )
    }
}

@Composable
fun NotificationSettingsScreen(
    settings: NotificationSettings,
    onBack: () -> Unit,
    onMorningChange: (Boolean) -> Unit,
    onEveningChange: (Boolean) -> Unit,
    onUpcomingDeductionChange: (Boolean) -> Unit,
    onHeldChange: (Boolean) -> Unit,
    onReportChange: (Boolean) -> Unit,
    onBigSpendChange: (Boolean) -> Unit,
    onMorningHourChange: (Int) -> Unit,
    onEveningHourChange: (Int) -> Unit,
    onSendTestClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DayDoneTopBar(title = stringResource(R.string.noti_settings_alrim), onBack = onBack)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp, top = 8.dp, end = 20.dp, bottom = 40.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            SwitchRow(
                                title = stringResource(R.string.noti_settings_achim_gweonjang_geumaeg),
                                desc = stringResource(R.string.noti_settings_oneul_sseodo_gwaenchanheun_geumaegeul),
                                checked = settings.morningEnabled,
                                onCheckedChange = onMorningChange
                            )
                            if (settings.morningEnabled) {
                                HourChips(
                                    hours = NotificationSettings.MORNING_HOUR_CHOICES,
                                    selected = settings.morningHour,
                                    onSelect = onMorningHourChange
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                            SwitchRow(
                                title = stringResource(R.string.noti_settings_jeonyeog_girog_annae),
                                desc = stringResource(R.string.noti_settings_geunal_girogi_eobseul_ttaeman),
                                checked = settings.eveningEnabled,
                                onCheckedChange = onEveningChange
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                            SwitchRow(
                                title = stringResource(R.string.noti_settings_naeil_nagal_don),
                                desc = stringResource(R.string.noti_settings_jeochug_gojeongbi_chulgeum_jeonnal),
                                checked = settings.upcomingDeductionEnabled,
                                onCheckedChange = onUpcomingDeductionChange
                            )
                            if (settings.anyEveningEnabled) {
                                HourChips(
                                    hours = NotificationSettings.EVENING_HOUR_CHOICES,
                                    selected = settings.eveningHour,
                                    onSelect = onEveningHourChange
                                )
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            SwitchRow(
                                title = stringResource(R.string.noti_settings_boryuham_30il),
                                desc = stringResource(R.string.noti_settings_boryuhan_mulgeonmada_ttag_han),
                                checked = settings.heldPurchaseEnabled,
                                onCheckedChange = onHeldChange
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                            SwitchRow(
                                title = stringResource(R.string.noti_settings_gigan_gyeolsan_ripoteu),
                                desc = stringResource(R.string.noti_settings_sae_gigan_cheosnal_achim),
                                checked = settings.periodReportEnabled,
                                onCheckedChange = onReportChange
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                            SwitchRow(
                                title = stringResource(R.string.noti_settings_keun_jichuli_issneun_dal),
                                desc = stringResource(R.string.noti_settings_myeongjeol_gajeongui_dalcheoreom_jichuli),
                                checked = settings.bigSpendMonthEnabled,
                                onCheckedChange = onBigSpendChange
                            )
                        }
                    }
                }

                // 디버그 빌드에서만 — 시각을 기다리지 않고 지금 보내본다
                if (BuildConfig.DEBUG) {
                    item {
                        OutlinedButton(
                            onClick = onSendTestClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.noti_settings_jigeum_teseuteu_alrim_bonaegi))
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.noti_settings_alrimeul_cheoeum_kyeol_ttaeman),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun HourChips(
    hours: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        hours.forEach { hour ->
            FilterChip(
                selected = hour == selected,
                onClick = { onSelect(hour) },
                label = { Text(stringResource(R.string.noti_settings_si, hour)) }
            )
        }
    }
}
