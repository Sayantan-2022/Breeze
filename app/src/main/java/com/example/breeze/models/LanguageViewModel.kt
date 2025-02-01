package com.example.breeze.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LanguageViewModel : ViewModel() {
    private val _languageCode = MutableLiveData<String>()
    val languageCode: LiveData<String> get() = _languageCode

    fun setLanguage(language: String) {
        _languageCode.value = language
    }
}