package hu.bme.aut.android.traveljournal.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

class Post(
    var id: String? = null,
    var parentJournalId: String? = null,
    var title: String? = null,
    var body: String? = null,
    var creationTime: Timestamp? = null,
    var img: String? = null,
    var location: GeoPoint? = null
)