package hu.bme.aut.android.traveljournal.ui.journal_edit

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import hu.bme.aut.android.traveljournal.JournalsActivity
import hu.bme.aut.android.traveljournal.R
import kotlinx.android.synthetic.main.fragment_edit_post.*
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.*

class EditPostFragment : Fragment() {

    val db = FirebaseFirestore.getInstance()

    companion object {
        private const val REQUEST_CODE = 101
    }

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

        if (arguments?.getString("img").isNullOrBlank()) {
            imgAttached.visibility = View.GONE
        } else {
            Glide.with(context!!).load(arguments?.getString("img")).into(imgAttached)
            imgAttached.visibility = View.VISIBLE
        }

        btnPhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent,
                REQUEST_CODE
            )
        }

        btnSave.setOnClickListener {
            btnSave.isEnabled = false
            if (imgAttached.visibility != View.VISIBLE) {
                uploadPost()
            } else {
                try {
                    uploadPostWithImage()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun uploadPost(imageUrl: String? = null) {
        val creationTime = arguments?.get("creationTime") ?: Timestamp.now()
        val post = hashMapOf(
            "parentJournalId" to arguments?.getString("parentJournalId"),
            "title" to etPostTitle.text.toString(),
            "body" to etPostBody.text.toString(),
            "creationTime" to creationTime,
            "img" to imageUrl,
            "location" to GeoPoint(arguments?.getDouble("locationLat")!!,arguments?.getDouble("locationLon")!!)
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
        btnSave.isEnabled = true
        findNavController().navigateUp()
    }

    private fun uploadPostWithImage() {
        val bitmap: Bitmap = (imgAttached.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference
        val newImageName = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImageRef = storageReference.child("images/$newImageName")

        newImageRef.putBytes(imageInBytes)
            .addOnFailureListener { exception ->
                (context as JournalsActivity).toast(exception.message)
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }

                newImageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                uploadPost(downloadUri.toString())
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == REQUEST_CODE) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap ?: return
            imgAttached.setImageBitmap(imageBitmap)
            imgAttached.visibility = View.VISIBLE
        }
    }

}