package com.example.meditrack.homeActivity.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meditrack.dataModel.dataClasses.SearchItemData

class SearchViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    val _dataList = MutableLiveData<List<SearchItemData?>>()

    fun setValueSearchItemsList(items:List<SearchItemData?>)
    {
        _dataList.value = items
    }
}