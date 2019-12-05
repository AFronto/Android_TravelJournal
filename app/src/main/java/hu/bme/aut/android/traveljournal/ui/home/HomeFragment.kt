package hu.bme.aut.android.traveljournal.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.constraintlayout.widget.Constraints.TAG
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.DocumentChange
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.data.Journal
import hu.bme.aut.android.traveljournal.ui.base.BaseJournalListFragment
import hu.bme.aut.android.traveljournal.ui.journal_read.ReadJornalFragment

class HomeFragment : BaseJournalListFragment() {
    override fun onItemLongClick(journal: Journal, view: View): Boolean {
        return false
    }

    override fun onItemClick(journal: Journal) {
        var bundle =
            bundleOf(
                "id" to journal.id,
                "title" to journal.title,
                "authorId" to journal.authorId,
                "author" to journal.author,
                "rating" to journal.rating
            )
        findNavController().navigate(R.id.nav_read_journal, bundle)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun fragmentSpecificInit() {
        ReadJornalFragment.journalId = null

        registration = db.collection("journals")
            .whereEqualTo("privatized", false)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }


                for (doc in value!!.documentChanges) {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> {
                            var newJournal = doc.document.toObject(
                                Journal::class.java
                            )
                            newJournal.id = doc.document.id
                            journalsAdapter.addJournal(
                                newJournal
                            )
                        }
                        DocumentChange.Type.MODIFIED -> {
                            var modJournal = doc.document.toObject(
                                Journal::class.java
                            )
                            modJournal.id = doc.document.id
                            journalsAdapter.updateJournal(
                                modJournal
                            )
                        }
                        DocumentChange.Type.REMOVED -> {
                            var delJournal = doc.document.toObject(
                                Journal::class.java
                            )
                            delJournal.id = doc.document.id
                            journalsAdapter.deleteJournal(
                                delJournal
                            )
                        }
                    }
                }

            }
    }
}