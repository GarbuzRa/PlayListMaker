package com.example.playlistmaker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.model.SearchState
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.usecase.AddToSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.ClearSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.SearchTracksUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(private val searchTracksUseCase: SearchTracksUseCase,
                      private val getSearchHistoryUseCase: GetSearchHistoryUseCase,
                      private val addToSearchHistoryUseCase: AddToSearchHistoryUseCase,
                      private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel()  {
    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _showError = MutableLiveData<Boolean>()
    val showError: LiveData<Boolean> = _showError

    private val _showEmpty = MutableLiveData<Boolean>()
    val showEmpty: LiveData<Boolean> = _showEmpty

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private val _historyTracks = MutableLiveData<List<Track>>()
    val historyTracks: LiveData<List<Track>> = _historyTracks

    private var searchJob: Job? = null

    fun searchTracks(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(2000)
            _searchState.value = SearchState.Loading
            searchTracksUseCase.execute(query).collect{ result ->
                result.fold(
                    onSuccess = { tracks ->
                        if (tracks.isNotEmpty()) {
                            _searchState.value = SearchState.ShowSearchResults(tracks)
                        } else {
                            _searchState.value = SearchState.Empty
                        }

                    },
                    onFailure = {
                        _searchState.value = SearchState.Error
                    }
                )
            }
        }
    }

    fun getSearchHistory() {
        _historyTracks.value = getSearchHistoryUseCase.execute()
        if(_historyTracks.value.isNullOrEmpty()){
            _searchState.value = SearchState.NoHistory
        }
        else{
            _searchState.value = SearchState.ShowHistory
        }

    }

    fun addToSearchHistory(track: Track) {
        addToSearchHistoryUseCase.execute(track)
    }

    fun clearSearchHistory() {
        clearSearchHistoryUseCase.execute()
        getSearchHistory()
    }
}