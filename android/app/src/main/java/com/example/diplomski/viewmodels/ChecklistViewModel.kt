package com.example.diplomski.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.diplomski.StudyApplication
import com.example.diplomski.database.tables.Checklist
import com.example.diplomski.repositories.ChecklistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class taskState(
    val text : String = "",
    val user: Int = 0
)
class ChecklistViewModel(
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<Checklist>>(emptyList())
    val items = _items.asStateFlow()

    var taskState by mutableStateOf(taskState())
        private set
    fun getItems(id: Int){
        taskState = taskState.copy(user = id)
        viewModelScope.launch {
            checklistRepository.getAllItems(id).collect { items->
                _items.value = items
            }
        }
    }

    fun setText(t : String) {
        taskState = taskState.copy(text = t)
    }

    fun insert(){
        viewModelScope.launch {
            checklistRepository.insert(Checklist(0, taskState.text, taskState.user, false))
            taskState = taskState.copy(text = "")
        }
    }

    fun delete(id: Int){
        viewModelScope.launch {
            checklistRepository.delete(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val checklistRepository = (this[APPLICATION_KEY] as StudyApplication).checklistRepository
                return@initializer ChecklistViewModel(checklistRepository)
            }
        }
    }

}