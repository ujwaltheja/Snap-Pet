package com.snappet.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.snappet.core.PetController
import com.snappet.media.MediaController
import com.snappet.persistence.PersistenceRepository
import com.snappet.store.StoreRepository
import com.snappet.ui.navigation.SnapPetNavigation
import com.snappet.ui.theme.SnapPetTheme
import com.snappet.utils.Logger
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var logger: Logger
    private lateinit var persistenceRepository: PersistenceRepository
    private lateinit var petController: PetController
    private lateinit var storeRepository: StoreRepository
    private lateinit var mediaController: MediaController

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            logger.warn("MainActivity", "Audio recording permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize logger first for crash reporting
        val logDir = getExternalFilesDir(null)
        logger = Logger.getInstance(logDir)

        // Set up crash reporting
        setupCrashReporting()

        // Enable immersive mode
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        )

        // Initialize components
        persistenceRepository = PersistenceRepository(applicationContext)
        petController = PetController(persistenceRepository, lifecycleScope, logger)
        storeRepository = StoreRepository(persistenceRepository, logger)
        mediaController = MediaController(applicationContext, logger)

        // Initialize database with defaults
        lifecycleScope.launch {
            persistenceRepository.initializeDefaults()

            // Load default pet
            val user = persistenceRepository.getUser()
            if (user != null) {
                petController.loadPet(user.selectedPetId)
            }
        }

        // Request audio permission
        checkAudioPermission()

        setContent {
            SnapPetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SnapPetNavigation(
                        petController = petController,
                        storeRepository = storeRepository,
                        mediaController = mediaController,
                        persistenceRepository = persistenceRepository,
                        logger = logger
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        petController.start()
    }

    override fun onPause() {
        super.onPause()
        petController.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController.cleanup()
    }

    private fun setupCrashReporting() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Log crash to local file
            logger.error(
                "CrashHandler",
                "Uncaught exception on thread ${thread.name}: ${throwable.message}",
                throwable
            )

            // Log full stack trace
            logger.error(
                "CrashHandler",
                "Stack trace:\n${throwable.stackTraceToString()}",
                throwable
            )

            // Call the default handler to terminate the app
            defaultHandler?.uncaughtException(thread, throwable)
        }

        logger.info("MainActivity", "Crash reporting initialized")
    }

    private fun checkAudioPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
            }
            else -> {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}
