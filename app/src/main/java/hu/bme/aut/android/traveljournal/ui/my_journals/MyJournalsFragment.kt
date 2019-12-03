package hu.bme.aut.android.traveljournal.ui.my_journals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.Constraints
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.android.traveljournal.JournalsActivity
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.adapter.JournalsAdapter
import hu.bme.aut.android.traveljournal.data.Journal
import hu.bme.aut.android.traveljournal.ui.base.BaseJournalListFragment
import hu.bme.aut.android.traveljournal.ui.journal_detailed.EditJournalFragment
import kotlinx.android.synthetic.main.fragment_my_journals.*
import kotlinx.android.synthetic.main.searchable_journal_list.*

class MyJournalsFragment : BaseJournalListFragment() {

    override fun onItemClick(journal: Journal) {
        var bundle =
            bundleOf(
                "id" to journal.id,
                "title" to journal.title,
                "authorId" to journal.authorId,
                "author" to journal.author,
                "rating" to journal.rating
            )
        findNavController().navigate(R.id.nav_edit_journal, bundle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_my_journals, container, false)
        return root
    }

    override fun fragmentSpecificInit() {
        //reset journalId
        EditJournalFragment.journalId = null

        addJournalFab.setOnClickListener{
            var bundle =
                bundleOf(
                    "id" to "",
                    "title" to "",
                    "authorId" to (context as JournalsActivity).uid,
                    "author" to (context as JournalsActivity).userName,
                    "rating" to 0
                )
            findNavController().navigate(R.id.nav_edit_journal, bundle)
        }

        db.collection("journals")
            .whereEqualTo("authorId", (context as JournalsActivity).uid)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(Constraints.TAG, "Listen failed.", e)
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
                    }
                }

            }
    }
}