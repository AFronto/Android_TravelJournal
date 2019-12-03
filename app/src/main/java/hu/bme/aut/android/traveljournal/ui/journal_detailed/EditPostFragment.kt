package hu.bme.aut.android.traveljournal.ui.journal_detailed

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import hu.bme.aut.android.traveljournal.R
import kotlinx.android.synthetic.main.fragment_edit_post.*
import kotlinx.android.synthetic.main.post_card.*
import java.util.*

class EditPostFragment : Fragment() {

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_edit_post, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        etPostTitle.setText(arguments?.getString("title"))
        etPostBody.setText(arguments?.getString("body"))
        btnSave.setOnClickListener {
            val post = hashMapOf(
                "parentJournalId" to arguments?.getString("parentJournalId"),
                "title" to etPostTitle.text.toString(),
                "body" to etPostBody.text.toString(),
                "creationTime" to Timestamp.now()
            )
            if (arguments?.getString("id").isNullOrBlank()) {
                db.collection("posts").document()
                    .set(post)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully added!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error adding document", e) }
            } else {
                db.collection("posts").document(arguments?.getString("id")!!)
                    .set(post)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
            }
            findNavController().navigateUp()
        }
    }
}