package com.trazavoz.domain.usecase

import com.trazavoz.data.local.prefs.TutorPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class VerifyTutorPinUseCase @Inject constructor(
    private val tutorPreferences: TutorPreferences
) {
    suspend operator fun invoke(enteredPin: String): Boolean {
        val currentPin = tutorPreferences.tutorPinFlow.first()
        return currentPin == enteredPin
    }
}
