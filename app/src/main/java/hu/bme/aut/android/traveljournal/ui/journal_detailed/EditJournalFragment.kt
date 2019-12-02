package hu.bme.aut.android.traveljournal.ui.journal_detailed

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import hu.bme.aut.android.traveljournal.R
import hu.bme.aut.android.traveljournal.data.Journal
import kotlinx.android.synthetic.main.fragment_edit_journal.*

class EditJournalFragment : Fragment() {


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
    }

}