package com.example.lab7

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel() {
    private val _value = MutableLiveData<String>()

    val value: MutableLiveData<String>
        get() = _value

    init {
        _value.value = "default"
    }
}
