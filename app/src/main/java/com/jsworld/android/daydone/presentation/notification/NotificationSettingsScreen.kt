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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.BuildConfig
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
            title = { Text("테스트 알림") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::onTestResultShown) { Text("확인") }
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
            DayDoneTopBar(title = "알림", onBack = onBack)

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
                                title = "아침 권장 금액",
                                desc = "오늘 써도 괜찮은 금액을 알려드려요",
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
                                title = "저녁 기록 안내",
                                desc = "그날 기록이 없을 때만 한 번 알려드려요",
                                checked = settings.eveningEnabled,
                                onCheckedChange = onEveningChange
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                            SwitchRow(
                                title = "내일 나갈 돈",
                                desc = "저축·고정비 출금 전날 저녁에 알려드려요",
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
                                title = "보류함 30일",
                                desc = "보류한 물건마다 딱 한 번",
                                checked = settings.heldPurchaseEnabled,
                                onCheckedChange = onHeldChange
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                            SwitchRow(
                                title = "기간 결산 리포트",
                                desc = "새 기간 첫날 아침",
                                checked = settings.periodReportEnabled,
                                onCheckedChange = onReportChange
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                            SwitchRow(
                                title = "큰 지출이 있는 달 안내",
                                desc = "명절·가정의 달처럼 지출이 몰리는 달",
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
                            Text("지금 테스트 알림 보내기 (개발용)")
                        }
                    }
                }

                item {
                    Text(
                        text = "🔒 알림을 처음 켤 때만 권한을 물어봐요. 안 켜면 아무것도 묻지 않아요.\n" +
                                "앱이 넘겼다고 다그치는 알림은 보내지 않아요.\n" +
                                "같은 시각에 알릴 게 여러 개면 한 건으로 합쳐서 보내드려요.",
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
                label = { Text("${hour}시") }
            )
        }
    }
}
