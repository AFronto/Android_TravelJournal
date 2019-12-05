package hu.bme.aut.android.traveljournal.location

import android.content.Context
import android.content.IntentSender
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class LocationHelper(private val context: Context, private val callback: LocationCallback, private val locationRequest: LocationRequest) {



    fun startLocationMonitoring() {
        LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
    }

    fun stopLocationMonitoring() {
        LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(callback)
    }

}