package hu.bme.aut.android.traveljournal.ui.my_journals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyJournalsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is my journals Fragment"
    }
    val text: LiveData<String> = _text
}