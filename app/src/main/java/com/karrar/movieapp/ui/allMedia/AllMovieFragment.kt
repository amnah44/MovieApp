package com.karrar.movieapp.ui.allMedia

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.karrar.movieapp.R
import com.karrar.movieapp.databinding.FragmentAllMovieBinding
import com.karrar.movieapp.ui.base.BaseFragment
import com.karrar.movieapp.utilities.EventObserve
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AllMovieFragment : BaseFragment<FragmentAllMovieBinding>() {
    override val layoutIdFragment = R.layout.fragment_all_movie
    override val viewModel: AllMovieViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTitle(true, viewModel.type.value)
        setMovieAdapter()
        observeEvents()
    }

    private fun setMovieAdapter() {
//        binding.recyclerMedia.adapter =
//            MediaAdapter(mutableListOf(), R.layout.item_media, viewModel)
       val allMediaAdapter = AllMediaAdapter()
        binding.recyclerMedia.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = allMediaAdapter
        }

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.allMedia.collectLatest { pagingData ->
                allMediaAdapter.submitData(pagingData)
            }
        }
    }

    private fun observeEvents() {
        viewModel.backEvent.observe(viewLifecycleOwner, EventObserve { removeFragment() })
        viewModel.clickMovieEvent.observe(
            viewLifecycleOwner,
            EventObserve { movieID -> seeMovieDetails(movieID) })
    }

    private fun seeMovieDetails(movieID: Int) {
        findNavController().navigate(
            AllMovieFragmentDirections.actionAllMovieFragmentToMovieDetailFragment(
                movieID
            )
        )
    }

    private fun removeFragment() {
        findNavController().popBackStack()
    }
}