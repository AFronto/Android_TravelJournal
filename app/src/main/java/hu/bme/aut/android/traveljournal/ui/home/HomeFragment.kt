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
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), SearchView.OnQueryTextListener {
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

    private lateinit var journalsAdapter: JournalsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        journalSearchView.setOnQueryTextListener(this)
        super.onStart()
        if (context != null) {

            journalsAdapter = JournalsAdapter(context!!, mutableListOf())

            rvJournals.layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            rvJournals.adapter = journalsAdapter
        }
        initJournalsListener()
    }

    private fun initJournalsListener() {
        val db = FirebaseFirestore.getInstance()
        db.collection("journals")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }


                for (doc in value!!.documentChanges) {
                    when (doc.type) {
                        DocumentChange.Type.ADDED -> journalsAdapter.addJournal(doc.document.toObject(Journal::class.java))
                    }
                }

            }
    }
}