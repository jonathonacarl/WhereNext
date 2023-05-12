package hu.ait.wherenext.ui.screen.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.maps.model.LatLng
import hu.ait.wherenext.data.LocationLiveData
import kotlinx.coroutines.flow.MutableStateFlow

class LocationViewModel(application: Application) : ViewModel() {

    var locationsFlow = MutableStateFlow(mutableListOf<LatLng>())

    private val locationLiveData = LocationLiveData(application)

    fun getLocationLiveData() = locationLiveData

    init {
        locationLiveData.observeForever {
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