package hu.bme.aut.android.traveljournal.ui.base

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.adapter.JournalsAdapter
import hu.bme.aut.android.traveljournal.data.Journal
import kotlinx.android.synthetic.main.searchable_journal_list.*

abstract class BaseJournalListFragment : Fragment(), SearchView.OnQueryTextListener,
    JournalsAdapter.JournalClickListener {

    override fun onItemDownVote(journal: Journal) {
        db.collection("journals").document(journal.id!!)
            .update("rating", (journal.rating!! - 1))
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }

    override fun onItemUpVote(journal: Journal) {
        db.collection("journals").document(journal.id!!)
            .update("rating", (journal.rating!! + 1))
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }

    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(s: String?): Boolean {
        journalsAdapter.filter(s) {
            // update UI on nothing found
            Toast.makeText(context, "Nothing Found", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    protected lateinit var registration: ListenerRegistration
    protected lateinit var journalsAdapter: JournalsAdapter
    val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        journalSearchView.setOnQueryTextListener(this)

        if (context != null) {

            journalsAdapter = JournalsAdapter(context!!, mutableListOf())
            journalsAdapter.itemClickListener = this

            rvJournals.layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            rvJournals.adapter = journalsAdapter
        }
        fragmentSpecificInit()
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }

    abstract fun fragmentSpecificInit()
}