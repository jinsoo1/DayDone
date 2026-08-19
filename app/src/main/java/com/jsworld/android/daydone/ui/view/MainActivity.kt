package com.jsworld.android.daydone.ui.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsworld.android.daydone.domain.usecase.ObserveOnboardingDoneUseCase
import com.jsworld.android.daydone.presentation.challenge.ChallengeHistoryRoute
import com.jsworld.android.daydone.presentation.held.HeldPurchasesRoute
import com.jsworld.android.daydone.presentation.monthly.MonthlyRoute
import com.jsworld.android.daydone.presentation.navigation.AddType
import com.jsworld.android.daydone.presentation.navigation.VaultAddPrefill
import com.jsworld.android.daydone.presentation.notices.NoticesRoute
import com.jsworld.android.daydone.presentation.onboarding.OnboardingRoute
import com.jsworld.android.daydone.presentation.report.ReportRoute
import com.jsworld.android.daydone.presentation.settings.SettingsRoute
import com.jsworld.android.daydone.presentation.today.TodayRoute
import com.jsworld.android.daydone.presentation.vault.VaultRoute
import com.jsworld.android.daydone.ui.theme.DayDoneTheme
import com.jsworld.android.daydone.widget.refreshDayDoneWidget
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DayDoneTheme {
                DayDoneRoot()
            }
        }
    }

    /**
     * 앱을 벗어날 때 위젯을 한 번 더 갱신한다.
     *
     * 데이터가 바뀔 때마다 갱신 요청은 나가지만(DayDoneApp), 실제 합성은 WorkManager 워커가
     * 하기 때문에 홈으로 나가며 프로세스가 얼면 반영이 늦는다.
     * 홈 화면을 보러 나가는 바로 이 순간이 마지막으로 확실히 살아 있는 시점이라
     * 여기서 한 번 더 밀어준다.
     */
    override fun onStop() {
        super.onStop()
        lifecycleScope.launch { refreshDayDoneWidget(applicationContext) }
    }
}

private sealed class HomeTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Today : HomeTab("today", "오늘", Icons.Outlined.Today)
    data object Monthly : HomeTab("monthly", "월", Icons.Outlined.CalendarMonth)
    data object Vault : HomeTab("vault", "금고", Icons.Outlined.Savings)
    data object Settings : HomeTab("settings", "설정", Icons.Outlined.Settings)
}

private val homeTabs = listOf(
    HomeTab.Today,
    HomeTab.Monthly,
    HomeTab.Vault,
    HomeTab.Settings
)

private const val NOTICES_ROUTE = "notices"
private const val CHALLENGE_HISTORY_ROUTE = "challenge_history"
private const val REPORT_ROUTE = "report"
private const val HELD_PURCHASES_ROUTE = "held_purchases"

private val BarHeight = 64.dp
private val FabOverhang = 28.dp
private val CradleRadius = 30.dp
private val CenterGap = 84.dp

// FAB 파랑 → 파스텔 보라 그라데이션 (좌상단 → 우하단), 보라 비중 살짝 ↑
private val FabGradient = Brush.linearGradient(
    0.0f to Color(0xFF4F87F5),
    0.45f to Color(0xFF7C74F1),
    1.0f to Color(0xFF9067EF)
)

@HiltViewModel
class RootViewModel @Inject constructor(
    observeOnboardingDoneUseCase: ObserveOnboardingDoneUseCase
) : ViewModel() {
    val isOnboardingDone: Flow<Boolean> = observeOnboardingDoneUseCase()
}

@Composable
fun DayDoneRoot(
    rootViewModel: RootViewModel = hiltViewModel()
) {
    val onboardingDone by rootViewModel.isOnboardingDone
        .collectAsStateWithLifecycle(initialValue = null)

    // Surface 로 감싸 LocalContentColor 를 onBackground 로 내려준다.
    // (없으면 Material 기본값인 검정이 쓰여 다크 모드에서 글자가 안 보인다)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (onboardingDone) {
            null -> Unit // 로딩 중 (깜빡임 방지)
            false -> OnboardingRoute()
            true -> DayDoneHome()
        }
    }
}

@Composable
private fun DayDoneHome() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var showChooser by remember { mutableStateOf(false) }
    var pendingAdd by remember { mutableStateOf<AddType?>(null) }
    // 살까 말까 "금고에 준비하기" → 금고 탭 추가 시트 프리필
    var pendingVaultPrefill by remember { mutableStateOf<VaultAddPrefill?>(null) }
    // 금고 편집 등 전체화면 진입 시 하단바 숨김
    var fullScreen by remember { mutableStateOf(false) }

    // 공지사항 등 상세 화면에서도 하단바 숨김
    val barHidden = fullScreen ||
            currentRoute == NOTICES_ROUTE ||
            currentRoute == CHALLENGE_HISTORY_ROUTE ||
            currentRoute == HELD_PURCHASES_ROUTE ||
            currentRoute?.startsWith(REPORT_ROUTE) == true

    fun goToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = HomeTab.Today.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (barHidden) 0.dp else BarHeight)
                .navigationBarsPadding()
        ) {
            composable(HomeTab.Today.route) {
                TodayRoute(
                    pendingAdd = pendingAdd,
                    onPendingAddConsumed = { pendingAdd = null },
                    onOpenReport = { month ->
                        navController.navigate("$REPORT_ROUTE?month=$month")
                    },
                    onPrepareInVault = { title, amount ->
                        pendingVaultPrefill = VaultAddPrefill(title, amount)
                        goToTab(HomeTab.Vault.route)
                    }
                )
            }
            composable(HomeTab.Monthly.route) {
                MonthlyRoute(
                    onNavigateToReport = { month ->
                        navController.navigate(
                            if (month != null) "$REPORT_ROUTE?month=$month" else REPORT_ROUTE
                        )
                    }
                )
            }
            composable(HomeTab.Vault.route) {
                VaultRoute(
                    onFullScreenChange = { fullScreen = it },
                    pendingPrefill = pendingVaultPrefill,
                    onPendingPrefillConsumed = { pendingVaultPrefill = null },
                    onNavigateToHeldPurchases = {
                        navController.navigate(HELD_PURCHASES_ROUTE)
                    }
                )
            }
            composable(HomeTab.Settings.route) {
                SettingsRoute(
                    onNavigateToNotices = { navController.navigate(NOTICES_ROUTE) },
                    onNavigateToChallengeHistory = {
                        navController.navigate(CHALLENGE_HISTORY_ROUTE)
                    }
                )
            }
            composable(NOTICES_ROUTE) {
                NoticesRoute(onBack = { navController.popBackStack() })
            }
            composable(CHALLENGE_HISTORY_ROUTE) {
                ChallengeHistoryRoute(onBack = { navController.popBackStack() })
            }
            composable(HELD_PURCHASES_ROUTE) {
                HeldPurchasesRoute(onBack = { navController.popBackStack() })
            }
            composable(
                route = "$REPORT_ROUTE?month={month}",
                arguments = listOf(
                    navArgument("month") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                ReportRoute(onBack = { navController.popBackStack() })
            }
        }

        if (!barHidden) {
            NotchedBottomBar(
                currentRoute = currentRoute,
                onTabClick = ::goToTab,
                onAddClick = { showChooser = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showChooser) {
        AddChooserSheet(
            onDismiss = { showChooser = false },
            onSelect = { type ->
                showChooser = false
                goToTab(HomeTab.Today.route)
                pendingAdd = type
            }
        )
    }
}

private class NotchBarShape(private val cradleRadius: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { cradleRadius.toPx() }
        val cx = size.width / 2f
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(cx - r, 0f)
            arcTo(
                rect = Rect(cx - r, -r, cx + r, r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
private fun NotchedBottomBar(
    currentRoute: String?,
    onTabClick: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(BarHeight + FabOverhang)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(BarHeight),
            shape = NotchBarShape(CradleRadius),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BarTab(Modifier.weight(1f), HomeTab.Today, currentRoute, onTabClick)
                BarTab(Modifier.weight(1f), HomeTab.Monthly, currentRoute, onTabClick)
                Spacer(modifier = Modifier.width(CenterGap))
                BarTab(Modifier.weight(1f), HomeTab.Vault, currentRoute, onTabClick)
                BarTab(Modifier.weight(1f), HomeTab.Settings, currentRoute, onTabClick)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(56.dp)
                .clip(CircleShape)
                .background(FabGradient)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "추가",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun BarTab(
    modifier: Modifier,
    tab: HomeTab,
    currentRoute: String?,
    onTabClick: (String) -> Unit
) {
    val selected = currentRoute == tab.route
    val color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onTabClick(tab.route) }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChooserSheet(
    onDismiss: () -> Unit,
    onSelect: (AddType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "무엇을 추가할까요?",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            AddChooserRow(Icons.Outlined.Payments, "지출") { onSelect(AddType.EXPENSE) }
            AddChooserRow(Icons.Outlined.TrendingUp, "수익") { onSelect(AddType.INCOME) }
            AddChooserRow(Icons.Outlined.Autorenew, "저축 / 고정비") { onSelect(AddType.DEDUCTION) }
            AddChooserRow(Icons.Outlined.AccountBalanceWallet, "이번 달 예산") { onSelect(AddType.BUDGET) }

            // 살까 말까는 기록이 아니라 도구 — 구분선으로 나눠 보여준다
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline
            )

            Text(
                text = "사기 전에 — 지금 사면 하루 권장 금액이 얼마나 달라지는지 미리 봐요",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AddChooserRow(Icons.Outlined.Balance, "살까 말까?") { onSelect(AddType.PURCHASE) }
        }
    }
}

@Composable
private fun AddChooserRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
