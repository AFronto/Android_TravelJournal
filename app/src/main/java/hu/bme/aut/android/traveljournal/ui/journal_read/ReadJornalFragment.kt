package hu.bme.aut.android.traveljournal.ui.journal_read

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.adapter.PostsAdapter
import hu.bme.aut.android.traveljournal.data.Post
import kotlinx.android.synthetic.main.fragment_read_journal.*

class ReadJornalFragment : Fragment(), PostsAdapter.PostClickListener{
    override fun onItemLongClick(post: Post, view: View): Boolean {
        return false
    }

    override fun onItemClick(post: Post) {

    }

    private lateinit var postsAdapter: PostsAdapter
    lateinit var registration: ListenerRegistration
    val db = FirebaseFirestore.getInstance()

    companion object {
        var journalId: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_read_journal, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        journalId = journalId ?: arguments?.getString("id")

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