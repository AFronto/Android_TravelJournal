package hu.bme.aut.android.traveljournal.data

import com.google.firebase.Timestamp

class Post(
    var id: String? = null,
    var parentJournalId: String? = null,
    var title: String? = null,
    var body: String? = null,
    var creationTime: Timestamp? = null,
    var img: String? = null
)