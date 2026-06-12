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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OSSimulationWorkspace()
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
            AppId.BROWSER -> 2
            AppId.CAMERA -> 3
            else -> 0
        }
        val xFraction = when (col) {
            0 -> 0.15f
            1 -> 0.38f
            2 -> 0.62f
            else -> 0.85f
        }
        val yFraction = 0.88f // Dock level
        return DpOffset(widthDp * xFraction, heightDp * yFraction)
    } else {
        val col = when (app) {
            AppId.SETTINGS, AppId.CAMERA -> 0
            AppId.FILES, AppId.GALLERY -> 1
            AppId.NOTES, AppId.TERMINAL -> 2
            AppId.MUSIC, AppId.BROWSER -> 3
            else -> 0
        }
        val row = when (app) {
            AppId.SETTINGS, AppId.FILES, AppId.NOTES, AppId.MUSIC -> 0
            AppId.CAMERA, AppId.GALLERY, AppId.TERMINAL, AppId.BROWSER -> 1
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

    // Smooth lock-screen sliding unlock progress (0f when locked, 1f when fully unlocked)
    val unlockProgress by animateFloatAsState(
        targetValue = if (isLockedState) 0f else 1f,
        animationSpec = spring(
            dampingRatio = 0.85f, // Luxury elastic slide slide
            stiffness = Spring.StiffnessLow
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
                        dampingRatio = 0.65f, // Elastic recovery rebound
                        stiffness = 260f
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
            }
            // Launch parallel spring animations
            launch {
                activeWindows[opened]?.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.76f, // Snappy & fluid like flagship HarmonyOS/HyperOS
                        stiffness = 450f
                    )
                )
            }
            // Concurrently close other windows in parallel
            activeWindows.forEach { (app, animatable) ->
                if (app != opened && animatable.targetValue > 0f) {
                    launch {
                        animatable.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = 0.8f,
                                stiffness = 350f
                            )
                        )
                        if (activeApp != app) {
                            activeWindows.remove(app)
                        }
                    }
                }
            }
        } else {
            // Close all windows concurrently in parallel
            activeWindows.forEach { (app, animatable) ->
                if (animatable.targetValue > 0f) {
                    launch {
                        if (isSwipingApp) {
                            animatable.snapTo((1f - appSwipeProgress).coerceIn(0f, 1f))
                        }
                        animatable.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = 0.82f, // Luxury deceleration decrescendo
                                stiffness = 420f
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
            val maxAnimProgress = if (activeApp != null) {
                activeWindows[activeApp]?.value ?: 0f
            } else 0f
            val baseDeskScale = if (isSwipingApp) {
                0.90f + (0.10f * appSwipeProgress)
            } else {
                1.0f - (0.10f * maxAnimProgress)
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

        // 2. Parallel Active App Scaling Windows (looping over concurrently animating instances)
        activeWindows.forEach { (app, animatable) ->
            val progress = if (app == activeApp && isSwipingApp) {
                (1f - appSwipeProgress).coerceIn(0f, 1f)
            } else {
                animatable.value
            }
            if (progress > 0.002f) {
                // Map coordinates using the specific dock/desktop launch origin
                val fromDock = appOpenedFromDockMap[app] ?: false
                val key = if (fromDock) "dock_${app}" else "desk_${app}"
                val iconCenter = appIconCenters[key] ?: getIconCenter(app, fromDock, wDp, hDp)

                val swipeDragY = if (app == activeApp && isSwipingApp) {
                    with(LocalDensity.current) { appSwipeDragY.toDp() }
                } else {
                    0.dp
                }

                val curWidth = lerpDp(54.dp, wDp, progress)
                val curHeight = lerpDp(54.dp, hDp, progress)
                val curX = lerpDp(iconCenter.x, wDp / 2f, progress)
                val curY = lerpDp(iconCenter.y, hDp / 2f, progress) + swipeDragY
                val curRadius = lerpDp(16.dp, 36.dp, progress)
                val curAlpha = lerpFloat(0f, 1f, (progress * 3f).coerceAtMost(1f))

                // Modern 3D perspective pitch tilt and scaling for depth and fluid weight
                val pitchTilt = if (isSwipingApp && app == activeApp) {
                    -14f * appSwipeProgress
                } else {
                    lerpFloat(12f, 0f, progress)
                }
                val appScale = 0.95f + (0.05f * progress)

                Box(
                    modifier = Modifier
                        .offset(
                            x = curX - (curWidth / 2f),
                            y = curY - (curHeight / 2f)
                        )
                        .size(width = curWidth, height = curHeight)
                        .graphicsLayer {
                            rotationX = pitchTilt
                            scaleX = appScale
                            scaleY = appScale
                        }
                        .clip(RoundedCornerShape(curRadius))
                        .alpha(curAlpha)
                        .background(Color(0xFF12141C))
                        .clickable(enabled = false) {}
                ) {
                    AppShell(app = app, viewModel = viewModel, contentPercent = progress)
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

        // 4. Lock Screen Overlay (slides vertically up and fades out cleanly upon unlock)
        if (unlockProgress < 0.99f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = -hDp * unlockProgress)
                    .scale(1.0f - (0.06f * unlockProgress))
                    .alpha(1f - unlockProgress)
            ) {
                VirtualLockScreen(viewModel)
            }
        }
    }
}

// Float helper math for DP lerps
fun lerp(start: Dp, stop: Dp, fraction: Float): Dp = start + (stop - start) * fraction

@Composable
fun VirtualLockScreen(viewModel: OSViewModel) {
    var swipeOffsetY by remember { mutableStateOf(0f) }
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
                        swipeOffsetY = (swipeOffsetY + dragAmount).coerceAtMost(0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val visualOffset = with(density) { swipeOffsetY.toDp() }

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
                        .background(Color.White, CircleShape)
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

    val getDeskIconAlpha = { app: AppId ->
        val progress = animatingAppsProgress[app]
        val openedFromDock = appOpenedFromDockMap[app] ?: false
        if (progress != null && !openedFromDock) {
            if (progress >= 1f) 0f else (1f - (progress / 0.15f)).coerceIn(0f, 1f)
        } else {
            1f
        }
    }

    val getDockIconAlpha = { app: AppId ->
        val progress = animatingAppsProgress[app]
        val openedFromDock = appOpenedFromDockMap[app] ?: false
        if (progress != null && openedFromDock) {
            if (progress >= 1f) 0f else (1f - (progress / 0.15f)).coerceIn(0f, 1f)
        } else {
            1f
        }
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

        // Upper Digital Clock & dynamic Date Desk Widget - Elegant vertical stack based on screenshot
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable { viewModel.openApp(AppId.SETTINGS, false) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "00",
                color = Color.White,
                fontSize = 58.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 52.sp,
                letterSpacing = 1.sp
            )
            Text(
                "26",
                color = Color.White,
                fontSize = 58.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 52.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "6/12 пт",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.alpha(0.65f)
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                     "Преимущественно облачно • 19°C",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Mid apps Grid (2 rows, 4 columns)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 14.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DeskIconItem(AppId.SETTINGS, "Settings", Icons.Default.Settings, Color(0xFF607D8B), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.SETTINGS), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.SETTINGS, false, it) }) { onLaunchApp(AppId.SETTINGS, false) }
                    DeskIconItem(AppId.FILES, "Files", Icons.Default.Folder, Color(0xFFFFB300), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.FILES), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.FILES, false, it) }) { onLaunchApp(AppId.FILES, false) }
                    DeskIconItem(AppId.NOTES, "QuickPad", Icons.Default.Edit, Color(0xFF5E35B1), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.NOTES), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.NOTES, false, it) }) { onLaunchApp(AppId.NOTES, false) }
                    DeskIconItem(AppId.MUSIC, "WavePlayer", Icons.Default.PlayArrow, Color(0xFFE91E63), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.MUSIC), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.MUSIC, false, it) }) { onLaunchApp(AppId.MUSIC, false) }
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DeskIconItem(AppId.CAMERA, "Aperture", Icons.Default.PhotoCamera, Color(0xFF00ACC1), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.CAMERA), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.CAMERA, false, it) }) { onLaunchApp(AppId.CAMERA, false) }
                    DeskIconItem(AppId.GALLERY, "PixelDeck", Icons.Default.PhotoLibrary, Color(0xFF43A047), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.GALLERY), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.GALLERY, false, it) }) { onLaunchApp(AppId.GALLERY, false) }
                    DeskIconItem(AppId.TERMINAL, "FluxShell", Icons.Default.Terminal, Color(0xFF2E7D32), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.TERMINAL), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.TERMINAL, false, it) }) { onLaunchApp(AppId.TERMINAL, false) }
                    DeskIconItem(AppId.BROWSER, "WebSim", Icons.Default.Language, Color(0xFF1E88E5), useNothingIconTheme, showAppLabels, alpha = getDeskIconAlpha(AppId.BROWSER), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.BROWSER, false, it) }) { onLaunchApp(AppId.BROWSER, false) }
                }
            }
        }

        // Bottom Dock Shelf containing phone, messages, browser, camera shortcuts
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 12.dp, end = 12.dp, bottom = 24.dp)
                .fillMaxWidth()
                .height(84.dp)
                .background(Color.White.copy(alpha = 0.09f), RoundedCornerShape(26.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(26.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockIconItem(AppId.PHONE, Icons.Default.Phone, Color(0xFF00E676), useNothingIconTheme, alpha = getDockIconAlpha(AppId.PHONE), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.PHONE, true, it) }) { onLaunchApp(AppId.PHONE, true) }
                DockIconItem(AppId.MESSAGES, Icons.Default.Email, Color(0xFF2979FF), useNothingIconTheme, alpha = getDockIconAlpha(AppId.MESSAGES), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.MESSAGES, true, it) }) { onLaunchApp(AppId.MESSAGES, true) }
                DockIconItem(AppId.BROWSER, Icons.Default.Language, Color(0xFF1E88E5), useNothingIconTheme, alpha = getDockIconAlpha(AppId.BROWSER), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.BROWSER, true, it) }) { onLaunchApp(AppId.BROWSER, true) }
                DockIconItem(AppId.CAMERA, Icons.Default.PhotoCamera, Color(0xFFFF3D00), useNothingIconTheme, alpha = getDockIconAlpha(AppId.CAMERA), modifier = Modifier.onGloballyPositioned { onIconPositioned(AppId.CAMERA, true, it) }) { onLaunchApp(AppId.CAMERA, true) }
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
                        text = "Настройки лаунчера",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Персонализация премиального рабочего стола. Сделайте интерфейс лаконичным и стильным.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    // Toggle App Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAppLabels() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Подписи значков",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Показывать имена приложений под иконками",
                                color = Color.Gray,
                                fontSize = 11.sp
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

@Composable
fun DeskIconItem(
    id: AppId,
    title: String,
    icon: ImageVector,
    themeColor: Color,
    useNothingTheme: Boolean,
    showLabels: Boolean,
    alpha: Float = 1f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val iconShape = RoundedCornerShape(16.dp) // Exquisite organic modern OS squircleProxy

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(if (showLabels) 68.dp else 76.dp)
            .alpha(alpha)
            .clickable(onClick = onClick)
            .testTag("app_${trimmed(id)}")
    ) {
        // High-end dual dynamic background brush based on monochromatic preference toggle
        val backgroundBrush = remember(useNothingTheme, themeColor) {
            if (useNothingTheme) {
                Brush.verticalGradient(listOf(Color(0xFF1C1D24), Color(0xFF101114)))
            } else {
                Brush.verticalGradient(listOf(themeColor, themeColor.copy(alpha = 0.65f)))
            }
        }

        Box(
            modifier = Modifier
                .size(if (showLabels) 54.dp else 66.dp) // Grows significantly larger when labels are hidden!
                .background(backgroundBrush, iconShape)
                .border(
                    width = 0.8.dp,
                    color = if (useNothingTheme) Color.White.copy(alpha = 0.12f) else themeColor.copy(alpha = 0.3f),
                    shape = iconShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(if (showLabels) 24.dp else 32.dp) // Grows much larger to stand out!
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
    alpha: Float = 1f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val iconShape = RoundedCornerShape(16.dp) // squircle alignment

    val backgroundBrush = remember(useNothingTheme, themeColor) {
        if (useNothingTheme) {
            Brush.verticalGradient(listOf(Color(0xFF1C1D24), Color(0xFF101114)))
        } else {
            Brush.verticalGradient(listOf(themeColor, themeColor.copy(alpha = 0.65f)))
        }
    }

    Box(
        modifier = modifier
            .size(54.dp)
            .alpha(alpha)
            .background(backgroundBrush, iconShape)
            .border(
                width = 0.8.dp,
                color = if (useNothingTheme) Color.White.copy(alpha = 0.12f) else themeColor.copy(alpha = 0.3f),
                shape = iconShape
            )
            .clickable(onClick = onClick)
            .testTag("dock_${trimmed(id)}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(26.dp)
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
fun AppShell(app: AppId, viewModel: OSViewModel, contentPercent: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        // App Internal Interface
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp) // Leave safety gap for navigation pill
        ) {
            // App internal status bar spacer
            Spacer(modifier = Modifier.height(28.dp))

            // Dynamic layout switcher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF0C0E14))
            ) {
                if (contentPercent > 0.82f) { // Render app elements only near maximum expansion to optimize rendering
                    when (app) {
                        AppId.SETTINGS -> SettingsAppScreen(viewModel)
                        AppId.NOTES -> NotesAppScreen(viewModel)
                        AppId.MUSIC -> MusicPlayerScreen(viewModel)
                        AppId.CAMERA -> CameraAppScreen(viewModel)
                        AppId.GALLERY -> GalleryAppScreen(viewModel)
                        AppId.TERMINAL -> TerminalAppScreen(viewModel)
                        AppId.FILES -> FileExplorerAppScreen(viewModel)
                        AppId.PHONE -> PhoneAppScreen(viewModel)
                        AppId.MESSAGES -> MessagesAppScreen(viewModel)
                        AppId.BROWSER -> WebSimAppScreen(viewModel)
                    }
                } else {
                    // Minimal loading spinner or placeholder icon while animating
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00FFCC), strokeWidth = 2.dp)
                    }
                }
            }
        }

        // Bottom Safe Navigation Pill (The user specified: Gesture navigation rather than 3 buttons)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(36.dp) // Generous grab area for intuitive touch ergonomics
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
                                if (cumulativeDragY < -100f || progress > 0.20f) { // Swipe up triggers app collapse
                                    viewModel.closeActiveApp()
                                } else {
                                    // Swipe-up canceled, animate active window back to full screen elastically
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
                        // Accessible companion single tap close fallback with gorgeous spring physics
                        viewModel.closeActiveApp()
                    },
                contentAlignment = Alignment.Center
            ) {
                val appSwipeProgress by viewModel.appSwipeProgress.collectAsStateWithLifecycle()
                val isSwipingApp by viewModel.isSwipingApp.collectAsStateWithLifecycle()
                
                // Formula for dynamic gesture pill deformation when active or dragged
                val pillWidth = lerpDp(100.dp, 60.dp, if (isSwipingApp) appSwipeProgress else 0f)
                val pillHeight = lerpDp(4.dp, 6.dp, if (isSwipingApp) appSwipeProgress else 0f)
                val pillColor = if (isSwipingApp) {
                    Color(0xFF00FFCC) // Glows luxurious teal accent when active
                } else {
                    Color.White.copy(alpha = 0.8f)
                }

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

// 2. Notes App Screen
@Composable
fun NotesAppScreen(viewModel: OSViewModel) {
    val notes by viewModel.notesList.collectAsStateWithLifecycle()
    var isAddingNote by remember { mutableStateOf(false) }
    var isViewingNote by remember { mutableStateOf<Note?>(null) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isAddingNote) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                Text("New Quick Note", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Title", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    label = { Text("Write content here...", color = Color.LightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.LightGray),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { isAddingNote = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (titleInput.isNotEmpty() && contentInput.isNotEmpty()) {
                                viewModel.addNote(titleInput, contentInput)
                                titleInput = ""
                                contentInput = ""
                                isAddingNote = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Note")
                    }
                }
            }
        } else if (isViewingNote != null) {
            val note = isViewingNote!!
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                Text(note.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(note.date, color = Color.Gray, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF161824), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Text(note.content, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            viewModel.deleteNote(note.id)
                            isViewingNote = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                    Button(
                        onClick = { isViewingNote = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("QuickPad Notes", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { isAddingNote = true },
                        modifier = Modifier.background(Color(0xFF5E35B1), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                if (notes.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No notes found. Create some!", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notes) { note ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isViewingNote = note },
                                colors = CardDefaults.cardColors(containerColor = Color(note.colorHex)),
                                border = BorderStroke(0.5.dp, Color.White.copy(0.15f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(note.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        note.content,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(note.date, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                                }
                            }
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
    var zoomFactor by remember { mutableStateOf("1x") }
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

        val centerScale = when (zoomFactor) {
            "2x" -> 1.5f
            "5x" -> 2.5f
            else -> 1.0f
        }

        // Viewfinder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Render abstract scenic canvas represent capturing environment
                    val shapeWidth = size.width * 0.7f * centerScale
                    val shapeHeight = size.height * 0.35f * centerScale

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = scanGradient,
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width * 0.8f
                        )
                    )

                    // Draw abstract structures
                    if (selectedFilter == "Matrix") {
                        // lines
                        for (i in 0..10) {
                            drawCircle(Color.Green.copy(0.15f), radius = 30f * i, center = Offset(size.width / 2f, size.height / 2f))
                        }
                    } else {
                        drawCircle(
                            color = Color.White.copy(0.15f),
                            radius = size.width * 0.2f * centerScale,
                            center = Offset(size.width / 2f, size.height * 0.4f)
                        )
                    }
                }
        ) {
            // Viewfinder Grid Lines & HUD targets
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Rule of thirds lines
                drawLine(Color.White.copy(alpha = 0.2f), Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.2f), Offset(size.width * 2f / 3f, 0f), Offset(size.width * 2f / 3f, size.height), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), strokeWidth = 1f)
                drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, size.height * 2f / 3f), Offset(size.width, size.height * 2f / 3f), strokeWidth = 1f)

                // Crosshair at the center
                drawCircle(Color.White.copy(alpha = 0.5f), radius = 10f, center = Offset(size.width / 2f, size.height / 2f))
            }

            // Top Camera Header Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .background(Color.Black.copy(0.4f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("HDR Auto", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("Aperture v1.0", color = Color(0xFF00FFCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("60 FPS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Controls bottom panel overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Zoom toggles
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ZoomIndicator("1x", zoomFactor == "1x") { zoomFactor = "1x" }
                    ZoomIndicator("2x", zoomFactor == "2x") { zoomFactor = "2x" }
                    ZoomIndicator("5x", zoomFactor == "5x") { zoomFactor = "5x" }
                }

                // Filter selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterOptionChip("Standard", selectedFilter == "Normal") { selectedFilter = "Normal" }
                    FilterOptionChip("Cyberpunk", selectedFilter == "Cyberpunk") { selectedFilter = "Cyberpunk" }
                    FilterOptionChip("Monochrome", selectedFilter == "Monochrome") { selectedFilter = "Monochrome" }
                    FilterOptionChip("Matrix CODE", selectedFilter == "Matrix") { selectedFilter = "Matrix" }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Physical trigger button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .border(3.dp, Color.White, CircleShape)
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

// 6. Terminal App Screen
@Composable
fun TerminalAppScreen(viewModel: OSViewModel) {
    val history by viewModel.terminalHistory.collectAsStateWithLifecycle()
    val consoleTheme by viewModel.terminalTheme.collectAsStateWithLifecycle()
    var inputCommand by remember { mutableStateOf("") }

    val themeColor = when (consoleTheme) {
        TerminalTheme.MATRIX_GREEN -> Color(0xFF00FF33)
        TerminalTheme.ORANGE_GLOW -> Color(0xFFFF6600)
        TerminalTheme.SLEEK_CYAN -> Color(0xFF00FFFF)
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FluxShell v2.0", color = themeColor, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .size(width = 46.dp, height = 20.dp)
                    .border(0.5.dp, themeColor, RoundedCornerShape(4.dp))
                    .clickable { viewModel.executeTerminalCommand("theme") },
                contentAlignment = Alignment.Center
            ) {
                Text("COLOR", color = themeColor, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        // Screen area scroll output
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
                .border(0.5.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(history) { log ->
                Text(
                    text = log,
                    color = themeColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick shortcut chips to prevent tricky touch keyword inputs
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TerminalShortcutChip("neofetch") { viewModel.executeTerminalCommand("neofetch") }
            TerminalShortcutChip("matrix") { viewModel.executeTerminalCommand("matrix") }
            TerminalShortcutChip("system") { viewModel.executeTerminalCommand("system") }
            TerminalShortcutChip("help") { viewModel.executeTerminalCommand("help") }
            TerminalShortcutChip("clear") { viewModel.executeTerminalCommand("clear") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Line command text field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$ ", color = themeColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = inputCommand,
                onValueChange = { inputCommand = it },
                textStyle = androidx.compose.ui.text.TextStyle(color = themeColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                modifier = Modifier.weight(1f).height(46.dp),
                placeholder = { Text("Type cmd...", color = themeColor.copy(alpha = 0.4f), fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColor,
                    unfocusedBorderColor = themeColor.copy(0.4f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (inputCommand.isNotEmpty()) {
                        viewModel.executeTerminalCommand(inputCommand)
                        inputCommand = ""
                    }
                },
                modifier = Modifier
                    .background(themeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, themeColor, RoundedCornerShape(8.dp))
                    .size(38.dp)
            ) {
                Icon(imageVector = Icons.Default.ArrowRight, contentDescription = "Send", tint = themeColor)
            }
        }
    }
}

@Composable
fun TerminalShortcutChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(Color.White.copy(0.04f), RoundedCornerShape(4.dp))
            .border(0.5.dp, Color.LightGray.copy(0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(label, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
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

// 8. Interactive Browser WebSim App
@Composable
fun WebSimAppScreen(viewModel: OSViewModel) {
    val browserUrl by viewModel.browserUrl.collectAsStateWithLifecycle()
    var searchInput by remember { mutableStateOf("") }
    var mockCartSize by remember { mutableStateOf(0) }
    var socialLikes by remember { mutableStateOf(105) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Address search bar head
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14161F))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = { viewModel.setBrowserUrl("flux://search") },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }

            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                modifier = Modifier.weight(1f).height(38.dp),
                placeholder = { Text("Search link / keyword...", color = Color.LightGray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FFCC),
                    unfocusedBorderColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp)
            )

            IconButton(
                onClick = {
                    if (searchInput.isNotEmpty()) {
                        viewModel.submitSearch(searchInput)
                    }
                },
                modifier = Modifier
                    .background(Color(0xFF00FFCC).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF00FFCC), modifier = Modifier.size(16.dp))
            }
        }

        // Web simulated view screen
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF07080B))
                .padding(8.dp)
        ) {
            when {
                browserUrl == "flux://search" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("FLUX SEARCH", color = Color(0xFF00FFCC), fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("The virtual portal of Flux networks.", color = Color.LightGray, fontSize = 10.sp)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Hot recommendation buttons
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Recommended interactive sites:", color = Color.Gray, fontSize = 9.sp)
                            WebShortcutRow("NeuraNews Tech Hub") { viewModel.setBrowserUrl("flux://neuranews") }
                            WebShortcutRow("Pineapple Gadget Outlet") { viewModel.setBrowserUrl("flux://pineapple") }
                            WebShortcutRow("GeminiSocial Workspace") { viewModel.setBrowserUrl("flux://social") }
                        }
                    }
                }
                browserUrl == "flux://neuranews" -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("NeuraNews Daily", color = Color(0xFF00FFCC), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Divider(color = Color.Gray.copy(0.3f))
                        }
                        item {
                            NewsArticleCard("ASTEROID CAPTURED", "Tech developers captured gold mine asteroid orbits 1,000 miles from central base.")
                        }
                        item {
                            NewsArticleCard("ROBO ATHLETE CUP WIN", "Flux metallic humanoids win soccer league matches in standard 4-0 score highlights.")
                        }
                    }
                }
                browserUrl == "flux://pineapple" -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pineapple Store", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Row {
                                Icon(Icons.Default.ShoppingCart, null, tint = Color(0xFF00FFCC), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$mockCartSize", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                            border = BorderStroke(0.5.dp, Color.White.copy(0.1f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pineapple Alpha Watch 2", color = Color(0xFF00FFCC), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Quantum display specs with integrated battery tiles which never decay.", color = Color.LightGray, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { mockCartSize += 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Buy Mock Specs ($299)", color = Color.Black, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
                browserUrl == "flux://social" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("GeminiSocial Hub", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
                            border = BorderStroke(0.5.dp, Color.White.copy(0.1f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(24.dp).background(Color(0xFF00FFCC), CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Lead Architect", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("FluxOS operates so fluid, the smooth scale-up zoom loops are incredibly neat!", color = Color.LightGray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { socialLikes += 1 },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$socialLikes Likes", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
                else -> { // Search results
                    val q = browserUrl.substringAfter("q=").replace("+", " ")
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Flux Search Results: '$q'", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161824))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("FluxOS Simulator Review", color = Color(0xFF00FFCC), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { viewModel.setBrowserUrl("flux://social") })
                                Text("Learn how gestures override keys inside these stateful frames...", color = Color.LightGray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebShortcutRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color.White.copy(0.04f), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color.White.copy(0.1f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Icon(imageVector = Icons.Default.Launch, contentDescription = null, tint = Color(0xFF00FFCC), modifier = Modifier.size(12.dp))
    }
}

@Composable
fun NewsArticleCard(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161824)),
        border = BorderStroke(0.5.dp, Color.White.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, color = Color(0xFFFF4081), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, color = Color.White, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

// 9. Phone Screen
@Composable
fun PhoneAppScreen(viewModel: OSViewModel) {
    var dialDigits by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf("dialer") } // "dialer" or "recents"

    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp),
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
