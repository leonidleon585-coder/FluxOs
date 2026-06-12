package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    companion object {
        var systemCrashError by mutableStateOf<String?>(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            systemCrashError = throwable.stackTraceToString()
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    val crash = systemCrashError
                    if (crash != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0A0C10))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF161822)),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Text(
                                        "Системный сбой",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Произошло исключение. Вы можете скопировать лог ошибки:",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    val localClipboard = LocalClipboardManager.current
                                    Button(
                                        onClick = {
                                            localClipboard.setText(androidx.compose.ui.text.AnnotatedString(crash))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2E3B)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Скопировать ошибку", color = Color.White, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = {
                                            systemCrashError = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Перезапустить систему", color = Color(0xFF0C0E14), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        OSSimulationWorkspace()
                    }
                }
            }
        }
    }
}

// Global utility for offset mapping
fun getIconCenter(app: AppId, fromDock: Boolean, widthDp: Dp, heightDp: Dp): DpOffset {
    if (fromDock) {
        val col = when (app) {
            AppId.PHONE -> 0
            AppId.MESSAGES -> 1
            AppId.MUSIC -> 2
            else -> 1
        }
        val xFraction = when (col) {
            0 -> 0.22f
            1 -> 0.50f
            2 -> 0.78f
            else -> 0.50f
        }
        val yFraction = 0.88f // Dock level
        return DpOffset(widthDp * xFraction, heightDp * yFraction)
    } else {
        val col = when (app) {
            AppId.SETTINGS, AppId.CLOCK -> 0
            AppId.FILES, AppId.COMPASS -> 1
            AppId.CALCULATOR, AppId.GALLERY -> 2
            AppId.CALENDAR, AppId.CAMERA -> 3
            else -> 0
        }
        val row = when (app) {
            AppId.SETTINGS, AppId.FILES, AppId.CALCULATOR, AppId.CALENDAR -> 0
            AppId.CLOCK, AppId.COMPASS, AppId.GALLERY, AppId.CAMERA -> 1
            else -> 0
        }
        val xFraction = when (col) {
            0 -> 0.15f
            1 -> 0.38f
            2 -> 0.62f
            else -> 0.85f
        }
        val yFraction = when (row) {
            0 -> 0.22f
            else -> 0.40f
        }
        return DpOffset(widthDp * xFraction, heightDp * yFraction)
    }
}

fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp {
    return start + (stop - start) * fraction
}

fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

@Composable
fun OSSimulationWorkspace() {
    val osViewModel: OSViewModel = viewModel()
    val isPowerOn by osViewModel.isPowerOn.collectAsStateWithLifecycle()
    val systemVolume by osViewModel.systemVolume.collectAsStateWithLifecycle()

    var showVolumeHUD by remember { mutableStateOf(false) }
    var hudTimerJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val scope = rememberCoroutineScope()

    fun triggerVolumeHUD() {
        showVolumeHUD = true
        hudTimerJob?.cancel()
        hudTimerJob = scope.launch {
            delay(1500)
            showVolumeHUD = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E1E2F), Color(0xFF0F0F15)),
                    center = Offset(500f, 500f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Master Container limiting scale on wide web emulators
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Specs / Desk Instructions side-panel for widescreen setups
            BoxWithConstraints {
                if (maxWidth > 700.dp) {
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .padding(end = 24.dp)
                            .wrapContentHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF151922).copy(alpha = 0.85f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF2C3240))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Simulator Lab",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Interact with the virtual phone. Tap the physical buttons on the frame edges for hardware controls:",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ListItem(
                                headlineContent = { Text("Power Button (Right Offset)", color = Color.White, fontSize = 11.sp) },
                                supportingContent = { Text("Turn phone screen on / off", color = Color.LightGray, fontSize = 10.sp) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            ListItem(
                                headlineContent = { Text("Volume Keys (Left Offset)", color = Color.White, fontSize = 11.sp) },
                                supportingContent = { Text("Change volume and show HUD feedback", color = Color.LightGray, fontSize = 10.sp) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Close apps by swiping UP on the thin white pill at the bottom center of the phone screen (No buttons!).",
                                color = Color(0xFF00FFCC),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Virtual Physical Phone Container
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .height(760.dp)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Physical Hardware buttons positioned cleanly on the RIGHT edge of the device (as seen in photo)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 1.dp, top = 175.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Vol Up Key
                    Box(
                        modifier = Modifier
                            .size(width = 5.dp, height = 45.dp)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFFFCDDCE), Color(0xFFC58E71))),
                                RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp)
                            )
                            .border(0.5.dp, Color(0xFF9F694F), RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                            .clickable {
                                osViewModel.updateVolume(systemVolume + 0.1f)
                                triggerVolumeHUD()
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Vol Down Key
                    Box(
                        modifier = Modifier
                            .size(width = 5.dp, height = 45.dp)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFFFCDDCE), Color(0xFFC58E71))),
                                RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp)
                            )
                            .border(0.5.dp, Color(0xFF9F694F), RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                            .clickable {
                                osViewModel.updateVolume(systemVolume - 0.1f)
                                triggerVolumeHUD()
                            }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    // Power Key
                    Box(
                        modifier = Modifier
                            .size(width = 5.dp, height = 66.dp)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFFFCDDCE), Color(0xFFC58E71))),
                                RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                            )
                            .border(0.5.dp, Color(0xFF9F694F), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                            .clickable {
                                osViewModel.togglePower()
                            }
                    )
                }

                // Main Polished Premium Rose Gold Frame (Rebuilt with extra realism, metal chamfers, and waterfall glasses)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 2.dp, end = 5.dp) // Perfect spacing alignment
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFFF7F2), // Specular light gleam on top chamfer
                                    Color(0xFFEAD2C3), // Soft light rose gold
                                    Color(0xFFD3A186), // Mid rose gold
                                    Color(0xFFB17354), // Deep metal shadow contour
                                    Color(0xFFF9EAE1), // Pronounced light reflection strip
                                    Color(0xFFCA9477), // Warm copper reflections
                                    Color(0xFFEAD2C3), // Low-edge soft light
                                    Color(0xFF904E2D)  // Base grounding shadow
                                )
                            ),
                            shape = RoundedCornerShape(46.dp)
                        )
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF16110F), Color(0xFF0E0B0A)) // Deep interior dark core
                            ),
                            shape = RoundedCornerShape(46.dp)
                        )
                        .padding(2.5.dp) // Bezel core divider
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFFFFF2EC).copy(alpha = 0.5f),
                                    Color(0xFF42302A)
                                )
                            ), // Inner gasket metallic highlight with high-contrast gradient
                            shape = RoundedCornerShape(44.dp)
                        )
                        .padding(1.5.dp) // Screen glass bezel margin
                        .background(Color.Black, RoundedCornerShape(42.dp)) // Pitch black solid screen bezel
                        .padding(3.dp), // Premium ultra-thin uniform black bezel
                    contentAlignment = Alignment.Center
                ) {
                    // Beautiful screen viewport clipped completely to simulate standard modern borders
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(38.dp))
                            .background(Color.Black)
                    ) {
                        if (isPowerOn) {
                            PhoneScreenContent(viewModel = osViewModel)
 
                            // Overlay Volume HUD indicator
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showVolumeHUD,
                                enter = slideInHorizontally(initialOffsetX = { -150 }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { -150 }) + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 12.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.width(36.dp).height(120.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .weight(1f)
                                                .background(Color.DarkGray, RoundedCornerShape(2.dp)),
                                            contentAlignment = Alignment.BottomStart
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(systemVolume)
                                                    .background(Color(0xFF00FFCC), RoundedCornerShape(2.dp))
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${(systemVolume * 100).toInt()}%",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Completely asleep state
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = "Off",
                                        tint = Color.DarkGray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("FluxOS Offline", color = Color.DarkGray, fontSize = 12.sp)
                                }
                            }
                        }

                        // Premium Real-time 3D Curved Glass Glare & Waterfall Screen Edge Overlay
                        // Drawn on top of the viewport (both awake and asleep locks) to maximize fidelity
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // 1. Left waterfall side reflection cylinder
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Black.copy(alpha = 0.5f),      // Edge border shadow
                                        0.3f to Color.White.copy(alpha = 0.08f),     // First cylinder light bend
                                        0.65f to Color.White.copy(alpha = 0.24f),    // Sharp specular waterfall reflection line
                                        0.82f to Color.White.copy(alpha = 0.03f),    // Gentle highlight fading
                                        1.0f to Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = 14.dp.toPx()
                                ),
                                topLeft = Offset(0f, 0f),
                                size = androidx.compose.ui.geometry.Size(14.dp.toPx(), h)
                            )

                            // 2. Right waterfall side reflection cylinder
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Transparent,
                                        0.18f to Color.White.copy(alpha = 0.03f),   // Gentle highlight start
                                        0.35f to Color.White.copy(alpha = 0.24f),   // Sharp specular waterfall reflection line
                                        0.7f to Color.White.copy(alpha = 0.08f),    // Second cylinder light bend
                                        1.0f to Color.Black.copy(alpha = 0.5f)      // Edge shadow transition
                                    ),
                                    startX = w - 14.dp.toPx(),
                                    endX = w
                                ),
                                topLeft = Offset(w - 14.dp.toPx(), 0f),
                                size = androidx.compose.ui.geometry.Size(14.dp.toPx(), h)
                            )

                            // 3. Luxurious swept diagonal curved reflection across top glass surface
                            val reflectionPath = Path().apply {
                                moveTo(-50f, h * 0.08f)
                                cubicTo(w * 0.25f, h * 0.04f, w * 0.85f, h * 0.32f, w + 50f, h * 0.35f)
                                lineTo(w + 50f, h * 0.37f)
                                cubicTo(w * 0.85f, h * 0.34f, w * 0.25f, h * 0.06f, -50f, h * 0.10f)
                                close()
                            }
                            drawPath(
                                path = reflectionPath,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.16f),
                                        Color.White.copy(alpha = 0.04f)
                                    )
                                )
                            )

                            // Secondary ambient glass glare spanning lower section
                            val lowerReflectionPath = Path().apply {
                                moveTo(-50f, h * 0.72f)
                                cubicTo(w * 0.32f, h * 0.74f, w * 0.78f, h * 0.90f, w + 50f, h * 0.92f)
                                lineTo(w + 50f, h * 0.93f)
                                cubicTo(w * 0.78f, h * 0.91f, w * 0.32f, h * 0.75f, -50f, h * 0.73f)
                                close()
                            }
                            drawPath(
                                path = lowerReflectionPath,
                                color = Color.White.copy(alpha = 0.06f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneScreenContent(viewModel: OSViewModel) {
    val isLockedState by viewModel.isScreenLocked.collectAsStateWithLifecycle()
    val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()
    val customLocalWallpaper by viewModel.customLocalWallpaper.collectAsStateWithLifecycle()
    val activeApp by viewModel.activeApp.collectAsStateWithLifecycle()

    val springDamping by viewModel.springDamping.collectAsStateWithLifecycle()
    val springStiffness by viewModel.springStiffness.collectAsStateWithLifecycle()

    // Smooth lock-screen sliding unlock progress (0f when locked, 1f when fully unlocked)
    val unlockProgress by animateFloatAsState(
        targetValue = if (isLockedState) 0f else 1f,
        animationSpec = spring(
            dampingRatio = springDamping * 0.9f, // Allow slightly more overshoot when unlocking
            stiffness = springStiffness * 0.85f  // Keep smooth uncoil weight
        )
    )

    // Parallel app launching/closing window maps
    val activeWindows = remember { mutableStateMapOf<AppId, Animatable<Float, AnimationVector1D>>() }
    val scope = rememberCoroutineScope()
    
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val appIconCenters = remember { mutableStateMapOf<String, DpOffset>() }
    val localDensity = LocalDensity.current

    val onIconPositioned = { app: AppId, fromDock: Boolean, coords: LayoutCoordinates ->
        if (activeApp == null) {
            rootCoordinates?.let { root ->
                if (coords.isAttached && root.isAttached) {
                    val positionInRoot = root.localPositionOf(coords, Offset(coords.size.width / 2f, coords.size.height / 2f))
                    val offsetDp = DpOffset(
                        x = with(localDensity) { positionInRoot.x.toDp() },
                        y = with(localDensity) { positionInRoot.y.toDp() }
                    )
                    appIconCenters[if (fromDock) "dock_${app}" else "desk_${app}"] = offsetDp
                }
            }
        }
    }
    
    val isSwipingApp by viewModel.isSwipingApp.collectAsStateWithLifecycle()
    val appSwipeProgress by viewModel.appSwipeProgress.collectAsStateWithLifecycle()
    val appSwipeDragY by viewModel.appSwipeDragY.collectAsStateWithLifecycle()
    val appOpenedFromDockMap by viewModel.appOpenedFromDockMap.collectAsStateWithLifecycle()

    // Elastic snap-back LaunchedEffect for interrupted home swipes
    LaunchedEffect(isSwipingApp) {
        if (!isSwipingApp && activeApp != null) {
            val animatable = activeWindows[activeApp!!]
            if (animatable != null && animatable.value != 1f) {
                animatable.snapTo((1f - appSwipeProgress).coerceIn(0f, 1f))
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = springDamping,
                        stiffness = springStiffness
                    )
                )
            }
        }
    }

    LaunchedEffect(activeApp) {
        if (activeApp != null) {
            val opened = activeApp!!
            if (!activeWindows.containsKey(opened)) {
                activeWindows[opened] = Animatable(0f)
            } else {
                if (activeWindows[opened]?.value == 1f) {
                    activeWindows[opened]?.snapTo(0f)
                }
            }
            // Launch parallel spring animations on the persistent coroutine scope!
            scope.launch {
                activeWindows[opened]?.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = springDamping,
                        stiffness = springStiffness
                    )
                )
            }
            // Concurrently close other windows in parallel on the persistent scope
            activeWindows.keys.toList().forEach { app ->
                if (app != opened) {
                    val animatable = activeWindows[app]
                    if (animatable != null && animatable.targetValue > 0f) {
                        scope.launch {
                            animatable.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = springDamping,
                                    stiffness = springStiffness
                                )
                            )
                            if (activeApp != app) {
                                activeWindows.remove(app)
                            }
                        }
                    }
                }
            }
        } else {
            // Close all windows concurrently in parallel
            activeWindows.keys.toList().forEach { app ->
                val animatable = activeWindows[app]
                if (animatable != null && animatable.targetValue > 0f) {
                    scope.launch {
                        animatable.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = springDamping,
                                stiffness = springStiffness
                            )
                        )
                        if (activeApp != app) {
                            activeWindows.remove(app)
                        }
                    }
                }
            }
            // Clear interactive flags
            viewModel.setSwipingApp(false)
            viewModel.updateAppSwipeProgress(0f)
        }
    }

    // Dynamic background matching theme
    val wallpaperBrush = remember(wallpaper, customLocalWallpaper) {
        when (wallpaper) {
            WallpaperType.AURORA -> Brush.verticalGradient(
                listOf(
                    Color(0xFF813DDE), // Deep premium violet at top
                    Color(0xFFDC4191), // Glowing magenta-pink mid
                    Color(0xFFFF8533), // Radiant orbital fire orange
                    Color(0xFFFFF2DC)  // Soft warm peach-cream base
                )
            )
            WallpaperType.SPACE -> Brush.linearGradient(listOf(Color(0xFF110E24), Color(0xFF281C4F), Color(0xFF6A1B9A)))
            WallpaperType.EMERALD -> Brush.verticalGradient(listOf(Color(0xFF021B10), Color(0xFF0F5232), Color(0xFF1E824C)))
            WallpaperType.NEON -> Brush.radialGradient(listOf(Color(0xFF3B0B3B), Color(0xFF1F0C2F), Color(0xFF0F0418)))
            WallpaperType.LOCAL -> {
                if (customLocalWallpaper != null) {
                    Brush.verticalGradient(customLocalWallpaper!!.map { Color(it) })
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF2E3138),
                            Color(0xFF1D1F23),
                            Color(0xFF101112)
                        )
                    )
                }
            }
        }
    }

    // Outer screen base container with selected wallpaper
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(wallpaperBrush)
            .onGloballyPositioned { rootCoordinates = it }
    ) {
        val wDp = maxWidth
        val hDp = maxHeight

        // Beautiful orbital crescent sphere glass curve drawing
        if (wallpaper == WallpaperType.AURORA) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val arcPath1 = Path().apply {
                    moveTo(-w * 0.15f, h * 0.18f)
                    cubicTo(
                        w * 0.35f, h * 0.22f,
                        w * 0.72f, h * 0.42f,
                        w * 1.15f, h * 0.52f
                    )
                }

                drawPath(
                    path = arcPath1,
                    color = Color(0xFFFF8A65).copy(alpha = 0.22f),
                    style = Stroke(width = 40f, cap = StrokeCap.Round)
                )

                drawPath(
                    path = arcPath1,
                    color = Color(0xFFFFD54F).copy(alpha = 0.45f),
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )

                drawPath(
                    path = arcPath1,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )

                val arcPath2 = Path().apply {
                    moveTo(w * 0.15f, h * 0.46f)
                    cubicTo(
                        w * 0.45f, h * 0.63f,
                        w * 0.68f, h * 0.79f,
                        w * 0.82f, h * 0.86f
                    )
                }
                drawPath(
                    path = arcPath2,
                    color = Color(0xFFFFD54F).copy(alpha = 0.22f),
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = arcPath2,
                    color = Color.White.copy(alpha = 0.45f),
                    style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                )
            }
        }

        // Star particle noise canvas overlay if Wallpaper is SPACE
        if (wallpaper == WallpaperType.SPACE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rand = Random(42)
                for (i in 0..30) {
                    val x = rand.nextFloat() * size.width
                    val y = rand.nextFloat() * size.height
                    val radius = rand.nextFloat() * 3f + 1f
                    drawCircle(Color.White.copy(alpha = rand.nextFloat() * 0.7f + 0.3f), radius, Offset(x, y))
                }
            }
        }

        // 1. Desktop layer (scales up and fades in during unlock transition)
        if (unlockProgress > 0.01f) {
            val maxAnimProgress = if (activeWindows.isNotEmpty()) {
                activeWindows.values.maxOf { it.value }
            } else 0f

            val baseDeskScale = if (isSwipingApp) {
                1.0f - (0.05f * (1f - appSwipeProgress))
            } else {
                1.0f - (0.05f * maxAnimProgress)
            }
            val finalDeskScale = (0.88f + (0.12f * unlockProgress)) * baseDeskScale

            val animatingAppsProgress = activeWindows.mapValues { (app, animatable) ->
                if (app == activeApp && isSwipingApp) {
                    (1f - appSwipeProgress).coerceIn(0f, 1f)
                } else {
                    animatable.value
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(finalDeskScale)
                    .alpha(unlockProgress)
            ) {
                VirtualDeskGrid(
                    viewModel = viewModel,
                    animatingAppsProgress = animatingAppsProgress,
                    onIconPositioned = onIconPositioned
                ) { app, fromDock ->
                    if (!activeWindows.containsKey(app)) {
                        activeWindows[app] = Animatable(0f)
                    }
                    viewModel.openApp(app, fromDock)
                }
            }
        }

        // 2. Parallel Active App Scaling Windows (looping over concurrently animating instances with GPU hardware transformations)
        activeWindows.forEach { (app, animatable) ->
            val progress = if (app == activeApp && isSwipingApp) {
                (1f - appSwipeProgress).coerceIn(0f, 1f)
            } else {
                animatable.value
            }
            if (progress > 0.001f || (isSwipingApp && app == activeApp)) {
                val safeProgress = progress.coerceAtLeast(0.001f)
                // Map coordinates using the specific dock/desktop launch origin
                val fromDock = appOpenedFromDockMap[app] ?: false
                val key = if (fromDock) "dock_${app}" else "desk_${app}"
                val iconCenter = appIconCenters[key] ?: getIconCenter(app, fromDock, wDp, hDp)

                val swipeDragY = if (app == activeApp && isSwipingApp) {
                    with(LocalDensity.current) { appSwipeDragY.toDp() }
                } else {
                    0.dp
                }

                // Modern 3D perspective pitch tilt for depth and fluid weight
                val pitchTilt = if (isSwipingApp && app == activeApp) {
                    -14f * appSwipeProgress
                } else {
                    lerpFloat(12f, 0f, safeProgress)
                }

                // Smoothly fade in content over the first few frames
                val curAlpha = (safeProgress * 12f).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val wPx = size.width
                            val hPx = size.height
                            if (wPx == 0f || hPx == 0f) return@graphicsLayer

                            val iconCenterXPx = iconCenter.x.toPx()
                            val iconCenterYPx = iconCenter.y.toPx()

                            // Base icon size is 54.dp
                            val iconSizePx = 54.dp.toPx()

                            // Calculate target scale values
                            val scaleX0 = iconSizePx / wPx
                            val scaleY0 = iconSizePx / hPx

                            val targetScaleX = scaleX0 + (1f - scaleX0) * progress
                            val targetScaleY = scaleY0 + (1f - scaleY0) * progress

                            val targetCenterX = iconCenterXPx + (wPx / 2f - iconCenterXPx) * progress
                            val targetCenterY = iconCenterYPx + (hPx / 2f - iconCenterYPx) * progress + swipeDragY.toPx()

                            this.scaleX = targetScaleX
                            this.scaleY = targetScaleY
                            this.translationX = targetCenterX - (wPx / 2f)
                            this.translationY = targetCenterY - (hPx / 2f)

                            this.rotationX = pitchTilt
                            this.alpha = curAlpha

                            val maxRadius = 36.dp.toPx()
                            val minRadius = 16.dp.toPx()
                            val visualRadius = minRadius + (maxRadius - minRadius) * safeProgress

                            this.clip = true
                            this.shape = RoundedCornerShape(visualRadius / targetScaleX)
                        }
                        .background(Color(0xFF12141C))
                        .clickable(enabled = false) {}
                ) {
                    AppShell(
                        app = app,
                        viewModel = viewModel,
                        contentPercent = safeProgress
                    ) { releasedSwipeProgress ->
                        val currentProgress = (1f - releasedSwipeProgress).coerceIn(0f, 1f)
                        scope.launch {
                            activeWindows[app]?.snapTo(currentProgress)
                            viewModel.closeActiveApp() // activeApp becomes null, triggering target 0f spring
                            viewModel.setSwipingApp(false) // won't trigger cancel-spring because activeApp is null
                            viewModel.updateAppSwipeProgress(0f, 0f)
                        }
                    }
                }
            }
        }

        // 3. Central status overlays / pull-downs (only shown when system is completely or mostly unlocked)
        if (unlockProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(unlockProgress)
            ) {
                SystemOverlays(viewModel)
            }
        }

        // 4. Context Menu Overlay
        val contextMenuApp by viewModel.contextMenuApp.collectAsStateWithLifecycle()
        if (contextMenuApp != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { viewModel.closeContextMenu() }
                        )
                    }
            ) {
                ContextMenuPopup(viewModel, contextMenuApp!!)
            }
        }

        // 5. Lock Screen Overlay (slides vertically up and fades out cleanly upon unlock)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = (-size.height * unlockProgress)
                    scaleX = 1.0f - (0.04f * unlockProgress)
                    scaleY = 1.0f - (0.04f * unlockProgress)
                    alpha = 1f - (unlockProgress * 1.5f).coerceIn(0f, 1f)
                }
        ) {
            VirtualLockScreen(viewModel)
        }
    }
}

// Float helper math for DP lerps
fun lerp(start: Dp, stop: Dp, fraction: Float): Dp = start + (stop - start) * fraction

@Composable
fun VirtualLockScreen(viewModel: OSViewModel) {
    var swipeOffsetY by remember { mutableStateOf(0f) }
    val animatedSwipeOffset by animateFloatAsState(
        targetValue = swipeOffsetY,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { swipeOffsetY = 0f },
                    onDragEnd = {
                        if (swipeOffsetY < -120f) { // Vertical drag upwards opens device
                            viewModel.unlockScreen()
                        }
                        swipeOffsetY = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        swipeOffsetY = (swipeOffsetY + dragAmount * 0.65f).coerceAtMost(0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val visualOffset = with(density) { animatedSwipeOffset.toDp() }

        Column(
            modifier = Modifier
                .offset(y = visualOffset)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Clock section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 70.dp)
            ) {
                Text(
                    text = "13:22",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "Thursday, June 11",
                    fontSize = 15.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))
                // Beautiful battery and network quick-look icon badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = Color(0xFF00FFCC),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Flux Sim Active", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            // Notification preview
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D26).copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF00FFCC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("FluxOS Update", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        Text("V5.2 system core successfully compiled.", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }

            // Unlock Guide Swipe bar at bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Swipe up to unlock",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(4.dp)
                        .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
@Composable
fun VirtualDeskGrid(
    viewModel: OSViewModel,
    animatingAppsProgress: Map<AppId, Float>,
    onIconPositioned: (AppId, Boolean, LayoutCoordinates) -> Unit,
    onLaunchApp: (AppId, Boolean) -> Unit
) {
    val animDurationMs by viewModel.animationSpeedMs.collectAsStateWithLifecycle()
    val showAppLabels by viewModel.showAppLabels.collectAsStateWithLifecycle()
    val useNothingIconTheme by viewModel.useNothingIconTheme.collectAsStateWithLifecycle()
    val isLauncherSettingsOpen by viewModel.isLauncherSettingsOpen.collectAsStateWithLifecycle()
    val appOpenedFromDockMap by viewModel.appOpenedFromDockMap.collectAsStateWithLifecycle()
    val activeApp by viewModel.activeApp.collectAsStateWithLifecycle()

    val getDeskIconAlpha = { app: AppId ->
        val isAnimating = animatingAppsProgress.containsKey(app)
        val openedFromDock = appOpenedFromDockMap[app] ?: false
        if (isAnimating && !openedFromDock) 0f
        else if (activeApp == app && !openedFromDock) 0f
        else 1f
    }

    val getDockIconAlpha = { app: AppId ->
        val isAnimating = animatingAppsProgress.containsKey(app)
        val openedFromDock = appOpenedFromDockMap[app] ?: false
        if (isAnimating && openedFromDock) 0f
        else if (activeApp == app && openedFromDock) 0f
        else 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, bottom = 12.dp)
    ) {
        // Desktop Wallpaper area touch capture to trigger launcher settings
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            viewModel.setLauncherSettingsOpen(true)
                        }
                    )
                }
        )

        // Mid apps Grid (2 rows, 4 columns) - Repositioned cleanly for spacious aesthetic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 36.dp, start = 14.dp, end = 14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DeskIconItem(AppId.SETTINGS, "Settings", Icons.Default.Settings, Color(0xFF607D8B), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.SETTINGS), onPositioned = { onIconPositioned(AppId.SETTINGS, false, it) }, onClick = { onLaunchApp(AppId.SETTINGS, false) }, onLongClick = { viewModel.openContextMenu(AppId.SETTINGS, it) })
                    DeskIconItem(AppId.FILES, "Files", Icons.Default.Folder, Color(0xFFFFB300), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.FILES), onPositioned = { onIconPositioned(AppId.FILES, false, it) }, onClick = { onLaunchApp(AppId.FILES, false) }, onLongClick = { viewModel.openContextMenu(AppId.FILES, it) })
                    DeskIconItem(AppId.CALCULATOR, "Calculator", Icons.Default.Calculate, Color(0xFFFB9600), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.CALCULATOR), onPositioned = { onIconPositioned(AppId.CALCULATOR, false, it) }, onClick = { onLaunchApp(AppId.CALCULATOR, false) }, onLongClick = { viewModel.openContextMenu(AppId.CALCULATOR, it) })
                    DeskIconItem(AppId.CALENDAR, "Calendar", Icons.Default.CalendarToday, Color(0xFFEA4335), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.CALENDAR), onPositioned = { onIconPositioned(AppId.CALENDAR, false, it) }, onClick = { onLaunchApp(AppId.CALENDAR, false) }, onLongClick = { viewModel.openContextMenu(AppId.CALENDAR, it) })
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DeskIconItem(AppId.CLOCK, "Clock", Icons.Default.AccessTime, Color(0xFF1E88E5), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.CLOCK), onPositioned = { onIconPositioned(AppId.CLOCK, false, it) }, onClick = { onLaunchApp(AppId.CLOCK, false) }, onLongClick = { viewModel.openContextMenu(AppId.CLOCK, it) })
                    DeskIconItem(AppId.COMPASS, "Compass", Icons.Default.Explore, Color(0xFF5856D6), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.COMPASS), onPositioned = { onIconPositioned(AppId.COMPASS, false, it) }, onClick = { onLaunchApp(AppId.COMPASS, false) }, onLongClick = { viewModel.openContextMenu(AppId.COMPASS, it) })
                    DeskIconItem(AppId.GALLERY, "Gallery", Icons.Default.Image, Color(0xFF34C759), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.GALLERY), onPositioned = { onIconPositioned(AppId.GALLERY, false, it) }, onClick = { onLaunchApp(AppId.GALLERY, false) }, onLongClick = { viewModel.openContextMenu(AppId.GALLERY, it) })
                    DeskIconItem(AppId.CAMERA, "Camera", Icons.Default.PhotoCamera, Color(0xFF555555), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.CAMERA), onPositioned = { onIconPositioned(AppId.CAMERA, false, it) }, onClick = { onLaunchApp(AppId.CAMERA, false) }, onLongClick = { viewModel.openContextMenu(AppId.CAMERA, it) })
                }
            }
        }

        // Bottom Floating Dock with Phone, SMS, and Music
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 14.dp, end = 14.dp, bottom = 16.dp)
                .fillMaxWidth()
                .height(96.dp)
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(26.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockIconItem(AppId.PHONE, Icons.Default.Phone, Color(0xFF34C759), useNothingIconTheme, showAppLabels, alpha = getDockIconAlpha(AppId.PHONE), onPositioned = { onIconPositioned(AppId.PHONE, true, it) }, onClick = { onLaunchApp(AppId.PHONE, true) }, onLongClick = { viewModel.openContextMenu(AppId.PHONE, it) })
                DockIconItem(AppId.MESSAGES, Icons.Default.Sms, Color(0xFF007AFF), useNothingIconTheme, showAppLabels, alpha = getDockIconAlpha(AppId.MESSAGES), onPositioned = { onIconPositioned(AppId.MESSAGES, true, it) }, onClick = { onLaunchApp(AppId.MESSAGES, true) }, onLongClick = { viewModel.openContextMenu(AppId.MESSAGES, it) })
                DockIconItem(AppId.MUSIC, Icons.Default.MusicNote, Color(0xFFFF2D55), useNothingIconTheme, showAppLabels, alpha = getDockIconAlpha(AppId.MUSIC), onPositioned = { onIconPositioned(AppId.MUSIC, true, it) }, onClick = { onLaunchApp(AppId.MUSIC, true) }, onLongClick = { viewModel.openContextMenu(AppId.MUSIC, it) })
            }
        }
    }

    // Launcher Settings Dialog
    if (isLauncherSettingsOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setLauncherSettingsOpen(false) },
            containerColor = Color(0xFF15171F),
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Анимации (Тюнинг ОС)",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                val animationType by viewModel.animationType.collectAsStateWithLifecycle()
                val springDamping by viewModel.springDamping.collectAsStateWithLifecycle()
                val springStiffness by viewModel.springStiffness.collectAsStateWithLifecycle()

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Персонализация премиального рабочего стола. Настройте физику и шрифты под себя.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    // Toggle App Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAppLabels() }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Подписи значков",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Показывать имена приложений",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = showAppLabels,
                            onCheckedChange = { viewModel.setAppLabels(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF12141C),
                                checkedTrackColor = Color(0xFF00FFCC),
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.08f))

                    // Animation Presets
                    Text(
                        text = "Профиль анимации",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Spring iOS", "Linear Fast", "Bouncy Elastic").forEach { type ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (animationType == type) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.setAnimationType(type)
                                        when (type) {
                                            "Spring iOS" -> {
                                                viewModel.setSpringDamping(0.70f)
                                                viewModel.setSpringStiffness(600f)
                                                viewModel.setAnimationSpeed(300)
                                            }
                                            "Linear Fast" -> {
                                                viewModel.setSpringDamping(1.0f)
                                                viewModel.setSpringStiffness(1200f)
                                                viewModel.setAnimationSpeed(200)
                                            }
                                            "Bouncy Elastic" -> {
                                                viewModel.setSpringDamping(0.5f)
                                                viewModel.setSpringStiffness(250f)
                                                viewModel.setAnimationSpeed(450)
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    type,
                                    color = if (animationType == type) Color.Black else Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Damping slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Упругость (Damping)", color = Color.White, fontSize = 11.sp)
                            Text(String.format(java.util.Locale.US, "%.2f", springDamping), color = Color(0xFF00FFCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = springDamping,
                            onValueChange = { viewModel.setSpringDamping(it) },
                            valueRange = 0.15f..1.5f,
                            colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC), thumbColor = Color(0xFF00FFCC))
                        )
                    }

                    // Stiffness slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Жесткость (Stiffness)", color = Color.White, fontSize = 11.sp)
                            Text("${springStiffness.toInt()}", color = Color(0xFF00FFCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = springStiffness,
                            onValueChange = { viewModel.setSpringStiffness(it) },
                            valueRange = 20f..2000f,
                            colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC), thumbColor = Color(0xFF00FFCC))
                        )
                    }

                    // Speed slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Длительность (Speed)", color = Color.White, fontSize = 11.sp)
                            Text("${animDurationMs} ms", color = Color(0xFF00FFCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = animDurationMs.toFloat(),
                            onValueChange = { viewModel.setAnimationSpeed(it.toInt()) },
                            valueRange = 50f..1800f,
                            colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC), thumbColor = Color(0xFF00FFCC))
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.setLauncherSettingsOpen(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Готово", color = Color(0xFF12141C), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

fun getAppIconGradient(app: AppId): Brush {
    return when (app) {
        AppId.SETTINGS -> Brush.verticalGradient(listOf(Color(0xFF8E8E93), Color(0xFF636366)))
        AppId.FILES -> Brush.verticalGradient(listOf(Color(0xFFFF9500), Color(0xFFFFCC00)))
        AppId.MUSIC -> Brush.verticalGradient(listOf(Color(0xFFFF2D55), Color(0xFFFF5E7C)))
        AppId.CAMERA -> Brush.verticalGradient(listOf(Color(0xFF555555), Color(0xFF222222)))
        AppId.GALLERY -> Brush.verticalGradient(listOf(Color(0xFF34C759), Color(0xFF00ACC1)))
        AppId.PHONE -> Brush.verticalGradient(listOf(Color(0xFF34C759), Color(0xFF4CD964)))
        AppId.MESSAGES -> Brush.verticalGradient(listOf(Color(0xFF007AFF), Color(0xFF5AC8FA)))
        AppId.CALCULATOR -> Brush.verticalGradient(listOf(Color(0xFFFB9600), Color(0xFFD47C00)))
        AppId.CALENDAR -> Brush.verticalGradient(listOf(Color(0xFFEA4335), Color(0xFFFF6B6B)))
        AppId.CLOCK -> Brush.verticalGradient(listOf(Color(0xFF1C1D24), Color(0xFF0C0E14)))
        AppId.COMPASS -> Brush.verticalGradient(listOf(Color(0xFF5856D6), Color(0xFFAF52DE)))
    }
}

@Composable
fun DeskIconItem(
    id: AppId,
    title: String,
    icon: ImageVector,
    themeColor: Color,
    useNothingTheme: Boolean,
    showLabels: Boolean,
    alpha: Float = 1f,
    onPositioned: (LayoutCoordinates) -> Unit = {},
    onClick: () -> Unit,
    onLongClick: (androidx.compose.ui.geometry.Offset) -> Unit = {}
) {
    val sizeDp = if (showLabels) 66.dp else 76.dp
    val iconShape = RoundedCornerShape(15.dp)
    val backgroundBrush = remember(id) { getAppIconGradient(id) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(if (showLabels) 80.dp else 86.dp)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { _ -> onClick() },
                    onLongPress = { offset -> onLongClick(offset) }
                )
            }
            .testTag("app_${id.name.lowercase(java.util.Locale.US)}")
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = iconShape,
                    clip = false
                )
                .size(sizeDp)
                .background(backgroundBrush, iconShape)
                .onGloballyPositioned { onPositioned(it) },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent,
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(110f, 110f)
                        ),
                        shape = iconShape
                    )
            )

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(sizeDp * 0.44f)
            )
        }

        if (showLabels) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DockIconItem(
    id: AppId,
    icon: ImageVector,
    themeColor: Color,
    useNothingTheme: Boolean,
    showLabels: Boolean,
    alpha: Float = 1f,
    onPositioned: (LayoutCoordinates) -> Unit = {},
    onClick: () -> Unit,
    onLongClick: (androidx.compose.ui.geometry.Offset) -> Unit = {}
) {
    val sizeDp = if (showLabels) 66.dp else 76.dp
    val iconShape = RoundedCornerShape(15.dp)
    val backgroundBrush = remember(id) { getAppIconGradient(id) }

    Box(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                shape = iconShape,
                clip = false
            )
            .size(sizeDp)
            .alpha(alpha)
            .background(backgroundBrush, iconShape)
            .onGloballyPositioned { onPositioned(it) }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { _ -> onClick() },
                    onLongPress = { offset -> onLongClick(offset) }
                )
            }
            .testTag("dock_${id.name.lowercase(java.util.Locale.US)}"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(110f, 110f)
                    ),
                    shape = iconShape
                )
        )

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(sizeDp * 0.44f)
        )
    }
}

fun trimmed(app: AppId): String = app.name.lowercase()

@Composable
fun SystemOverlays(viewModel: OSViewModel) {
    val isWifiOn by viewModel.isWifiOn.collectAsStateWithLifecycle()
    val isBluetoothOn by viewModel.isBluetoothOn.collectAsStateWithLifecycle()
    val batteryPct by viewModel.batteryPercentage.collectAsStateWithLifecycle()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
    val trackIndex by viewModel.currentTrackIndex.collectAsStateWithLifecycle()
    val isQuickSettingsOpen by viewModel.isQuickSettingsOpen.collectAsStateWithLifecycle()
    val systemBrightness by viewModel.systemBrightness.collectAsStateWithLifecycle()

    var showExpandedNotch by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Transparent brightness dimmer overlay in response to brightness settings
        val dimProgress = 1f - systemBrightness
        if (dimProgress > 0.05f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(dimProgress)
                    .background(Color.Black)
            )
        }

        // Edge-to-edge Status Bar aligned beautifully with the new frame
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hours Time Display
            Text(
                "13:22",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(42.dp)
                    .clickable { viewModel.toggleQuickSettings() }
            )

            // Spacing to keep SpaceBetween layout perfectly aligned to corners without center camera
            Spacer(modifier = Modifier.width(9.dp))

            // Power Icons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.toggleQuickSettings() }
            ) {
                Icon(
                    imageVector = if (isWifiOn) Icons.Default.Wifi else Icons.Default.SignalWifiOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = Color(0xFF00FFCC),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    "${batteryPct}%",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Sliding Control drop-down
        AnimatedVisibility(
            visible = isQuickSettingsOpen,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ControlCenterPanel(viewModel)
        }
    }
}

@Composable
fun ControlCenterPanel(viewModel: OSViewModel) {
    val isWifiOn by viewModel.isWifiOn.collectAsStateWithLifecycle()
    val isBluetoothOn by viewModel.isBluetoothOn.collectAsStateWithLifecycle()
    val isAirplaneModeOn by viewModel.isAirplaneModeOn.collectAsStateWithLifecycle()
    val isDNDOn by viewModel.isDNDOn.collectAsStateWithLifecycle()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsStateWithLifecycle()
    val systemBrightness by viewModel.systemBrightness.collectAsStateWithLifecycle()
    val systemVolume by viewModel.systemVolume.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .padding(top = 34.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14161F).copy(alpha = 0.96f)),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Control Center",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Grid Layout of Toggles
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickSettingsTile("Wi-Fi", Icons.Default.Wifi, isWifiOn) { viewModel.toggleWifi() }
                QuickSettingsTile("Bluetooth", Icons.Default.Bluetooth, isBluetoothOn) { viewModel.toggleBluetooth() }
                QuickSettingsTile("Airplane", Icons.Default.AirplaneTicket, isAirplaneModeOn) { viewModel.toggleAirplaneMode() }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickSettingsTile("Flashlight", if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff, isFlashlightOn) { viewModel.toggleFlashlight() }
                QuickSettingsTile("Silent DND", Icons.Default.DoNotDisturb, isDNDOn) { viewModel.toggleDND() }
                QuickSettingsTile("Brightness", Icons.Default.Lock, false) { viewModel.lockScreen() }
            }

            // Sliders Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Brightness
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BrightnessMedium, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Slider(
                        value = systemBrightness,
                        onValueChange = { viewModel.updateBrightness(it) },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF00FFCC),
                            thumbColor = Color(0xFF00FFCC)
                        )
                    )
                }

                // Volume
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Slider(
                        value = systemVolume,
                        onValueChange = { viewModel.updateVolume(it) },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF00FFCC),
                            thumbColor = Color(0xFF00FFCC)
                        )
                    )
                }
            }

            // Optimize Button
            Button(
                onClick = { viewModel.setQuickSettingsOpen(false) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Collapse Panel", color = Color.White, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun QuickSettingsTile(label: String, icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(86.dp)
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF00B0FF).copy(0.18f) else Color.White.copy(0.04f)
        ),
        border = BorderStroke(1.dp, if (isActive) Color(0xFF00B0FF) else Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) Color(0xFF00B0FF) else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// Shell holding the opened App wrapper + dynamic Gesture Pill navigation
@Composable
fun AppShell(
    app: AppId,
    viewModel: OSViewModel,
    contentPercent: Float,
    onCloseApp: (Float) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // App Content Layout (renders the app screen at full size beneath overlays)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0E14))
        ) {
            if (contentPercent > 0.85f) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(28.dp)) // Safe status bar offset
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        when (app) {
                            AppId.SETTINGS -> SettingsAppScreen(viewModel)
                            AppId.FILES -> FileExplorerAppScreen(viewModel)
                            AppId.MUSIC -> MusicPlayerScreen(viewModel)
                            AppId.CAMERA -> CameraAppScreen(viewModel)
                            AppId.GALLERY -> GalleryAppScreen(viewModel)
                            AppId.PHONE -> PhoneAppScreen(viewModel)
                            AppId.MESSAGES -> MessagesAppScreen(viewModel)
                            AppId.CALCULATOR -> CalculatorAppScreen(viewModel)
                            AppId.CALENDAR -> CalendarAppScreen(viewModel)
                            AppId.CLOCK -> ClockAppScreen(viewModel)
                            AppId.COMPASS -> CompassAppScreen(viewModel)
                        }
                    }
                }
            }
        }

        // Luxury Organic Splash Screen (cross-fades seamlessly with full screen state)
        val splashAlpha = (1f - (contentPercent - 0.75f) / 0.15f).coerceIn(0f, 1f)
        if (splashAlpha > 0f) {
            val appBrandColor = when (app) {
                AppId.SETTINGS -> Color(0xFF607D8B)
                AppId.FILES -> Color(0xFFFFB300)
                AppId.MUSIC -> Color(0xFFFF2D55)
                AppId.CAMERA -> Color(0xFF555555)
                AppId.GALLERY -> Color(0xFF34C759)
                AppId.PHONE -> Color(0xFF34C759)
                AppId.MESSAGES -> Color(0xFF007AFF)
                AppId.CALCULATOR -> Color(0xFFFB9600)
                AppId.CALENDAR -> Color(0xFFEA4335)
                AppId.CLOCK -> Color(0xFF1E88E5)
                AppId.COMPASS -> Color(0xFF5856D6)
            }

            val appIcon = when (app) {
                AppId.SETTINGS -> Icons.Default.Settings
                AppId.FILES -> Icons.Default.Folder
                AppId.MUSIC -> Icons.Default.MusicNote
                AppId.CAMERA -> Icons.Default.PhotoCamera
                AppId.GALLERY -> Icons.Default.Image
                AppId.PHONE -> Icons.Default.Phone
                AppId.MESSAGES -> Icons.Default.Sms
                AppId.CALCULATOR -> Icons.Default.Calculate
                AppId.CALENDAR -> Icons.Default.CalendarToday
                AppId.CLOCK -> Icons.Default.AccessTime
                AppId.COMPASS -> Icons.Default.Explore
            }

            val useNothingTheme by viewModel.useNothingIconTheme.collectAsStateWithLifecycle()
            val splashBg = if (useNothingTheme) Color(0xFF16181F) else appBrandColor

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(splashAlpha)
                    .background(splashBg),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(elevation = 16.dp, shape = CircleShape, clip = false)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        .border(
                            width = 0.8.dp,
                            color = Color.White.copy(alpha = 0.20f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = appIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }
        }

        // Interaction Blocker logic during open/close animations to prevent rogue tap events
        if (contentPercent < 1.0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { }
                    }
            )
        }

        // Bottom Safe Navigation Pill Gesture Overlay (Worked cleanly over all application overlays)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(36.dp)
        ) {
            val screenHPx = with(LocalDensity.current) { 620.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        var cumulativeDragY = 0f
                        detectVerticalDragGestures(
                            onDragStart = { 
                                cumulativeDragY = 0f 
                                viewModel.setSwipingApp(true)
                                viewModel.updateAppSwipeProgress(0f)
                            },
                            onDragEnd = {
                                val progress = (kotlin.math.abs(cumulativeDragY) / screenHPx).coerceIn(0f, 1f)
                                if (cumulativeDragY < -100f || progress > 0.20f) {
                                    val swipeProg = viewModel.appSwipeProgress.value
                                    onCloseApp(swipeProg)
                                } else {
                                    viewModel.setSwipingApp(false)
                                    viewModel.updateAppSwipeProgress(0f, 0f)
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                cumulativeDragY += dragAmount
                                if (cumulativeDragY < 0) {
                                    val progress = (kotlin.math.abs(cumulativeDragY) / screenHPx).coerceIn(0f, 1f)
                                    viewModel.updateAppSwipeProgress(progress, cumulativeDragY)
                                }
                            }
                        )
                    }
                    .clickable {
                        onCloseApp(0f)
                    },
                contentAlignment = Alignment.Center
            ) {
                val appSwipeProgress by viewModel.appSwipeProgress.collectAsStateWithLifecycle()
                val isSwipingApp by viewModel.isSwipingApp.collectAsStateWithLifecycle()
                
                val targetPillWidth = lerpDp(100.dp, 60.dp, if (isSwipingApp) appSwipeProgress else 0f)
                val targetPillHeight = lerpDp(4.dp, 6.dp, if (isSwipingApp) appSwipeProgress else 0f)
                val targetPillColor = if (isSwipingApp) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.8f)

                val pillWidth by animateDpAsState(targetValue = targetPillWidth, animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f))
                val pillHeight by animateDpAsState(targetValue = targetPillHeight, animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f))
                val pillColor by animateColorAsState(targetValue = targetPillColor, animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f))

                Box(
                    modifier = Modifier
                        .width(pillWidth)
                        .height(pillHeight)
                        .background(pillColor, CircleShape)
                )
            }
        }
    }
}

// 1. Settings App Screen
@Composable
fun SettingsAppScreen(viewModel: OSViewModel) {
    val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()
    val speedMs by viewModel.animationSpeedMs.collectAsStateWithLifecycle()
    val showAppLabels by viewModel.showAppLabels.collectAsStateWithLifecycle()
    val useNothingIconTheme by viewModel.useNothingIconTheme.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var isOptimizing by remember { mutableStateOf(false) }
    var optProgress by remember { mutableStateOf(0f) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Settings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Interactive Wallpapers", color = Color(0xFF00FFCC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WallpaperChip("Aurora Sky", wallpaper == WallpaperType.AURORA) { viewModel.setWallpaper(WallpaperType.AURORA) }
                        WallpaperChip("Nebula Space", wallpaper == WallpaperType.SPACE) { viewModel.setWallpaper(WallpaperType.SPACE) }
                        WallpaperChip("Emerald Field", wallpaper == WallpaperType.EMERALD) { viewModel.setWallpaper(WallpaperType.EMERALD) }
                        WallpaperChip("Dark Violet", wallpaper == WallpaperType.NEON) { viewModel.setWallpaper(WallpaperType.NEON) }
                        WallpaperChip("Local Photo", wallpaper == WallpaperType.LOCAL) { viewModel.setWallpaper(WallpaperType.LOCAL) }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Launcher Customization", color = Color(0xFF00FFCC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Show Icon Names", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("When disabled, icons grow larger", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = showAppLabels,
                            onCheckedChange = { viewModel.setAppLabels(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00FFCC),
                                checkedTrackColor = Color(0xFF00FFCC).copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.White.copy(0.08f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Monochromatic Icons", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Apply modern dark vector icons shell", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = useNothingIconTheme,
                            onCheckedChange = { viewModel.setNothingIconTheme(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00FFCC),
                                checkedTrackColor = Color(0xFF00FFCC).copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Animation Speed Scaling", color = Color(0xFF00FFCC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Select a standard scale to view zoom details:", color = Color.LightGray, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SpeedChip("Rapid (150ms)", speedMs == 150) { viewModel.setAnimationSpeed(150) }
                        SpeedChip("Standard (350ms)", speedMs == 350) { viewModel.setAnimationSpeed(350) }
                        SpeedChip("Cinematic (1200ms)", speedMs == 1200) { viewModel.setAnimationSpeed(1200) }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Device Optimization Engine", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isOptimizing) {
                        LinearProgressIndicator(progress = optProgress, color = Color(0xFF00FFCC), modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Defragmenting Cache... ${(optProgress * 100).toInt()}%", color = Color.LightGray, fontSize = 10.sp)
                    } else {
                        Button(
                            onClick = {
                                isOptimizing = true
                                optProgress = 0f
                                scope.launch {
                                    while (optProgress < 1.0f) {
                                        delay(80)
                                        optProgress += 0.1f
                                    }
                                    isOptimizing = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Purge RAM & Optimize Storage", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("System Hardware Information", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Model Name: Flux Alpha Simulated Hardware", color = Color.White, fontSize = 10.sp)
                    Text("Virtual Processor: ARM-Cortex Quantum Core", color = Color.White, fontSize = 10.sp)
                    Text("Operating System: FluxOS v5.2 (Unix Build)", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun WallpaperChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(if (selected) Color(0xFF00FFCC) else Color.White.copy(0.04f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(label, color = if (selected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SpeedChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(if (selected) Color(0xFF00B0FF) else Color.White.copy(0.04f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(label, color = if (selected) Color.White else Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

// 2. Calculator App Screen
@Composable
fun CalculatorAppScreen(viewModel: OSViewModel) {
    var display by remember { mutableStateOf("0") }
    var runningExpression by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf<String>()) }
    var resetOnNextKey by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1016))
            .padding(16.dp)
    ) {
        // App Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFFFB9600), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Calculator", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // Calculation log history
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF161824), RoundedCornerShape(12.dp))
                .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true
            ) {
                if (history.isEmpty()) {
                    item {
                        Text("No Calculations Yet", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    items(history.reversed()) { expr ->
                        Text(expr, color = Color.LightGray.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large Output Display
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Text(runningExpression.ifEmpty { " " }, color = Color.Gray, fontSize = 13.sp, maxLines = 1)
            Text(
                display,
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Premium iOS circular interactive keypad
        val buttons = listOf(
            listOf("C", "±", "%", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "=")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { char ->
                        val isOperatorVal = char == "/" || char == "*" || char == "-" || char == "+" || char == "="
                        val isSpecial = char == "C" || char == "±" || char == "%" || char == "⌫"
                        val bgColor = when {
                            isOperatorVal -> Color(0xFFFB9600)
                            isSpecial -> Color.White.copy(alpha = 0.12f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }
                        val fgColor = when {
                            isOperatorVal -> Color.White
                            isSpecial -> Color(0xFFFB9600)
                            else -> Color.White
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.15f)
                                .background(bgColor, RoundedCornerShape(16.dp))
                                .clickable {
                                    when (char) {
                                        "C" -> {
                                            display = "0"
                                            runningExpression = ""
                                            resetOnNextKey = false
                                        }
                                        "⌫" -> {
                                            if (display.length > 1) {
                                                display = display.dropLast(1)
                                            } else {
                                                display = "0"
                                            }
                                        }
                                        "±" -> {
                                            if (display != "0") {
                                                display = if (display.startsWith("-")) display.drop(1) else "-$display"
                                            }
                                        }
                                        "%" -> {
                                            val value = display.toDoubleOrNull() ?: 0.0
                                            display = (value / 100.0).toString()
                                        }
                                        "+", "-", "*", "/" -> {
                                            runningExpression = "$display $char "
                                            resetOnNextKey = true
                                        }
                                        "=" -> {
                                            if (runningExpression.isNotEmpty()) {
                                                val parts = runningExpression.trim().split(" ")
                                                if (parts.size >= 2) {
                                                    val num1 = parts[0].toDoubleOrNull() ?: 0.0
                                                    val op = parts[1]
                                                    val num2 = display.toDoubleOrNull() ?: 0.0
                                                    val result = when (op) {
                                                        "+" -> num1 + num2
                                                        "-" -> num1 - num2
                                                        "*" -> num1 * num2
                                                        "/" -> if (num2 != 0.0) num1 / num2 else "Error"
                                                        else -> num2
                                                    }
                                                    val formattedResult = if (result is Double) {
                                                        if (result % 1.0 == 0.0) result.toLong().toString() else String.format(java.util.Locale.US, "%.4f", result).trimEnd('0').trimEnd('.')
                                                    } else {
                                                        result.toString()
                                                    }
                                                    history = history + "$runningExpression$display = $formattedResult"
                                                    display = formattedResult
                                                    runningExpression = ""
                                                    resetOnNextKey = true
                                                }
                                            }
                                        }
                                        else -> { // Digits & Decimal
                                            if (display == "0" || resetOnNextKey) {
                                                display = char
                                                resetOnNextKey = false
                                            } else {
                                                if (char == "." && display.contains(".")) {
                                                    // ignore duplicate decimal points
                                                } else {
                                                    display += char
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                char,
                                color = fgColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 2b. Calendar App Screen
@Composable
fun CalendarAppScreen(viewModel: OSViewModel) {
    var selectedDay by remember { mutableStateOf(12) }
    val daysInMonth = 30
    val startingWeekdayOffset = 4 // Friday

    val events = remember {
        mapOf(
            5 to listOf("🔥 Team Project Synch", "🚀 FluxOS Core release"),
            12 to listOf("💡 Design System Review", "🍕 Developer Pizza Night"),
            18 to listOf("📅 Hardware Sprint Demo"),
            25 to listOf("🚀 Final Production QA testing", "🌟 Antigravity engine deploy")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1016))
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFEA4335), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calendar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text("June 2026", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        // Days of Week Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                Text(day, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
            }
        }

        // Calendar Grid layout
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            var dayCounter = 1
            for (row in 0..5) {
                if (dayCounter > daysInMonth) break
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0..6) {
                        val isDayValid = (row > 0 || col >= startingWeekdayOffset) && (dayCounter <= daysInMonth)
                        if (isDayValid) {
                            val currentDay = dayCounter
                            val hasEvent = events.containsKey(currentDay)
                            val isSelected = currentDay == selectedDay

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = when {
                                            isSelected -> Color(0xFFEA4335)
                                            hasEvent -> Color.White.copy(alpha = 0.08f)
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = if (hasEvent && !isSelected) 1.dp else 0.dp,
                                        color = if (hasEvent) Color(0xFFEA4335).copy(alpha = 0.4f) else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedDay = currentDay },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        currentDay.toString(),
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected || hasEvent) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (hasEvent && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(Color(0xFFEA4335), CircleShape)
                                        )
                                    }
                                }
                            }
                            dayCounter++
                        } else {
                            Spacer(modifier = Modifier.size(36.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Schedule Event Details
        Text("Events on June $selectedDay", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF161824), RoundedCornerShape(12.dp))
                .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            val dayEvents = events[selectedDay]
            if (dayEvents == null || dayEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No events scheduled for today", color = Color.Gray, fontSize = 11.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dayEvents) { event ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                                .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp, 24.dp).background(Color(0xFFEA4335), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(event, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// 3. Music Player Screen
@Composable
fun MusicPlayerScreen(viewModel: OSViewModel) {
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
    val currentTrackIndex by viewModel.currentTrackIndex.collectAsStateWithLifecycle()

    val title = viewModel.trackTitles[currentTrackIndex]
    val artist = viewModel.trackArtists[currentTrackIndex]

    val infiniteRotation = rememberInfiniteTransition()
    val diskAngle by infiniteRotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("WavePlayer", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Simulated High-Fi Engine", color = Color.Gray, fontSize = 11.sp)
        }

        // Spinning vinyl design
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(Color(0xFF101216), CircleShape)
                .border(6.dp, Color(0xFF1E212D), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .rotate(if (isMusicPlaying) diskAngle else 0f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF151515), Color(0xFF353535), Color(0xFF1C1C1C)),
                            radius = 200f
                        ),
                        CircleShape
                    )
            ) {
                // Outer lines repeating grooves
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Color.White.copy(alpha = 0.05f), radius = size.width * 0.45f)
                    drawCircle(Color.White.copy(alpha = 0.05f), radius = size.width * 0.35f)
                    drawCircle(Color.White.copy(alpha = 0.05f), radius = size.width * 0.25f)
                }

                // Inner sticker
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(0xFFE91E63), CircleShape)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFF0C0E14), CircleShape))
                }
            }
        }

        // Title text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = artist,
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }

        // Custom Equalizer columns using custom Canvas
        Row(
            modifier = Modifier.height(30.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val pulse = rememberInfiniteTransition()
            for (i in 0..7) {
                val duration = 300 + (i * 80)
                val barProgress by pulse.animateFloat(
                    initialValue = 0.15f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 30.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(if (isMusicPlaying) barProgress else 0.1f)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFE91E63), Color(0xFFFF4081))
                                )
                            )
                    )
                }
            }
        }

        // Play controllers row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.prevTrack() }) {
                Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFE91E63), CircleShape)
            ) {
                Icon(
                    imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(onClick = { viewModel.nextTrack() }) {
                Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

// 4. Camera App Screen
@Composable
fun CameraAppScreen(viewModel: OSViewModel) {
    var selectedFilter by remember { mutableStateOf("Normal") }
    var zoomValue by remember { mutableStateOf(1f) } // Float supporting 1.0x to 100.0x
    var isShutterTriggered by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // Finders overlay
        val scanGradient = when (selectedFilter) {
            "Cyberpunk" -> listOf(Color(0xFFFC466B), Color(0xFF3F5EFB))
            "Monochrome" -> listOf(Color(0xFF222222), Color(0xFFCCCCCC))
            "Matrix" -> listOf(Color(0xFF003300), Color(0xFF00FF00))
            else -> listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF))
        }

        // Scale corresponding to the 100x zoom value
        val centerScale = 1.0f + (zoomValue / 100f) * 12f

        // Viewfinder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = scanGradient,
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width * 0.8f * centerScale
                        )
                    )

                    // Draw abstract shapes depending on zoom scale
                    drawCircle(
                        color = Color.White.copy(0.12f),
                        radius = size.width * 0.15f * centerScale,
                        center = Offset(size.width / 2f, size.height * 0.45f)
                    )

                    drawCircle(
                        color = Color.White.copy(0.06f),
                        radius = size.width * 0.35f * centerScale,
                        center = Offset(size.width / 2f, size.height * 0.45f)
                    )
                }
        ) {
            // Viewfinder Grid Lines & HUD targets
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Rule of thirds lines
                drawLine(Color.White.copy(alpha = 0.15f), Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.15f), Offset(size.width * 2f / 3f, 0f), Offset(size.width * 2f / 3f, size.height), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, size.height * 2f / 3f), Offset(size.width, size.height * 2f / 3f), strokeWidth = 1f)

                // Crosshair at the center
                drawCircle(Color.White.copy(alpha = 0.35f), radius = 12f, center = Offset(size.width / 2f, size.height / 2f))
            }

            // Top Camera Header Actions (Clean modern lens stats overlay, removed trash HDR, 60 FPS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color.Black.copy(0.5f), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ZOOM STREAK: ${String.format(java.util.Locale.US, "%.1f", zoomValue)}x", color = Color(0xFF00FFCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("SUPER ZOOM 100x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Light)
            }

            // Controls bottom panel overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Zoom Presets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ZoomIndicator("1x", zoomValue == 1f) { zoomValue = 1f }
                    ZoomIndicator("10x", zoomValue == 10f) { zoomValue = 10f }
                    ZoomIndicator("50x", zoomValue == 50f) { zoomValue = 50f }
                    ZoomIndicator("100x", zoomValue == 100f) { zoomValue = 100f }
                }

                // Fine precision zoom slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = zoomValue,
                        onValueChange = { zoomValue = it },
                        valueRange = 1f..100f,
                        colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC), thumbColor = Color(0xFF00FFCC)),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Filter selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterOptionChip("Standard", selectedFilter == "Normal") { selectedFilter = "Normal" }
                    FilterOptionChip("Cyberpunk", selectedFilter == "Cyberpunk") { selectedFilter = "Cyberpunk" }
                    FilterOptionChip("Monochrome", selectedFilter == "Monochrome") { selectedFilter = "Monochrome" }
                    FilterOptionChip("Matrix", selectedFilter == "Matrix") { selectedFilter = "Matrix" }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Physical trigger button with Gallery Shortcut selector
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Gallery shortcut icon selection
                    IconButton(
                        onClick = { viewModel.openApp(AppId.GALLERY) },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Collections, contentDescription = "Choose Photo", tint = Color.White)
                    }

                    // Large physical trigger capture circle
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(4.dp)
                            .background(Color.White, CircleShape)
                            .clickable {
                                isShutterTriggered = true
                                viewModel.takePhoto(selectedFilter)
                                scope.launch {
                                    delay(100)
                                    isShutterTriggered = false
                                }
                            }
                    )

                    // Spacer placeholder to balance layout
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
        }

        // Action camera flashback
        if (isShutterTriggered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun ZoomIndicator(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(if (selected) Color(0xFF00FFCC) else Color.White.copy(0.12f), CircleShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, color = if (selected) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FilterOptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) Color(0xFF00FFCC) else Color.LightGray,
        fontSize = 11.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(6.dp)
    )
}

// 5. Gallery App Screen
@Composable
fun GalleryAppScreen(viewModel: OSViewModel) {
    val photos by viewModel.galleryPhotos.collectAsStateWithLifecycle()
    var selectedPhoto by remember { mutableStateOf<GalleryPhoto?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Text("PixelDeck Gallery", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Simulated Storage Space", color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(14.dp))

            if (photos.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No photos taken yet.", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(photos) { photo ->
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { selectedPhoto = photo },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                            border = BorderStroke(0.5.dp, Color.White.copy(0.12f))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Draw preview
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = photo.wallGradient.map { Color(it) }
                                            )
                                        )
                                )

                                // Text metadata
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(0.6f))
                                        .padding(6.dp)
                                ) {
                                    Column {
                                        Text(photo.title, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(photo.date, color = Color.LightGray, fontSize = 7.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Complete fullscreen zoom expansion lightbox
        selectedPhoto?.let { photo ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { selectedPhoto = null }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Frame
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = photo.wallGradient.map { Color(it) }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "FILTERS APPLIED: ${photo.filterType}",
                            color = Color.White.copy(0.3f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Metadata footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF12141C))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(photo.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(if (photo.isCaptured) "SNAP" else "WALL", color = Color(0xFF00FFCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Captured: ${photo.date}", color = Color.LightGray, fontSize = 11.sp)
                        Text("Resolution: 4096 x 3072 Simulated PX (1.2 MB PNG)", color = Color.Gray, fontSize = 10.sp)

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.setCustomLocalWallpaper(photo.wallGradient)
                                selectedPhoto = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set as Wallpaper", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { selectedPhoto = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to Gallery", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// 6. Clock App Screen
@Composable
fun ClockAppScreen(viewModel: OSViewModel) {
    var activeSubTab by remember { mutableStateOf("world") } // "world" or "stopwatch"
    
    // Stopwatch state
    var stopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchMilliseconds by remember { mutableStateOf(0L) }
    var laps by remember { mutableStateOf(listOf<String>()) }
    
    // Coroutine stopwatch loop
    LaunchedEffect(stopwatchRunning) {
        if (stopwatchRunning) {
            val startTime = System.currentTimeMillis() - stopwatchMilliseconds
            while (stopwatchRunning) {
                stopwatchMilliseconds = System.currentTimeMillis() - startTime
                delay(16)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1016))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clock", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // Sub tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("world" to "World Clock", "stopwatch" to "Stopwatch").forEach { (tabId, tabName) ->
                val isSelected = activeSubTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) Color(0xFF1E88E5) else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { activeSubTab = tabId }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tabName, color = if (isSelected) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeSubTab == "world") {
            // Live-drawn Dial on Canvas
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color(0xFF161824).copy(alpha = 0.5f), CircleShape)
                    .border(2.dp, Color(0xFF1E88E5).copy(alpha = 0.3f), CircleShape)
            ) {
                val secAngle = rememberInfiniteTransition().animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(60000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerPt = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f

                    // Draw 12 dial tick markers
                    for (i in 0..11) {
                        val angleRad = (i * 30) * Math.PI / 180f
                        val start = Offset(
                            (centerPt.x + (radius - 12.dp.toPx()) * Math.sin(angleRad)).toFloat(),
                            (centerPt.y - (radius - 12.dp.toPx()) * Math.cos(angleRad)).toFloat()
                        )
                        val end = Offset(
                            (centerPt.x + (radius - 4.dp.toPx()) * Math.sin(angleRad)).toFloat(),
                            (centerPt.y - (radius - 4.dp.toPx()) * Math.cos(angleRad)).toFloat()
                        )
                        drawLine(Color(0xFF1E88E5).copy(alpha = 0.5f), start, end, strokeWidth = 2.dp.toPx())
                    }

                    // Rotating animated second hand
                    val secRad = (secAngle.value - 90f) * Math.PI / 180f
                    val secEnd = Offset(
                        (centerPt.x + (radius - 16.dp.toPx()) * Math.cos(secRad)).toFloat(),
                        (centerPt.y + (radius - 16.dp.toPx()) * Math.sin(secRad)).toFloat()
                    )
                    drawLine(Color(0xFFFF2D55), centerPt, secEnd, strokeWidth = 1.5.dp.toPx())

                    // Static hour hand
                    val hourEnd = Offset(
                        (centerPt.x + (radius * 0.5f) * Math.cos(-45f * Math.PI / 180f)).toFloat(),
                        (centerPt.y + (radius * 0.5f) * Math.sin(-45f * Math.PI / 180f)).toFloat()
                    )
                    drawLine(Color.White, centerPt, hourEnd, strokeWidth = 3.dp.toPx())

                    // Static minute hand
                    val minEnd = Offset(
                        (centerPt.x + (radius * 0.75f) * Math.cos(70f * Math.PI / 180f)).toFloat(),
                        (centerPt.y + (radius * 0.75f) * Math.sin(70f * Math.PI / 180f)).toFloat()
                    )
                    drawLine(Color.LightGray, centerPt, minEnd, strokeWidth = 2.5.dp.toPx())

                    drawCircle(Color(0xFF1E88E5), radius = 4.dp.toPx(), center = centerPt)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // World clocks list
            Text("World Cities", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val worldCities = listOf(
                    Triple("London", "UTC +0 (6:26 AM)", "Today, -3 hrs"),
                    Triple("Moscow", "UTC +3 (9:26 AM)", "Local time"),
                    Triple("Tokyo", "UTC +9 (3:26 PM)", "Today, +6 hrs"),
                    Triple("New York", "UTC -5 (1:26 AM)", "Today, -8 hrs")
                )
                items(worldCities) { (city, time, offset) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161824), RoundedCornerShape(10.dp))
                            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(city, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(offset, color = Color.Gray, fontSize = 10.sp)
                        }
                        Text(time, color = Color(0xFF1E88E5), fontSize = 15.sp, fontWeight = FontWeight.Light)
                    }
                }
            }
        } else {
            // Stopwatch view
            val formattedTime = remember(stopwatchMilliseconds) {
                val hours = (stopwatchMilliseconds / 3600000) % 24
                val minutes = (stopwatchMilliseconds / 60000) % 60
                val seconds = (stopwatchMilliseconds / 1000) % 60
                val ms = (stopwatchMilliseconds % 1000) / 10
                String.format(java.util.Locale.US, "%02d:%02d:%02d.%02d", hours, minutes, seconds, ms)
            }

            Text(
                text = formattedTime,
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            // Stopwatch Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (stopwatchRunning) {
                            laps = laps + "${laps.size + 1}. $formattedTime"
                        } else {
                            stopwatchMilliseconds = 0L
                            laps = emptyList()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (stopwatchRunning) "Lap" else "Reset", color = Color.White)
                }

                Button(
                    onClick = { stopwatchRunning = !stopwatchRunning },
                    colors = ButtonDefaults.buttonColors(containerColor = if (stopwatchRunning) Color(0xFFFF2D55) else Color(0xFF1E88E5)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (stopwatchRunning) "Pause" else "Start", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lap List
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(laps.reversed()) { lap ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Lap ${lap.substringBefore('.')}", color = Color.LightGray, fontSize = 11.sp)
                        Text(lap.substringAfter(' '), color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 7. Files App Screen
@Composable
fun FileExplorerAppScreen(viewModel: OSViewModel) {
    val currentPath by viewModel.currentPath.collectAsStateWithLifecycle()
    var openedTextFileContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Path navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Root > $currentPath",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            if (currentPath != "Root") {
                Button(
                    onClick = { viewModel.setFilePath("Root") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.08f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("Up", color = Color.White, fontSize = 9.sp)
                }
            }
        }

        Divider(color = Color.White.copy(0.12f), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))

        if (openedTextFileContent != null) {
            // Document reader viewer
            val (name, text) = openedTextFileContent!!
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(name, color = Color(0xFF00FFCC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("MOCK DOCUMENT", color = Color.Gray, fontSize = 8.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { openedTextFileContent = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Document", fontSize = 10.sp)
                    }
                }
            }
        } else {
            // Lists files of selected directory
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (currentPath) {
                    "Root" -> {
                        FolderRowItem("Documents", "System text guides") { viewModel.setFilePath("Documents") }
                        FolderRowItem("Downloads", "Grooves & audio clips") { viewModel.setFilePath("Downloads") }
                        FolderRowItem("Gallery", "Shortcuts to snaps") { viewModel.setFilePath("Gallery") }
                        FolderRowItem("System Config", "Hardware specs configs") { viewModel.setFilePath("System") }
                    }
                    "Documents" -> {
                        FileRowItem("goals_for_2026.txt", Icons.Default.Description, "420 B") {
                            openedTextFileContent = Pair(
                                "goals_for_2026.txt",
                                "FLUXOS WORKPLAN 2026:\n- Deliver pure gesture navigation on virtual frames.\n- Implement spring based scaling window mechanics.\n- Provide rich functional applications."
                            )
                        }
                        FileRowItem("secret_command_codes.txt", Icons.Default.Description, "120 B") {
                            openedTextFileContent = Pair(
                                "secret_command_codes.txt",
                                "SECRET CODES:\n- Launch FluxShell terminal and input neofetch command for ASCII outputs."
                            )
                        }
                    }
                    "Downloads" -> {
                        FileRowItem("cyberpunk_vibes.mp3", Icons.Default.AudioFile, "4.2 MB") {
                            // Automatically configures audio player and opens it
                            viewModel.togglePlayPause()
                            viewModel.openApp(AppId.MUSIC)
                        }
                    }
                    "Gallery" -> {
                        Text("Photos taken on device link directly to Gallery App. Use PixelDeck App to browse captured elements.", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp))
                    }
                    "System" -> {
                        FileRowItem("hardware_manifest.json", Icons.Default.Code, "1.8 KB") {
                            openedTextFileContent = Pair(
                                "hardware_manifest.json",
                                "{\n  \"device\": \"Flux Alpha\",\n  \"ram_gb\": 16,\n  \"core_count\": 8,\n  \"storage_gb\": 512,\n  \"screen\": \"AMOLED 90Hz\"\n}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderRowItem(name: String, support: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White.copy(0.03f), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(support, color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun FileRowItem(name: String, icon: ImageVector, size: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White.copy(0.02f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF00FFCC), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("File size: $size", color = Color.Gray, fontSize = 9.sp)
        }
    }
}

// 8. Compass App Screen
@Composable
fun CompassAppScreen(viewModel: OSViewModel) {
    var azimuthSlider by remember { mutableStateOf(210f) } // default Southwest-ish
    var isAutoRotating by remember { mutableStateOf(true) }

    LaunchedEffect(isAutoRotating) {
        if (isAutoRotating) {
            while (true) {
                azimuthSlider = (azimuthSlider + 0.35f) % 360f
                delay(16)
            }
        }
    }

    val directionString = remember(azimuthSlider) {
        val heading = (azimuthSlider + 360) % 360
        when {
            heading >= 337.5 || heading < 22.5 -> "N"
            heading >= 22.5 && heading < 67.5 -> "NE"
            heading >= 67.5 && heading < 112.5 -> "E"
            heading >= 112.5 && heading < 157.5 -> "SE"
            heading >= 157.5 && heading < 202.5 -> "S"
            heading >= 202.5 && heading < 247.5 -> "SW"
            heading >= 247.5 && heading < 292.5 -> "W"
            else -> "NW"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1016))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Explore, contentDescription = null, tint = Color(0xFF5856D6), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Compass", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // Live rotated Compass Dial Canvas
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(Color(0xFF161824).copy(alpha = 0.4f), CircleShape)
                .border(1.dp, Color(0xFF5856D6).copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerPt = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                // Outer ticks representing degree increments
                for (angle in 0..359 step 15) {
                    val finalAngle = (angle - azimuthSlider - 90) * Math.PI / 180f
                    val innerR = if (angle % 90 == 0) radius - 20.dp.toPx() else radius - 12.dp.toPx()
                    val outerR = radius - 4.dp.toPx()
                    drawLine(
                        color = if (angle % 90 == 0) Color(0xFF5856D6) else Color.White.copy(alpha = 0.25f),
                        start = Offset(
                            (centerPt.x + innerR * Math.cos(finalAngle)).toFloat(),
                            (centerPt.y + innerR * Math.sin(finalAngle)).toFloat()
                        ),
                        end = Offset(
                            (centerPt.x + outerR * Math.cos(finalAngle)).toFloat(),
                            (centerPt.y + outerR * Math.sin(finalAngle)).toFloat()
                        ),
                        strokeWidth = if (angle % 90 == 0) 2.5.dp.toPx() else 1.dp.toPx()
                    )
                }

                // Draw central needle
                val pointerAngle = (-90) * Math.PI / 180f // North always points top on phone face
                val pointerNorth = Offset(
                    (centerPt.x + (radius - 40.dp.toPx()) * Math.cos(pointerAngle)).toFloat(),
                    (centerPt.y + (radius - 40.dp.toPx()) * Math.sin(pointerAngle)).toFloat()
                )
                val pointerSouth = Offset(
                    (centerPt.x + (radius - 40.dp.toPx()) * Math.cos(pointerAngle + Math.PI)).toFloat(),
                    (centerPt.y + (radius - 40.dp.toPx()) * Math.sin(pointerAngle + Math.PI)).toFloat()
                )

                drawLine(Color(0xFFFF2D55), centerPt, pointerNorth, strokeWidth = 4.dp.toPx())
                drawLine(Color.Gray, centerPt, pointerSouth, strokeWidth = 4.dp.toPx())
                drawCircle(Color.White, radius = 6.dp.toPx(), center = centerPt)
            }
        }

        // Digital telemetry
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${azimuthSlider.toInt()}° $directionString",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            )
            Text(
                "LAT: 55° 45' 21\" N   |   LON: 37° 37' 04\" E",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Controls
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().border(0.5.dp, Color.White.copy(0.06f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Manual Heading Tuning", color = Color.White, fontSize = 11.sp)
                    Text(
                        if (isAutoRotating) "Auto Rotate On" else "Fine Adjust Active",
                        color = Color(0xFF5856D6),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = azimuthSlider,
                    onValueChange = {
                        azimuthSlider = it
                        isAutoRotating = false
                    },
                    valueRange = 0f..359f,
                    colors = SliderDefaults.colors(activeTrackColor = Color(0xFF5856D6), thumbColor = Color(0xFF5856D6))
                )

                Button(
                    onClick = { isAutoRotating = !isAutoRotating },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAutoRotating) Color(0xFF5856D6) else Color.White.copy(0.06f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (isAutoRotating) "Pause Auto Simulation" else "Resume Auto Simulation",
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// 9. Phone Screen
@Composable
fun PhoneAppScreen(viewModel: OSViewModel) {
    var dialDigits by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("dialer") } // "dialer" or "recents"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .scale(0.88f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(if (activeTab == "dialer") Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { activeTab = "dialer" }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Dialer", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .background(if (activeTab == "recents") Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(16.dp))
                        .clickable { activeTab = "recents" }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Recents", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activeTab == "recents") {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color.LightGray.copy(alpha = 0.15f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No recent calls", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Your call history is empty", color = Color.DarkGray, fontSize = 10.sp)
                }
            }
            // Spacer at bottom to match height balance
            Spacer(modifier = Modifier.height(44.dp))
        } else {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    dialDigits.ifEmpty { "Enter Number" },
                    color = if (dialDigits.isEmpty()) Color.DarkGray else Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                // Numpad Column
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("*", "0", "#")
                    )

                    for (row in keys) {
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            for (key in row) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.White.copy(0.04f), CircleShape)
                                        .border(0.5.dp, Color.LightGray.copy(0.2f), CircleShape)
                                        .clickable {
                                            if (dialDigits.length < 11) dialDigits += key
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(key, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (dialDigits.isNotEmpty()) {
                                dialDigits = dialDigits.dropLast(1)
                            }
                        },
                        modifier = Modifier.background(Color.White.copy(0.04f), CircleShape)
                    ) {
                        Icon(Icons.Default.Backspace, null, tint = Color.LightGray)
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 110.dp, height = 44.dp)
                            .background(Color(0xFF00E676), RoundedCornerShape(22.dp))
                            .clickable { dialDigits = "DIALING..." },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 10. Messages Screen
@Composable
fun MessagesAppScreen(viewModel: OSViewModel) {
    val messages = emptyList<Pair<String, String>>()

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Text("Conversations", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.LightGray.copy(alpha = 0.15f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No messages",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your inbox is completely empty",
                        color = Color.DarkGray,
                        fontSize = 10.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(messages) { msg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                        border = BorderStroke(0.5.dp, Color.White.copy(0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(Color(0xFF2979FF), CircleShape), contentAlignment = Alignment.Center) {
                                Text(msg.first.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(msg.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(msg.second, color = Color.LightGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContextMenuPopup(viewModel: OSViewModel, app: AppId) {
    var isReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(10) // small delay to trigger the spring entrance
        isReady = true
    }
    
    val popProgress by animateFloatAsState(
        targetValue = if (isReady) 1f else 0.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 800f)
    )

    val offset by viewModel.contextMenuOffset.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .graphicsLayer {
                    // Position menu slightly above or near the icon
                    val menuWidthPx = 220.dp.toPx()
                    val menuHeightPx = 140.dp.toPx()
                    
                    var targetX = offset.x
                    var targetY = offset.y - menuHeightPx - 20f
                    
                    if (targetY < 0f) targetY = offset.y + 100f
                    if (targetX + menuWidthPx > size.width) targetX = size.width - menuWidthPx - 20f
                    
                    translationX = targetX
                    translationY = targetY
                    
                    scaleX = popProgress
                    scaleY = popProgress
                    alpha = popProgress.coerceIn(0f, 1f)
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                }
                .width(220.dp)
                .background(Color(0xFF1E1E24).copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            val appName = app.name.lowercase(java.util.Locale.US).replaceFirstChar { it.uppercase() }
            Text(
                "App Info", 
                color = Color.White, 
                fontSize = 14.sp, 
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.closeContextMenu() }
                    .padding(12.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
            Text(
                "Share $appName", 
                color = Color.White, 
                fontSize = 14.sp, 
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.closeContextMenu() }
                    .padding(12.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
            Text(
                "Uninstall", 
                color = Color(0xFFFF453A), 
                fontSize = 14.sp, 
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.closeContextMenu() }
                    .padding(12.dp)
            )
        }
    }
}

