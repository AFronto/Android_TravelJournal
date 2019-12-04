package hu.bme.aut.android.traveljournal.data

class Journal(
        var id: String? = null,
        var authorId: String? = null,
        var author: String? = null,
        var title: String? = null,
        var rating: Int? = null,
        var privatized: Boolean = false,
        var onGoing: Boolean = false
)