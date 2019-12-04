package hu.bme.aut.android.traveljournal.ui.journal_detailed

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.adapter.PostsAdapter
import hu.bme.aut.android.traveljournal.data.Post
import hu.bme.aut.android.traveljournal.extensions.validateNonEmpty
import kotlinx.android.synthetic.main.fragment_edit_journal.*

class EditJournalFragment : Fragment(), PostsAdapter.PostClickListener {
    override fun onItemLongClick(post: Post, view: View): Boolean {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.menu_todo)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> deletePost(post)
            }
            false
        }
        popup.show()
        return false
    }

    override fun onItemClick(post: Post) {
        var bundle =
            bundleOf(
                "id" to post.id,
                "title" to post.title,
                "body" to post.body,
                "parentJournalId" to journalId,
                "creationTime" to post.creationTime,
                "img" to post.img
            )
        findNavController().navigate(R.id.nav_edit_post, bundle)
    }

    private lateinit var registration: ListenerRegistration
    private lateinit var postsAdapter: PostsAdapter
    val db = FirebaseFirestore.getInstance()

    companion object {
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
        journalId = journalId ?: arguments?.getString("id")

        btnSave.setOnClickListener() {
            saveJournal()
        }

        addPostFab.setOnClickListener {
            createNewPost()
        }

        if (context != null) {

            postsAdapter = PostsAdapter(context!!, mutableListOf())

            rvPosts.layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            rvPosts.adapter = postsAdapter
        }

        postsAdapter.itemClickListener = this

        initPostsListener()
    }

    private fun saveJournal() {
        if (!etTitle.validateNonEmpty()) {
            Toast.makeText(context, "Missing journal title!", Toast.LENGTH_SHORT).show()
        } else {
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
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully added!")
                        Toast.makeText(
                            context,
                            "Journal successfully saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
            } else {
                db.collection("journals").document(journalId!!)
                    .set(journal)
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "DocumentSnapshot successfully updated!"
                        )
                        Toast.makeText(
                            context,
                            "Journal successfully saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
            }
        }
    }

    private fun createNewPost() {
        if (journalId.isNullOrBlank()) {
            Toast.makeText(context, "There is no journal created yet!", Toast.LENGTH_SHORT)
                .show()
        } else {
            var bundle =
                bundleOf(
                    "id" to "",
                    "title" to "",
                    "body" to "",
                    "parentJournalId" to journalId,
                    "creationTime" to null,
                    "img" to null
                )
            findNavController().navigate(R.id.nav_edit_post, bundle)
        }
    }

    private fun deletePost(post: Post) {
        db.collection("posts").document(post.id!!).delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    override fun onStop() {
        super.onStop()
        registration.remove()
    }

    private fun initPostsListener() {
        registration = db.collection("posts")
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
                            postsAdapter.addPost(
                                newPost
                            )
                        }
                        DocumentChange.Type.MODIFIED -> {
                            var modPost = doc.document.toObject(
                                Post::class.java
                            )
                            modPost.id = doc.document.id
                            postsAdapter.updatePost(
                                modPost
                            )
                        }
                        DocumentChange.Type.REMOVED -> {
                            var delPost = doc.document.toObject(
                                Post::class.java
                            )
                            delPost.id = doc.document.id
                            postsAdapter.deletePost(
                                delPost
                            )
                        }
                    }
                }

            }
    }

}