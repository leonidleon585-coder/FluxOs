package com.example

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppId {
    SETTINGS,
    FILES,
    NOTES,
    MUSIC,
    CAMERA,
    GALLERY,
    TERMINAL,
    BROWSER,
    PHONE,
    MESSAGES
}

enum class WallpaperType {
    AURORA,
    SPACE,
    EMERALD,
    NEON,
    LOCAL
}

enum class TerminalTheme {
    MATRIX_GREEN,
    ORANGE_GLOW,
    SLEEK_CYAN
}

data class Note(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val date: String,
    val colorHex: Long
)

data class MockFile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "txt", "mp3", "json", "html", "folder"
    val content: String = "",
    val path: String
)

data class GalleryPhoto(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val isCaptured: Boolean = false,
    val filterType: String = "Normal", // "Normal", "Cyberpunk", "Monochrome", "Matrix"
    val wallGradient: List<Long> // Custom aesthetic gradients
)

class OSViewModel : ViewModel() {
    // Basic power & lock states
    private val _isPowerOn = MutableStateFlow(true)
    val isPowerOn: StateFlow<Boolean> = _isPowerOn.asStateFlow()

    private val _isScreenLocked = MutableStateFlow(false)
    val isScreenLocked: StateFlow<Boolean> = _isScreenLocked.asStateFlow()

    // Navigation and gestures
    private val _activeApp = MutableStateFlow<AppId?>(null)
    val activeApp: StateFlow<AppId?> = _activeApp.asStateFlow()

    private val _appSwipeProgress = MutableStateFlow(0f)
    val appSwipeProgress: StateFlow<Float> = _appSwipeProgress.asStateFlow()

    private val _appSwipeDragY = MutableStateFlow(0f)
    val appSwipeDragY: StateFlow<Float> = _appSwipeDragY.asStateFlow()

    private val _isSwipingApp = MutableStateFlow(false)
    val isSwipingApp: StateFlow<Boolean> = _isSwipingApp.asStateFlow()

    private val _appOpenedFromDockMap = MutableStateFlow<Map<AppId, Boolean>>(emptyMap())
    val appOpenedFromDockMap: StateFlow<Map<AppId, Boolean>> = _appOpenedFromDockMap.asStateFlow()

    // Dynamic control widgets
    private val _isQuickSettingsOpen = MutableStateFlow(false)
    val isQuickSettingsOpen: StateFlow<Boolean> = _isQuickSettingsOpen.asStateFlow()

    private val _isWifiOn = MutableStateFlow(true)
    val isWifiOn: StateFlow<Boolean> = _isWifiOn.asStateFlow()

    private val _isBluetoothOn = MutableStateFlow(true)
    val isBluetoothOn: StateFlow<Boolean> = _isBluetoothOn.asStateFlow()

    private val _isAirplaneModeOn = MutableStateFlow(false)
    val isAirplaneModeOn: StateFlow<Boolean> = _isAirplaneModeOn.asStateFlow()

    private val _isDNDOn = MutableStateFlow(false)
    val isDNDOn: StateFlow<Boolean> = _isDNDOn.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _batteryPercentage = MutableStateFlow(84)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _systemBrightness = MutableStateFlow(0.85f) // overlay dimming factor
    val systemBrightness: StateFlow<Float> = _systemBrightness.asStateFlow()

    private val _systemVolume = MutableStateFlow(0.6f)
    val systemVolume: StateFlow<Float> = _systemVolume.asStateFlow()

    // Wallpaper configuration
    private val _wallpaper = MutableStateFlow(WallpaperType.AURORA)
    val wallpaper: StateFlow<WallpaperType> = _wallpaper.asStateFlow()

    private val _customLocalWallpaper = MutableStateFlow<List<Long>?>(null)
    val customLocalWallpaper: StateFlow<List<Long>?> = _customLocalWallpaper.asStateFlow()

    // Animation speeds: 1200ms (Slow-mo), 350ms (Normal), 150ms (Fast)
    private val _animationSpeedMs = MutableStateFlow(350)
    val animationSpeedMs: StateFlow<Int> = _animationSpeedMs.asStateFlow()

    // Launcher Settings & Nothing OS Icon Style
    private val _showAppLabels = MutableStateFlow(true)
    val showAppLabels: StateFlow<Boolean> = _showAppLabels.asStateFlow()

    private val _useNothingIconTheme = MutableStateFlow(true)
    val useNothingIconTheme: StateFlow<Boolean> = _useNothingIconTheme.asStateFlow()

    private val _isLauncherSettingsOpen = MutableStateFlow(false)
    val isLauncherSettingsOpen: StateFlow<Boolean> = _isLauncherSettingsOpen.asStateFlow()

    // Virtual DB / Lists
    private val _notesList = MutableStateFlow<List<Note>>(emptyList())
    val notesList: StateFlow<List<Note>> = _notesList.asStateFlow()

    private val _galleryPhotos = MutableStateFlow<List<GalleryPhoto>>(emptyList())
    val galleryPhotos: StateFlow<List<GalleryPhoto>> = _galleryPhotos.asStateFlow()

    private val _terminalHistory = MutableStateFlow<List<String>>(emptyList())
    val terminalHistory: StateFlow<List<String>> = _terminalHistory.asStateFlow()

    private val _terminalTheme = MutableStateFlow(TerminalTheme.MATRIX_GREEN)
    val terminalTheme: StateFlow<TerminalTheme> = _terminalTheme.asStateFlow()

    // Browser state
    private val _browserUrl = MutableStateFlow("flux://search")
    val browserUrl: StateFlow<String> = _browserUrl.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // File Manager active folder state
    private val _currentPath = MutableStateFlow("Root")
    val currentPath: StateFlow<String> = _currentPath.asStateFlow()

    // Music Player state
    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    val trackTitles = listOf("Neon Horizon", "Synthwave Dream", "Digital Rainfall", "Cosmic Voyager")
    val trackArtists = listOf("Vector Prime", "Lumina OS", "Quartz Flow", "Nebula Cascade")

    init {
        // Initialize default notes
        _notesList.value = listOf(
            Note(
                title = "FluxOS Vision",
                content = "Welcome to FluxOS! This operating system simulation features ultra-fast animations, immersive physics-based feedback, and clean Material 3 cards. Gestures replace standard button grids in the name of future fluid motion.",
                date = "June 11, 2026",
                colorHex = 0xFF2D3250
            ),
            Note(
                title = "Secrets command list",
                content = "Access FluxShell terminal and try executing:\n- neofetch\n- matrix\n- clear\n- theme",
                date = "June 11, 2026",
                colorHex = 0xFF5B4B8A
            )
        )

        // Initialize default gallery images
        _galleryPhotos.value = listOf(
            GalleryPhoto(
                title = "Deep Nebula",
                date = "05/18/2026",
                wallGradient = listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364)
            ),
            GalleryPhoto(
                title = "Cyber Dusk",
                date = "06/02/2026",
                wallGradient = listOf(0xFFFC466B, 0xFF3F5EFB)
            ),
            GalleryPhoto(
                title = "Emerald Void",
                date = "06/10/2026",
                wallGradient = listOf(0xFF051937, 0xFF004D7A, 0xFF008793, 0xFF00BF72)
            )
        )

        // Terminal initial messages
        _terminalHistory.value = listOf(
            "FluxOS Core [Version 5.2.2026]",
            "All physical virtual controllers green.",
            "Type 'help' for command directory."
        )
    }

    // ACTIONS
    fun togglePower() {
        if (_isPowerOn.value) {
            _isPowerOn.value = false
            _isScreenLocked.value = true
        } else {
            _isPowerOn.value = true
            _isScreenLocked.value = true
        }
    }

    fun unlockScreen() {
        _isScreenLocked.value = false
    }

    fun lockScreen() {
        _isScreenLocked.value = true
    }

    fun openApp(app: AppId, fromDock: Boolean = false) {
        if (!_isScreenLocked.value && _isPowerOn.value) {
            _appOpenedFromDockMap.value = _appOpenedFromDockMap.value + (app to fromDock)
            _activeApp.value = app
            _isQuickSettingsOpen.value = false
        }
    }

    fun closeActiveApp() {
        _activeApp.value = null
    }

    fun updateAppSwipeProgress(progress: Float, dragY: Float = 0f) {
        _appSwipeProgress.value = progress.coerceIn(0f, 1f)
        _appSwipeDragY.value = dragY
    }

    fun setSwipingApp(swiping: Boolean) {
        _isSwipingApp.value = swiping
    }

    fun toggleQuickSettings() {
        if (_isPowerOn.value && !_isScreenLocked.value) {
            _isQuickSettingsOpen.value = !_isQuickSettingsOpen.value
        }
    }

    fun setQuickSettingsOpen(open: Boolean) {
        if (_isPowerOn.value && !_isScreenLocked.value) {
            _isQuickSettingsOpen.value = open
        }
    }

    // Launcher Settings actions
    fun toggleAppLabels() {
        _showAppLabels.value = !_showAppLabels.value
    }

    fun setAppLabels(visible: Boolean) {
        _showAppLabels.value = visible
    }

    fun toggleNothingIconTheme() {
        _useNothingIconTheme.value = !_useNothingIconTheme.value
    }

    fun setNothingIconTheme(enabled: Boolean) {
        _useNothingIconTheme.value = enabled
    }

    fun setLauncherSettingsOpen(open: Boolean) {
        _isLauncherSettingsOpen.value = open
    }

    fun toggleWifi() { _isWifiOn.value = !_isWifiOn.value }
    fun toggleBluetooth() { _isBluetoothOn.value = !_isBluetoothOn.value }
    fun toggleAirplaneMode() {
        _isAirplaneModeOn.value = !_isAirplaneModeOn.value
        if (_isAirplaneModeOn.value) {
            _isWifiOn.value = false
            _isBluetoothOn.value = false
        }
    }
    fun toggleDND() { _isDNDOn.value = !_isDNDOn.value }
    fun toggleFlashlight() { _isFlashlightOn.value = !_isFlashlightOn.value }

    fun updateBrightness(v: Float) {
        _systemBrightness.value = v.coerceIn(0.15f, 1.0f)
    }

    fun updateVolume(v: Float) {
        _systemVolume.value = v.coerceIn(0.0f, 1.0f)
    }

    fun setWallpaper(wp: WallpaperType) {
        _wallpaper.value = wp
    }

    fun setCustomLocalWallpaper(gradient: List<Long>) {
        _customLocalWallpaper.value = gradient
        _wallpaper.value = WallpaperType.LOCAL
    }

    fun setAnimationSpeed(ms: Int) {
        _animationSpeedMs.value = ms
    }

    // Notes CRUD
    fun addNote(title: String, content: String) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val colors = listOf(0xFF2D3250, 0xFF5B4B8A, 0xFF424B54, 0xFF5A3E36, 0xFF1B4965)
        val selectedColor = colors.random()
        val newNote = Note(
            title = title,
            content = content,
            date = currentDate,
            colorHex = selectedColor
        )
        _notesList.value = _notesList.value + newNote
    }

    fun deleteNote(id: String) {
        _notesList.value = _notesList.value.filter { it.id != id }
    }

    // Terminal Commands
    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim().lowercase()
        if (trimmed.isEmpty()) return

        val responseList = mutableListOf<String>()
        responseList.add("flux@system:~$ $cmd")

        when (trimmed) {
            "help" -> {
                responseList.add("Available commands:")
                responseList.add("  neofetch - Hardware specs & ascii flag")
                responseList.add("  matrix   - Matrix falling-code visualizer stream")
                responseList.add("  clear    - Clear logs history")
                responseList.add("  theme    - Cycles shell cyan / neon / matrix orange color")
                responseList.add("  system   - Virtual file storage disk status")
            }
            "neofetch" -> {
                responseList.add("       .:^~^:.       OS: FluxOS v5.2 (Flux-X64)")
                responseList.add("     .:~7????7~:.    Kernel: Virtual ARM Cortex-G9")
                responseList.add("    :?7::::::::7?:   Uptime: 2 hours, 14 mins")
                responseList.add("   .77::^^^^^^::77.  Shell: FluxShell 1.0")
                responseList.add("   :?7::7????7::7?:  Theme: Premium Nebula Midnight")
                responseList.add("    :?7::::::::7?:   Processor: Simulated Core-8 Quantum")
                responseList.add("     .:~7????7~:.    Memory: 16 GB Physical / 512 GB Virtual")
                responseList.add("       .:^~^:.       Graphics: NeonFluid Hardware Engine")
            }
            "matrix" -> {
                responseList.add("[Initializing Core Flow Interface...]")
                responseList.add("01010100 01100101 01111000 01110100")
                responseList.add("11010111 10101100 11001110 11000010")
                responseList.add("10011010 SYSTEM OVERRIDE PRESET ACTIVATED")
                responseList.add("01001111 01010010 01001001 01001111 01001110")
            }
            "clear" -> {
                _terminalHistory.value = emptyList()
                return
            }
            "theme" -> {
                when (_terminalTheme.value) {
                    TerminalTheme.MATRIX_GREEN -> {
                        _terminalTheme.value = TerminalTheme.ORANGE_GLOW
                        responseList.add("Console theme shifted: ORANGE GLOW")
                    }
                    TerminalTheme.ORANGE_GLOW -> {
                        _terminalTheme.value = TerminalTheme.SLEEK_CYAN
                        responseList.add("Console theme shifted: SLEEK CYAN")
                    }
                    TerminalTheme.SLEEK_CYAN -> {
                        _terminalTheme.value = TerminalTheme.MATRIX_GREEN
                        responseList.add("Console theme shifted: MATRIX CODE GREEN")
                    }
                }
            }
            "system" -> {
                responseList.add("DRIVE /dev/sda1 SYSTEM MOUNTED:")
                responseList.add("  Size: 512 GB Virtual SSD")
                responseList.add("  Used: 31 GB (Sparsely Loaded Virtualized Containers)")
                responseList.add("  Available: 481 GB (Clean sandbox memory)")
                responseList.add("  Health Status: 100% (Solid Solid State)")
            }
            else -> {
                responseList.add("Command not recognized: '$cmd'. Type 'help' for support.")
            }
        }

        _terminalHistory.value = _terminalHistory.value + responseList
    }

    // Browser navigations
    fun setBrowserUrl(url: String) {
        _browserUrl.value = url
    }

    fun submitSearch(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty()) {
            _browserUrl.value = "flux://results?q=${query.replace(" ", "+")}"
        }
    }

    // File Manager movements
    fun setFilePath(path: String) {
        _currentPath.value = path
    }

    // Camera captures
    fun takePhoto(filter: String) {
        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
        val curDate = sdf.format(Date())
        val sizeVal = _galleryPhotos.value.size
        val newPhoto = GalleryPhoto(
            title = "Capture #$sizeVal",
            date = curDate,
            isCaptured = true,
            filterType = filter,
            wallGradient = when (filter) {
                "Cyberpunk" -> listOf(0xFFFF007F, 0xFF00F0FF)
                "Monochrome" -> listOf(0xFF222222, 0xFFBBBBBB)
                "Matrix" -> listOf(0xFF001100, 0xFF00FF00)
                else -> listOf(0xFF4A00E0, 0xFF8E2DE2)
            }
        )
        _galleryPhotos.value = _galleryPhotos.value + newPhoto
        _batteryPercentage.value = (_batteryPercentage.value - 1).coerceAtLeast(1)
    }

    // Music playback
    fun togglePlayPause() {
        _isMusicPlaying.value = !_isMusicPlaying.value
    }

    fun nextTrack() {
        _currentTrackIndex.value = (_currentTrackIndex.value + 1) % trackTitles.size
    }

    fun prevTrack() {
        _currentTrackIndex.value = if (_currentTrackIndex.value - 1 < 0) {
            trackTitles.size - 1
        } else {
            _currentTrackIndex.value - 1
        }
    }
}
