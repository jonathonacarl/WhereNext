package hu.ait.wherenext.ui.screen.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.maps.model.LatLng
import hu.ait.wherenext.data.LocationLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationViewModel(application: Application) : ViewModel() {

    private val _locationsFlow = MutableStateFlow(mutableListOf<LatLng>())

    val locationsFlow = _locationsFlow.asStateFlow()

    private val locationLiveData = LocationLiveData(application)

    var firstPositionArrived by mutableStateOf(false)
    fun getLocationLiveData() = locationLiveData

    init {
        locationLiveData.observeForever {
            firstPositionArrived = true
            locationsFlow.value.add(LatLng(it.latitude,it.longitude))
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LocationViewModel(
                    application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])!!)
            }
        }
    }
}