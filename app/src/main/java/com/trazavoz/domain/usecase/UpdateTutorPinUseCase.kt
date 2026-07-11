package com.trazavoz.domain.usecase

import com.trazavoz.data.local.prefs.TutorPreferences
import javax.inject.Inject

class UpdateTutorPinUseCase @Inject constructor(
    private val tutorPreferences: TutorPreferences
) {
    suspend operator fun invoke(newPin: String) {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            tutorPreferences.savePin(newPin)
        }
    }
}
