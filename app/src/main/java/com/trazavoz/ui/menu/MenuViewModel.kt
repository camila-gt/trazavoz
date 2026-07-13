package com.trazavoz.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trazavoz.domain.model.Board
import com.trazavoz.domain.repository.BoardRepository
import com.trazavoz.domain.usecase.VerifyTutorPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    boardRepository: BoardRepository,
    private val verifyTutorPinUseCase: VerifyTutorPinUseCase
) : ViewModel() {

    val boards: StateFlow<List<Board>> = boardRepository.getAllBoards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun verifyPin(enteredPin: String): Boolean {
        return verifyTutorPinUseCase(enteredPin)
    }
}
