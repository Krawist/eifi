package com.eifi.feature.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eifi.core.data.ReceivingProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/** Entry of the Receive mode: `null` while loading, then whether the receiving profile is filled in. */
@HiltViewModel
internal class ReceiveEntryViewModel @Inject constructor(receivingProfileRepository: ReceivingProfileRepository) :
    ViewModel() {

    val profileConfigured: StateFlow<Boolean?> = flow<Boolean?> {
        emit(receivingProfileRepository.isConfigured.first())
    }
        .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = null)
}
