package hu.bme.aut.android.traveljournal.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.data.Post
import kotlinx.android.synthetic.main.post_card.view.*

class PostsAdapter(private val context: Context, private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    private var lastPosition = -1

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.tvPostTitle
        val tvBody: TextView = itemView.tvPostBody

        var post: Post? = null
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(viewGroup.context)
            .inflate(R.layout.post_card, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val tmpPost = postList[position]
        viewHolder.post = tmpPost

        viewHolder.tvTitle.text = tmpPost.title
        viewHolder.tvBody.text = tmpPost.body

        setAnimation(viewHolder.itemView, position)
    }

    override fun getItemCount() = postList.size

    fun addPost(post: Post) {
        post ?: return

        postList.add(post)
        notifyDataSetChanged()
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }
}