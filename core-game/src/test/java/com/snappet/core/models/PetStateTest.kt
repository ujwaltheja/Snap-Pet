package com.snappet.core.models

import org.junit.Assert.*
import org.junit.Test

class PetStateTest {

    @Test
    fun `feed interaction increases hunger and happiness`() {
        val state = PetState(petId = "test", hunger = 50f, happiness = 50f, energy = 50f)
        val newState = state.feed()

        assertEquals(80f, newState.hunger, 0.1f) // 50 + 30
        assertEquals(55f, newState.happiness, 0.1f) // 50 + 5
        assertEquals(1, newState.totalInteractions)
    }

    @Test
    fun `play interaction increases happiness and decreases energy`() {
        val state = PetState(petId = "test", hunger = 50f, happiness = 50f, energy = 50f)
        val newState = state.play()

        assertEquals(75f, newState.happiness, 0.1f) // 50 + 25
        assertEquals(35f, newState.energy, 0.1f) // 50 - 15
        assertEquals(1, newState.totalInteractions)
    }

    @Test
    fun `pet interaction increases happiness`() {
        val state = PetState(petId = "test", hunger = 50f, happiness = 50f, energy = 50f)
        val newState = state.pet()

        assertEquals(65f, newState.happiness, 0.1f) // 50 + 15
        assertEquals(1, newState.totalInteractions)
    }

    @Test
    fun `poke interaction decreases happiness`() {
        val state = PetState(petId = "test", hunger = 50f, happiness = 50f, energy = 50f)
        val newState = state.poke()

        assertEquals(45f, newState.happiness, 0.1f) // 50 - 5
        assertEquals(1, newState.totalInteractions)
    }

    @Test
    fun `values are clamped to 0-100 range`() {
        val state = PetState(petId = "test", hunger = 95f, happiness = 5f, energy = 10f)

        // Test upper bound
        val fedState = state.feed()
        assertEquals(100f, fedState.hunger, 0.1f) // Clamped at 100

        // Test lower bound
        val pokedState = state.poke()
        assertEquals(0f, pokedState.happiness, 0.1f) // Clamped at 0
    }

    @Test
    fun `elapsed time decreases all needs`() {
        val state = PetState(petId = "test", hunger = 100f, happiness = 100f, energy = 100f)
        val elapsedSeconds = 100f

        val newState = state.updateWithElapsedTime(elapsedSeconds)

        assertTrue(newState.hunger < 100f)
        assertTrue(newState.happiness < 100f)
        assertTrue(newState.energy < 100f)

        // Verify decay rates
        val expectedHunger = 100f - (PetState.HUNGER_DECAY_PER_SECOND * elapsedSeconds)
        val expectedHappiness = 100f - (PetState.HAPPINESS_DECAY_PER_SECOND * elapsedSeconds)
        val expectedEnergy = 100f - (PetState.ENERGY_DECAY_PER_SECOND * elapsedSeconds)

        assertEquals(expectedHunger, newState.hunger, 0.1f)
        assertEquals(expectedHappiness, newState.happiness, 0.1f)
        assertEquals(expectedEnergy, newState.energy, 0.1f)
    }

    @Test
    fun `equip cosmetics updates state`() {
        val state = PetState(petId = "test")
        val newState = state.equipCosmetics(hat = "party_hat", skin = "golden")

        assertEquals("party_hat", newState.equippedHat)
        assertEquals("golden", newState.equippedSkin)
    }

    @Test
    fun `rest interaction increases energy`() {
        val state = PetState(petId = "test", hunger = 50f, happiness = 50f, energy = 30f)
        val newState = state.rest()

        assertEquals(70f, newState.energy, 0.1f) // 30 + 40
    }
}
