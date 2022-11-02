package com.karrar.movieapp.ui.search

import android.content.Context
import android.os.Bundle
import android.transition.ChangeTransform
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import androidx.recyclerview.widget.*
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentSearchBinding
import com.karrar.movieapp.ui.adapters.LoadUIStateAdapter
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.ui.search.adapters.*
import com.karrar.movieapp.ui.search.mediaSearchUIState.*
import com.karrar.movieapp.utilities.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    override val layoutIdFragment: Int = R.layout.fragment_search
    override val viewModel: SearchViewModel by viewModels()

    private val mediaSearchAdapter by lazy { MediaSearchAdapter(viewModel) }
    private val actorSearchAdapter by lazy { ActorSearchAdapter(viewModel) }

    private val oldValue = MutableStateFlow(MediaSearchUIState())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedElementEnterTransition = ChangeTransform()
        setTitle(false)
        getSearchResult()
        setSearchHistoryAdapter()
        getSearchResultsBySearchTerm()
        observeEvents()

    }

    private fun setSearchHistoryAdapter(){
        val inputMethodManager =
            binding.inputSearch.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.inputSearch, InputMethodManager.SHOW_IMPLICIT)

        binding.recyclerSearchHistory.adapter = SearchHistoryAdapter(mutableListOf(), viewModel)
    }

    @OptIn(FlowPreview::class)
    private fun getSearchResultsBySearchTerm(){
        lifecycleScope.launch {
            viewModel.uiState.debounce(500).collect{ searchTerm ->
                if (searchTerm.searchInput.isNotBlank() && oldValue.value.searchInput != viewModel.uiState.value.searchInput ||  oldValue.value.searchTypes != viewModel.uiState.value.searchTypes) {
                    getSearchResult()
                    oldValue.emit(viewModel.uiState.value)
                }
            }
        }
    }

    private fun getSearchResult(){
        when (viewModel.uiState.value.searchTypes) {
            MediaTypes.ACTOR -> { bindActors() }
            else -> { bindMedia() }
        }
    }

    private fun observeEvents() {
        viewModel.clickMediaEvent.observeEvent(viewLifecycleOwner) {
            when (it.mediaTypes) {
                Constants.MOVIE -> navigateToMovieDetails(it.mediaID)
                Constants.TV_SHOWS -> navigateToSeriesDetails(it.mediaID)
            }
        }

        viewModel.clickActorEvent.observeEvent(viewLifecycleOwner) { actorID -> navigateToActorDetails(actorID) }
        viewModel.clickBackEvent.observeEvent(viewLifecycleOwner) { popFragment() }

        viewModel.clickRetryEvent.observeEvent(viewLifecycleOwner){
            actorSearchAdapter.retry()
            mediaSearchAdapter.retry()
        }
    }

    private fun navigateToMovieDetails(movieId: Int) {
        findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToMovieDetailFragment(movieId))
    }

    private fun navigateToSeriesDetails(seriesId: Int) {
        findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToTvShowDetailsFragment(seriesId))
    }

    private fun navigateToActorDetails(actorId: Int) {
        findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToActorDetailsFragment(actorId))
    }


    private fun bindMedia() {
        val footerAdapter = LoadUIStateAdapter(mediaSearchAdapter::retry)
        binding.recyclerMedia.adapter = mediaSearchAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager = LinearLayoutManager(this@SearchFragment.context, RecyclerView.VERTICAL, false)

        collect(flow = mediaSearchAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, mediaSearchAdapter.itemCount) })

        getMediaSearchResults()
    }

    private fun bindActors() {
        val footerAdapter = LoadUIStateAdapter(actorSearchAdapter::retry)
        binding.recyclerMedia.adapter = actorSearchAdapter.withLoadStateFooter(footerAdapter)
        binding.recyclerMedia.layoutManager = GridLayoutManager(this@SearchFragment.context, 3)
        setSpanSize(footerAdapter)

        collect(flow = actorSearchAdapter.loadStateFlow,
            action = { viewModel.setErrorUiState(it, actorSearchAdapter.itemCount) })

        getActorsSearchResults()
    }

    private fun getMediaSearchResults(){
        lifecycleScope.launch {
            collectLast(viewModel.uiState.value.searchResult)
            { mediaSearchAdapter.submitData(it) }
        }
    }

    private fun getActorsSearchResults(){
        lifecycleScope.launch {
            collectLast(viewModel.uiState.value.searchResult)
            { actorSearchAdapter.submitData(it) }
        }
    }

    private fun setSpanSize(footerAdapter: LoadUIStateAdapter) {
        val mManager = binding.recyclerMedia.layoutManager as GridLayoutManager
        mManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if ((position == actorSearchAdapter.itemCount)
                    && footerAdapter.itemCount > 0
                ) { mManager.spanCount }
                else { 1 }
            }
        }
    }

    private fun popFragment() {
        findNavController().popBackStack()
    }

}