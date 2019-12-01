package hu.bme.aut.android.traveljournal.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.data.Journal
import kotlinx.android.synthetic.main.journal_card.view.*

class JournalsAdapter(private val context: Context) : RecyclerView.Adapter<JournalsAdapter.ViewHolder>() {

    private val journalList: MutableList<Journal> = mutableListOf()
    private var lastPosition = -1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthor: TextView = itemView.tvJournalAuthor
        val tvTitle: TextView = itemView.tvJournalTitle
        val tvRating: TextView = itemView.tvJournalRating
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
                .from(viewGroup.context)
                .inflate(R.layout.journal_card, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val tmpJournal = journalList[position]
        viewHolder.tvAuthor.text = "Created by: ${tmpJournal.author}"
        viewHolder.tvTitle.text = tmpJournal.title
        viewHolder.tvRating.text = tmpJournal.rating.toString()

        setAnimation(viewHolder.itemView, position)
    }

    override fun getItemCount() = journalList.size

    fun addJournal(journal: Journal) {
        journal ?: return

        journalList.add(journal)
        journalList.sortBy { j -> j.rating }
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