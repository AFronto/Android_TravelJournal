package hu.bme.aut.android.traveljournal.ui.map

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.data.Journal
import hu.bme.aut.android.traveljournal.data.Post
import hu.bme.aut.android.traveljournal.data.PostWithMarker
import hu.bme.aut.android.traveljournal.ui.base.BaseLocationListeningFragment
import hu.bme.aut.android.traveljournal.ui.journal_read.ReadJornalFragment
import kotlin.random.Random


class MapFragment : BaseLocationListeningFragment(), OnMapReadyCallback,
    GoogleMap.OnPolylineClickListener, GoogleMap.OnInfoWindowClickListener {
    override fun onInfoWindowClick(p0: Marker?) {
        var journalId =
            markers.filterValues { markersAndPosts -> markersAndPosts.any { markersAndPost -> markersAndPost.marker == p0 } }
                .entries.first().key
        tryToReadJournal(journalId)
    }

    override fun onPolylineClick(p0: Polyline?) {
        var journalId = routes.filterValues { polyline -> p0 == polyline }.entries.first().key
        tryToReadJournal(journalId)
    }

    private fun tryToReadJournal(journalId: String) {
        db.collection("journals")
            .document(journalId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    var journal = document.toObject(
                        Journal::class.java
                    )
                    if (journal!!.privatized) {
                        Toast.makeText(
                            context,
                            "This journal is private you cannot read it!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        var bundle =
                            bundleOf(
                                "id" to journalId,
                                "title" to journal.title,
                                "authorId" to journal.authorId,
                                "author" to journal.author,
                                "rating" to journal.rating,
                                "privatized" to journal.privatized,
                                "onGoing" to journal.onGoing
                            )

                        clearRoutes()
                        findNavController().navigate(R.id.nav_read_journal, bundle)
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private lateinit var myMap: GoogleMap
    private lateinit var registration: ListenerRegistration
    val db = FirebaseFirestore.getInstance()
    var markers = mutableMapOf<String, MutableList<PostWithMarker>>()
    var routes = mutableMapOf<String, Polyline>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createLocationRequest()
    }

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
        myMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        myMap.clear()
        initPostsListener()
        myMap.setOnInfoWindowClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        ReadJornalFragment.journalId = null
        startOrStopServiceAsNecessary(context!!, true, locationRequest)
    }

    override fun onStop() {
        registration.remove()
        super.onStop()
    }

    override fun firstLocationRecived() {
        myMap.moveCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    lastKnownLocation!!.latitude,
                    lastKnownLocation!!.longitude
                )
            )
        )
    }

    private fun initPostsListener() {
        registration = db.collection("posts")
            .orderBy("creationTime")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                for (doc in value!!.documentChanges) {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            var newPost = doc.document.toObject(
                                Post::class.java
                            )
                            newPost.id = doc.document.id
                            if (markers[newPost.parentJournalId!!] == null) {
                                markers[newPost.parentJournalId!!] = mutableListOf()
                            }
                            createMarker(newPost)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            var modPost = doc.document.toObject(
                                Post::class.java
                            )
                            modPost.id = doc.document.id
                            updateMarker(modPost)
                        }
                        DocumentChange.Type.REMOVED -> {
                            var delPost = doc.document.toObject(
                                Post::class.java
                            )
                            delPost.id = doc.document.id
                            deleteMarker(delPost)
                        }
                    }
                }

            }
    }

    private fun createMarker(newPost: Post) {
        markers[newPost.parentJournalId!!]!!.add(
            PostWithMarker(
                newPost, myMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(newPost.location!!.latitude, newPost.location!!.longitude))
                        .title(newPost.title)
                )
            )
        )

        if (routes[newPost.parentJournalId!!] == null) {
            routes[newPost.parentJournalId!!] = myMap.addPolyline(
                PolylineOptions().color(Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))).width(25f).clickable(
                    true
                )
            )
            myMap.setOnPolylineClickListener(this)
        }
        routes[newPost.parentJournalId!!]!!.points =
            markers[newPost.parentJournalId!!]!!.map { mAndP -> mAndP.marker!!.position }
    }

    private fun updateMarker(modPost: Post) {
        val index =
            markers[modPost.parentJournalId!!]!!.indexOfFirst { old -> old.post!!.id == modPost.id }
        markers[modPost.parentJournalId!!]!![index].post = modPost
        markers[modPost.parentJournalId!!]!![index].marker!!.remove()
        markers[modPost.parentJournalId!!]!![index].marker = myMap.addMarker(
            MarkerOptions()
                .position(LatLng(modPost.location!!.latitude, modPost.location!!.longitude))
                .title(modPost.title)
        )

        routes[modPost.parentJournalId!!]!!.points =
            markers[modPost.parentJournalId!!]!!.map { mAndP -> mAndP.marker!!.position }
    }

    private fun deleteMarker(delPost: Post) {
        val index =
            markers[delPost.parentJournalId!!]!!.indexOfFirst { old -> old.post!!.id == delPost.id }
        markers[delPost.parentJournalId!!]!![index].marker!!.remove()
        markers[delPost.parentJournalId!!]!!.removeAt(index)

        routes[delPost.parentJournalId!!]!!.points =
            markers[delPost.parentJournalId!!]!!.map { mAndP -> mAndP.marker!!.position }
    }

    private fun clearRoutes() {
        routes.keys.forEach { key ->
            routes[key]!!.points = listOf()
            routes[key]!!.remove()
        }
        routes.clear()

        markers.forEach { markersAndPosts ->
            markersAndPosts.value.clear()
        }
        markers.clear()
    }
}