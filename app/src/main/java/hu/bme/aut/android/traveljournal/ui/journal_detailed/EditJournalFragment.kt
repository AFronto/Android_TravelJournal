package hu.bme.aut.android.traveljournal.ui.journal_detailed

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.adapter.PostsAdapter
import hu.bme.aut.android.traveljournal.data.Journal
import hu.bme.aut.android.traveljournal.data.Post
import kotlinx.android.synthetic.main.fragment_edit_journal.*

class EditJournalFragment : Fragment() {

    private lateinit var postssAdapter: PostsAdapter
    val db = FirebaseFirestore.getInstance()
    companion object{
        var journalId: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_edit_journal, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etTitle.setText(arguments?.getString("title"))
        tvAuthor.text = "Written by: ${arguments?.getString("author")}"
        journalId = journalId?:arguments?.getString("id")

        btnSave.setOnClickListener() {
            val journal = hashMapOf(
                "author" to arguments?.getString("author"),
                "title" to etTitle.text.toString(),
                "rating" to arguments?.getInt("rating"),
                "authorId" to arguments?.getString("authorId")
            )
            if (journalId.isNullOrBlank()) {
                var newDoc = db.collection("journals").document()
                journalId = newDoc.id
                newDoc.set(journal)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully added!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
            } else {
                db.collection("journals").document(journalId!!)
                    .set(journal)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
            }
        }

        addPostFab.setOnClickListener {
            if (journalId.isNullOrBlank()) {
                Toast.makeText(context, "There is no journal created yet!", Toast.LENGTH_SHORT).show()
            } else {
                var bundle =
                    bundleOf(
                        "id" to "",
                        "title" to "",
                        "body" to "",
                        "parentJournalId" to journalId
                    )
                findNavController().navigate(R.id.nav_edit_post, bundle)
            }
        }

        if (context != null) {

            postssAdapter = PostsAdapter(context!!, mutableListOf())

            rvPosts.layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            rvPosts.adapter = postssAdapter
        }

        initPostsListener()
    }

    private fun initPostsListener() {
        db.collection("posts")
            .whereEqualTo("parentJournalId", journalId)
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
                            postssAdapter.addPost(
                                newPost
                            )
                        }
                    }
                }

            }
    }

}