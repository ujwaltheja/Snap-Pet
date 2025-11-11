package com.snappet.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.snappet.core.PetController
import com.snappet.core.models.Pet
import com.snappet.core.models.PetState
import com.snappet.media.MediaController
import com.snappet.persistence.PersistenceRepository
import com.snappet.ui.screens.HomeScreen
import com.snappet.ui.theme.SnapPetTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysBasicUI() {
        // Mock dependencies
        val petController = mock<PetController>()
        val mediaController = mock<MediaController>()
        val persistenceRepository = mock<PersistenceRepository>()

        val petState = PetState(
            petId = "cat",
            hunger = 80f,
            happiness = 70f,
            energy = 90f
        )
        val pet = Pet.DEFAULT_PETS.first()

        whenever(petController.currentPetState).thenReturn(MutableStateFlow(petState))
        whenever(petController.currentPet).thenReturn(MutableStateFlow(pet))
        whenever(persistenceRepository.observeUser()).thenReturn(MutableStateFlow(null))

        composeTestRule.setContent {
            SnapPetTheme {
                val navController = rememberNavController()
                HomeScreen(
                    navController = navController,
                    petController = petController,
                    mediaController = mediaController,
                    persistenceRepository = persistenceRepository
                )
            }
        }

        // Verify key UI elements are present
        composeTestRule.onNodeWithText("Snap Pet").assertExists()
        composeTestRule.onNodeWithText("Whiskers").assertExists()
        composeTestRule.onNodeWithText("Cat").assertExists()

        // Verify needs meters
        composeTestRule.onNodeWithText("Hunger").assertExists()
        composeTestRule.onNodeWithText("Happiness").assertExists()
        composeTestRule.onNodeWithText("Energy").assertExists()

        // Verify action buttons
        composeTestRule.onNodeWithText("Pet").assertExists()
        composeTestRule.onNodeWithText("Feed").assertExists()
        composeTestRule.onNodeWithText("Play").assertExists()
        composeTestRule.onNodeWithText("Record").assertExists()
    }
}
