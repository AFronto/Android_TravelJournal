package hu.bme.aut.android.traveljournal.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.data.Post
import kotlinx.android.synthetic.main.post_card.view.*

class PostsAdapter(private val context: Context, private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    private var lastPosition = -1
    var itemClickListener: PostClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.tvPostTitle
        val tvBody: TextView = itemView.tvPostBody
        val imgPost: ImageView = itemView.imgPost

        var post: Post? = null

        init {
            itemView.setOnClickListener {
                post?.let { post -> itemClickListener?.onItemClick(post) }
            }
            itemView.setOnLongClickListener {
                post.let { post
                    ->
                    itemClickListener?.onItemLongClick(post!!, itemView)
                    true
                }
            }

        }
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

        if (tmpPost.img.isNullOrBlank()) {
            viewHolder.imgPost.visibility = View.GONE
        } else {
            Glide.with(context).load(tmpPost.img).into(viewHolder.imgPost)
            viewHolder.imgPost.visibility = View.VISIBLE
        }

        setAnimation(viewHolder.itemView, position)
    }

    override fun getItemCount() = postList.size

    fun addPost(post: Post) {
        if(postList.find{it.id == post.id} != null){
            updatePost(post)
            return
        }

        postList.add(post)
        notifyDataSetChanged()
    }

    fun updatePost(post: Post) {
        val index = postList.indexOfFirst { old -> old.id == post.id }
        postList[index] = post
        notifyItemChanged(index)
    }

    fun deletePost(post: Post) {
        val index = postList.indexOfFirst { old -> old.id == post.id }
        postList.removeAt(index)
        notifyItemRemoved(index)
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    interface PostClickListener {
        fun onItemClick(post: Post)
        fun onItemLongClick(post: Post, view: View): Boolean
    }
}