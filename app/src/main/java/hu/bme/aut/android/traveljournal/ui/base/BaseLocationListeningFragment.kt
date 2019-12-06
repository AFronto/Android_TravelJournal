package hu.bme.aut.android.traveljournal.ui.base

import android.Manifest
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.GeoPoint
import hu.bme.aut.android.traveljournal.location.LocationService

abstract class BaseLocationListeningFragment : Fragment() {

    protected lateinit var locationRequest: LocationRequest
    protected var lastKnownLocation: GeoPoint? = null

    companion object {

        fun startOrStopServiceAsNecessary(
            context: Context,
            startService: Boolean,
            locationRequest: LocationRequest?
        ) {
            val intent = Intent(context, LocationService::class.java)

            if (startService && locationRequest != null) {
                intent.putExtra("locationRequest", locationRequest)
                context.startService(intent)
            } else {
                context.stopService(intent)
            }
        }
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val currentLocation = intent.getParcelableExtra<Location>(LocationService.KEY_LOCATION)

            if(lastKnownLocation == null){
                lastKnownLocation = GeoPoint(currentLocation.latitude,currentLocation.longitude)
                firstLocationRecived()
            }else{
                lastKnownLocation = GeoPoint(currentLocation.latitude,currentLocation.longitude)
            }

            Log.d("LOC", "Location caught ${currentLocation.latitude} ${currentLocation.longitude}")
        }
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(locationReceiver, IntentFilter(LocationService.BR_NEW_LOCATION))
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(locationReceiver)
        startOrStopServiceAsNecessary(context!!,false,null)
        super.onStop()
    }

    protected fun hasPermission(): Boolean {       //checks pesmission for ACCESS_FINE_LOCATION
        return (ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
                == PackageManager.PERMISSION_GRANTED
                )
    }

    protected fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(context!!)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) try {
                // Show the dialog by calling startResolutionForResult(),
                // and check the result in onActivityResult().
                exception.startResolutionForResult(
                    activity,
                    1
                )
            } catch (sendEx: IntentSender.SendIntentException) {
                // Ignore the error.
            }
            // Location settings are not satisfied, but this can be fixed
            // by showing the user a dialog.
        }
    }

    abstract fun firstLocationRecived()
}
