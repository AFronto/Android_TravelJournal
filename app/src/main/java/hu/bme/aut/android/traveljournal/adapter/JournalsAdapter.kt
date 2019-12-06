package hu.bme.aut.android.traveljournal.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.data.Journal
import kotlinx.android.synthetic.main.journal_card.view.*

class JournalsAdapter(private val context: Context, private val journalList: MutableList<Journal>) :
    RecyclerView.Adapter<JournalsAdapter.ViewHolder>() {

    private var lastPosition = -1
    private var filteredJournals = mutableListOf<Journal>()
    private var actualFilter = ""

    var itemClickListener: JournalClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAuthor: TextView = itemView.tvJournalAuthor
        val tvTitle: TextView = itemView.tvJournalTitle
        val tvRating: TextView = itemView.tvJournalRating
        val isFinished: ImageView = itemView.imgIsFinished

        var journal: Journal? = null

        init {
            itemView.setOnClickListener {
                journal?.let { journal -> itemClickListener?.onItemClick(journal) }
            }

            itemView.downVote.setOnClickListener {
                journal?.let { journal -> itemClickListener?.onItemDownVote(journal) }
            }

            itemView.upVote.setOnClickListener {
                journal?.let { journal -> itemClickListener?.onItemUpVote(journal) }
            }
            itemView.setOnLongClickListener {
                journal.let { journal
                    ->
                    itemClickListener?.onItemLongClick(journal!!, itemView)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(viewGroup.context)
            .inflate(R.layout.journal_card, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val tmpJournal = filteredJournals[position]
        viewHolder.journal = tmpJournal

        viewHolder.tvAuthor.text = "Written by: ${tmpJournal.author}"
        viewHolder.tvTitle.text = tmpJournal.title
        viewHolder.tvRating.text = tmpJournal.rating.toString()

        if(!tmpJournal.onGoing){
            viewHolder.isFinished.visibility =  View.VISIBLE
        }else{
            viewHolder.isFinished.visibility =  View.GONE
        }

        setAnimation(viewHolder.itemView, position)
    }

    override fun getItemCount() = filteredJournals.size

    fun addJournal(journal: Journal) {
        if(journalList.find{it.id == journal.id} != null){
            updateJournal(journal)
            return
        }

        journalList.add(journal)
        journalList.sortBy { j -> j.rating }
        filter(actualFilter) {}
    }

    fun updateJournal(journal: Journal) {
        journalList[journalList.indexOfFirst { old -> old.id == journal.id }] = journal
        journalList.sortBy { j -> j.rating }
        filter(actualFilter) {}
    }

    fun deleteJournal(journal: Journal) {
        journalList.removeAt(journalList.indexOfFirst { old -> old.id == journal.id })
        journalList.sortBy { j -> j.rating }
        filter(actualFilter) {}
    }

    fun isThereOnGoing(): Boolean {
        return journalList.any{ journal -> journal.onGoing }
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    fun filter(constraint: String?, onNothingFound: (() -> Unit)?) {
        actualFilter = constraint ?: ""
        actualFilter = actualFilter.toLowerCase()
        filteredJournals.clear()
        if (actualFilter.isNullOrBlank()) {
            filteredJournals.addAll(journalList)
        } else {
            val searchResults =
                journalList.filter { it.title!!.toLowerCase().contains(actualFilter) }
            filteredJournals.addAll(searchResults)
        }
        if (filteredJournals.isNullOrEmpty() and !journalList.isNullOrEmpty() )
            onNothingFound?.invoke()
        notifyDataSetChanged()
    }

    interface JournalClickListener {
        fun onItemClick(journal: Journal)
        fun onItemLongClick(journal: Journal, view: View): Boolean
        fun onItemDownVote(journal: Journal)
        fun onItemUpVote(journal: Journal)
    }
}