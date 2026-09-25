package com.droidspec.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.droidspec.R
import com.droidspec.core.di.AppContainer
import com.droidspec.core.di.AppViewModelFactory
import com.droidspec.domain.model.SensorSpec
import com.droidspec.presentation.screens.BatteryScreen
import com.droidspec.presentation.screens.BenchmarkScreen
import com.droidspec.presentation.screens.CameraScreen
import com.droidspec.presentation.screens.CpuScreen
import com.droidspec.presentation.screens.DashboardScreen
import com.droidspec.presentation.screens.DeviceScreen
import com.droidspec.presentation.screens.DisplayScreen
import com.droidspec.presentation.screens.GpuScreen
import com.droidspec.presentation.screens.HardwareTestsScreen
import com.droidspec.presentation.screens.LiveMonitorScreen
import com.droidspec.presentation.screens.MemoryScreen
import com.droidspec.presentation.screens.NetworkScreen
import com.droidspec.presentation.screens.RefurbScreen
import com.droidspec.presentation.screens.ReportsScreen
import com.droidspec.presentation.screens.SearchScreen
import com.droidspec.presentation.screens.SensorDetailScreen
import com.droidspec.presentation.screens.SensorsScreen
import com.droidspec.presentation.screens.SettingsScreen
import com.droidspec.presentation.screens.StorageScreen
import com.droidspec.presentation.screens.ThermalScreen
import com.droidspec.presentation.viewmodel.BatteryViewModel
import com.droidspec.presentation.viewmodel.BenchmarkViewModel
import com.droidspec.presentation.viewmodel.CameraViewModel
import com.droidspec.presentation.viewmodel.CpuViewModel
import com.droidspec.presentation.viewmodel.DashboardViewModel
import com.droidspec.presentation.viewmodel.DeviceViewModel
import com.droidspec.presentation.viewmodel.DisplayViewModel
import com.droidspec.presentation.viewmodel.GpuViewModel
import com.droidspec.presentation.viewmodel.LiveMonitorViewModel
import com.droidspec.presentation.viewmodel.MemoryViewModel
import com.droidspec.presentation.viewmodel.NetworkViewModel
import com.droidspec.presentation.viewmodel.RefurbViewModel
import com.droidspec.presentation.viewmodel.ReportsViewModel
import com.droidspec.presentation.viewmodel.SearchViewModel
import com.droidspec.presentation.viewmodel.SensorDetailViewModel
import com.droidspec.presentation.viewmodel.SensorsViewModel
import com.droidspec.presentation.viewmodel.SettingsViewModel
import com.droidspec.presentation.viewmodel.StorageViewModel
import com.droidspec.presentation.viewmodel.ThermalViewModel
import kotlinx.coroutines.launch

object Routes {
    const val DASHBOARD = "dashboard"
    const val DEVICE = "device"
    const val CPU = "cpu"
    const val GPU = "gpu"
    const val MEMORY = "memory"
    const val STORAGE = "storage"
    const val BATTERY = "battery"
    const val THERMAL = "thermal"
    const val DISPLAY = "display"
    const val CAMERA = "camera"
    const val SENSORS = "sensors"
    const val SENSOR_DETAIL = "sensor_detail"
    const val NETWORK = "network"
    const val REFURB = "refurb"
    const val TESTS = "tests"
    const val LIVE = "live"
    const val BENCHMARK = "benchmark"
    const val REPORTS = "reports"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
}

/** Holds the tapped sensor between the list and the detail destinations. */
object SensorSelection {
    var current: SensorSpec? = null
}

private data class NavItem(val route: String, val labelRes: Int, val icon: ImageVector)

private val allItems = listOf(
    NavItem(Routes.DASHBOARD, R.string.nav_dashboard, Icons.Default.Dashboard),
    NavItem(Routes.DEVICE, R.string.nav_device, Icons.Default.Info),
    NavItem(Routes.CPU, R.string.nav_cpu, Icons.Default.Build),
    NavItem(Routes.GPU, R.string.nav_gpu, Icons.Default.Smartphone),
    NavItem(Routes.MEMORY, R.string.nav_memory, Icons.Default.Memory),
    NavItem(Routes.STORAGE, R.string.nav_storage, Icons.Default.Storage),
    NavItem(Routes.BATTERY, R.string.nav_battery, Icons.Default.BatteryFull),
    NavItem(Routes.THERMAL, R.string.nav_thermal, Icons.Default.LocalFireDepartment),
    NavItem(Routes.DISPLAY, R.string.nav_display, Icons.Default.AspectRatio),
    NavItem(Routes.CAMERA, R.string.nav_camera, Icons.Default.CameraAlt),
    NavItem(Routes.SENSORS, R.string.nav_sensors, Icons.Default.Sensors),
    NavItem(Routes.NETWORK, R.string.nav_network, Icons.Default.Wifi),
    NavItem(Routes.REFURB, R.string.nav_refurb, Icons.Default.Warning),
    NavItem(Routes.TESTS, R.string.nav_tests, Icons.Default.CheckCircle),
    NavItem(Routes.LIVE, R.string.nav_live_monitor, Icons.Default.Speed),
    NavItem(Routes.BENCHMARK, R.string.nav_benchmark, Icons.Default.Assessment),
    NavItem(Routes.REPORTS, R.string.nav_reports, Icons.Default.Description),
    NavItem(Routes.SEARCH, R.string.nav_search, Icons.Default.Search),
    NavItem(Routes.SETTINGS, R.string.nav_settings, Icons.Default.Settings)
)

private val bottomRoutes = setOf(
    Routes.DASHBOARD, Routes.DEVICE, Routes.SENSORS, Routes.SEARCH, Routes.SETTINGS
)

private val topLevelRoutes = bottomRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(container: AppContainer, factory: AppViewModelFactory) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Routes.DASHBOARD

    BoxWithConstraints {
        val wide = maxWidth >= 840.dp
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail {
                    Spacer(Modifier.height(8.dp))
                    allItems.forEach { item ->
                        NavigationRailItem(
                            selected = currentRoute == item.route,
                            onClick = { nav.navigateTop(item.route) },
                            icon = { Icon(item.icon, stringResource(item.labelRes)) },
                            label = { Text(stringResource(item.labelRes)) }
                        )
                    }
                }
                AppScaffold(
                    nav, container, factory, currentRoute,
                    showBottomBar = false,
                    onMenu = null
                )
            }
        } else {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        allItems.forEach { item ->
                            NavigationDrawerItem(
                                label = { Text(stringResource(item.labelRes)) },
                                selected = currentRoute == item.route,
                                icon = { Icon(item.icon, contentDescription = null) },
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    nav.navigateTop(item.route)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            ) {
                AppScaffold(
                    nav, container, factory, currentRoute,
                    showBottomBar = true,
                    onMenu = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}

private fun NavHostController.navigateTop(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(Routes.DASHBOARD) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    nav: NavHostController,
    container: AppContainer,
    factory: AppViewModelFactory,
    currentRoute: String,
    showBottomBar: Boolean,
    onMenu: (() -> Unit)?
) {
    val titleRes = allItems.firstOrNull { it.route == currentRoute }?.labelRes ?: R.string.app_name

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    when {
                        onMenu != null && currentRoute in topLevelRoutes ->
                            IconButton(onClick = onMenu) {
                                Icon(Icons.Default.Menu, contentDescription = null)
                            }
                        currentRoute !in topLevelRoutes ->
                            IconButton(onClick = { nav.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null)
                            }
                        else -> Unit
                    }
                }
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    allItems.filter { it.route in bottomRoutes }.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = { nav.navigateTop(item.route) },
                            icon = { Icon(item.icon, stringResource(item.labelRes)) },
                            label = {
                                Text(
                                    text = stringResource(item.labelRes),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.DASHBOARD) {
                val vm: DashboardViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                DashboardScreen(state)
            }
            composable(Routes.DEVICE) {
                val vm: DeviceViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                DeviceScreen(state)
            }
            composable(Routes.CPU) {
                val vm: CpuViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                CpuScreen(state)
            }
            composable(Routes.GPU) {
                val vm: GpuViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                GpuScreen(state)
            }
            composable(Routes.MEMORY) {
                val vm: MemoryViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                MemoryScreen(state)
            }
            composable(Routes.STORAGE) {
                val vm: StorageViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                StorageScreen(state)
            }
            composable(Routes.BATTERY) {
                val vm: BatteryViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                BatteryScreen(state)
            }
            composable(Routes.THERMAL) {
                val vm: ThermalViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                ThermalScreen(state)
            }
            composable(Routes.DISPLAY) {
                val vm: DisplayViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                DisplayScreen(state)
            }
            composable(Routes.CAMERA) {
                val vm: CameraViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                CameraScreen(state)
            }
            composable(Routes.SENSORS) {
                val vm: SensorsViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                SensorsScreen(state) { type ->
                    SensorSelection.current = state.data?.firstOrNull { it.type == type }
                    nav.navigate(Routes.SENSOR_DETAIL)
                }
            }
            composable(Routes.SENSOR_DETAIL) {
                val sensor = SensorSelection.current
                val sensorType = sensor?.type ?: -1
                val vm: SensorDetailViewModel = viewModel(
                    key = "sensor_detail_${'$'}sensorType",
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            SensorDetailViewModel(container.sensorsDataSource, sensorType) as T
                    }
                )
                val values by vm.values.collectAsStateWithLifecycle()
                SensorDetailScreen(sensor, values)
            }
            composable(Routes.NETWORK) {
                val vm: NetworkViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                NetworkScreen(state)
            }
            composable(Routes.REFURB) {
                val vm: RefurbViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                RefurbScreen(state)
            }
            composable(Routes.TESTS) {
                HardwareTestsScreen()
            }
            composable(Routes.LIVE) {
                val vm: LiveMonitorViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                LiveMonitorScreen(state)
            }
            composable(Routes.BENCHMARK) {
                val vm: BenchmarkViewModel = viewModel(factory = factory)
                val state by vm.state.collectAsStateWithLifecycle()
                BenchmarkScreen(state, onStart = vm::start, onCancel = vm::cancel)
            }
            composable(Routes.REPORTS) {
                val vm: ReportsViewModel = viewModel(factory = factory)
                val busy by vm.busy.collectAsStateWithLifecycle()
                val text by vm.text.collectAsStateWithLifecycle()
                val format by vm.exportFormat.collectAsStateWithLifecycle()
                val saved by vm.savedMessage.collectAsStateWithLifecycle()
                ReportsScreen(
                    busy = busy,
                    text = text,
                    exportFormat = format,
                    savedMessage = saved,
                    onGenerate = vm::generate,
                    onSave = vm::save,
                    onConsumeSavedMessage = vm::consumeSavedMessage,
                    onFormatChange = vm::setExportFormat
                )
            }
            composable(Routes.SEARCH) {
                val vm: SearchViewModel = viewModel(factory = factory)
                val query by vm.query.collectAsStateWithLifecycle()
                val results by vm.results.collectAsStateWithLifecycle()
                val searching by vm.searching.collectAsStateWithLifecycle()
                SearchScreen(query, results, searching, vm::onQueryChange)
            }
            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(factory = factory)
                val settings by vm.settings.collectAsStateWithLifecycle()
                SettingsScreen(
                    settings = settings,
                    onTheme = vm::setTheme,
                    onLanguage = vm::setLanguage,
                    onRefreshMs = vm::setRefreshMs,
                    onExportFormat = vm::setExportFormat
                )
            }
        }
    }
}
