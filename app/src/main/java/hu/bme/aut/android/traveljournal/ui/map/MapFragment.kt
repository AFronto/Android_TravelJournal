package hu.bme.aut.android.traveljournal.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import hu.bme.aut.android.traveljournal.R


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var myMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment!!.getMapAsync(this)


        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        myMap.isTrafficEnabled = true
        myMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        val budapest = LatLng(47.0, 19.0)
        myMap.addMarker(MarkerOptions()
            .position(budapest)
            .title("Marker in Hungary"))
        myMap.moveCamera(CameraUpdateFactory.newLatLng(budapest))
    }


}