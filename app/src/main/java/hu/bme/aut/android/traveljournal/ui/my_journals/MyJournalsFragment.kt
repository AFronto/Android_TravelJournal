package hu.bme.aut.android.traveljournal.ui.my_journals

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.DocumentChange
import hu.bme.aut.android.traveljournal.JournalsActivity
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.data.Journal
import hu.bme.aut.android.traveljournal.ui.base.BaseJournalListFragment
import hu.bme.aut.android.traveljournal.ui.journal_edit.EditJournalFragment
import kotlinx.android.synthetic.main.fragment_my_journals.*

class MyJournalsFragment : BaseJournalListFragment() {
    override fun onItemLongClick(journal: Journal, view: View): Boolean {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.menu_todo)
        if (journal.onGoing) {
            popup.menu.getItem(0).title = "FINISH"
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> deleteOrFinishJournal(journal)
            }
            false
        }
        popup.show()
        return false
    }

    override fun onItemClick(journal: Journal) {
        var bundle =
            bundleOf(
                "id" to journal.id,
                "title" to journal.title,
                "authorId" to journal.authorId,
                "author" to journal.author,
                "rating" to journal.rating,
                "privatized" to journal.privatized,
                "onGoing" to journal.onGoing
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

    private fun deleteOrFinishJournal(journal: Journal) {
        if (journal.onGoing) {
            db.collection("journals").document(journal.id!!)
                .update("onGoing", false)
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
        } else {
            db.collection("journals").document(journal.id!!).delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }

            db.collection("posts")
                .whereEqualTo("parentJournalId", journal.id)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        db.collection("posts").document(document.id).delete()
                            .addOnSuccessListener {
                                Log.d(
                                    TAG,
                                    "DocumentSnapshot successfully deleted!"
                                )
                            }
                            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

    override fun fragmentSpecificInit() {
        //reset journalId
        EditJournalFragment.journalId = null

        addJournalFab.setOnClickListener {
            if (journalsAdapter.isThereOnGoing()) {
                Toast.makeText(context, "You already have an on going Journal!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                var bundle =
                    bundleOf(
                        "id" to "",
                        "title" to "",
                        "authorId" to (context as JournalsActivity).uid,
                        "author" to (context as JournalsActivity).userName,
                        "rating" to 0,
                        "privatized" to false,
                        "onGoing" to true
                    )
                findNavController().navigate(R.id.nav_edit_journal, bundle)
            }
        }

        registration = db.collection("journals")
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