package hu.bme.aut.android.traveljournal.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast

import androidx.constraintlayout.widget.Constraints.TAG
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.adapter.JournalsAdapter
import hu.bme.aut.android.traveljournal.data.Journal
import hu.bme.aut.android.traveljournal.ui.base.BaseJournalListFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.searchable_journal_list.*

class HomeFragment : BaseJournalListFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun fragmentSpecificInit() {
        db.collection("journals")
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
                    }
                }

            }
    }
}