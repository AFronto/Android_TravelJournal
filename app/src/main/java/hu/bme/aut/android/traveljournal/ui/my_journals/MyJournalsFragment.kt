package hu.bme.aut.android.traveljournal.ui.my_journals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import hu.bme.aut.android.traveljournal.R

class MyJournalsFragment : Fragment() {

    private lateinit var myJournalsViewModel: MyJournalsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myJournalsViewModel =
            ViewModelProviders.of(this).get(MyJournalsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_my_journals, container, false)
        val textView: TextView = root.findViewById(R.id.text_my_journals)
        myJournalsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}