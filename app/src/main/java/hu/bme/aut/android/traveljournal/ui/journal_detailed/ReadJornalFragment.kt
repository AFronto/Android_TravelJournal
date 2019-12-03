package hu.bme.aut.android.traveljournal.ui.journal_detailed

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
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.adapter.PostsAdapter
import hu.bme.aut.android.traveljournal.data.Post
import kotlinx.android.synthetic.main.fragment_read_journal.*

class ReadJornalFragment : Fragment(), PostsAdapter.PostClickListener{
    override fun onItemClick(post: Post) {

    }

    private lateinit var postssAdapter: PostsAdapter
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

            postssAdapter = PostsAdapter(context!!, mutableListOf())

            rvPosts.layoutManager = LinearLayoutManager(context).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            rvPosts.adapter = postssAdapter
        }

        postssAdapter.itemClickListener = this

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