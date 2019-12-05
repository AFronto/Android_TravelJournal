package hu.bme.aut.android.traveljournal.location

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class LocationService : Service() {

    companion object {
        const val BR_NEW_LOCATION = "BR_NEW_LOCATION"
        const val KEY_LOCATION = "KEY_LOCATION"
    }


    private var locationHelper: LocationHelper? = null


    var lastLocation: Location? = null
        private set

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("LOC","Service started!")
        if (locationHelper == null) {
            val locationRequest = intent.extras!!["locationRequest"] as LocationRequest

            val helper = LocationHelper(applicationContext, LocationServiceCallback(),locationRequest)
            helper.startLocationMonitoring()
            locationHelper = helper
        }

        return START_STICKY
    }

    inner class LocationServiceCallback : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return

            lastLocation = location

            val intent = Intent()
            intent.action = BR_NEW_LOCATION
            intent.putExtra(KEY_LOCATION, location)
            LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(intent)
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            // TODO
        }
    }


    override fun onDestroy() {
        Log.d("LOC","Service stopped!")
        locationHelper?.stopLocationMonitoring()
        super.onDestroy()
    }

}