package com.example.gifapp.ui.fragments.gifLIst

import com.example.gifapp.baseClasses.BaseViewModel
import com.example.gifapp.models.dataManager.DataManager
import javax.inject.Inject

class GifListViewModel @Inject constructor(private val dataManager: DataManager) : BaseViewModel(dataManager) {

    fun setDeleted(gifId: String) {
        dataManager.setDeleted(gifId)
    }
}