package com.rsupport.mobile1.test.ui.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.rsupport.mobile1.test.adapter.PhotoContentsAdapter
import com.rsupport.mobile1.test.databinding.ActivityMainBinding
import com.rsupport.mobile1.test.paging.PageScrollListener
import com.rsupport.mobile1.test.ui.state.UiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val PRELOADING_COUNT = 30
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModels()
    private var adapter: PhotoContentsAdapter? = null

    private val pageScrollListener: PageScrollListener = object : PageScrollListener() {
        override fun onNextPaging() {
            pagingEnable = false
            viewModel.nextPage()
        }

        override fun onTop(isTop: Boolean) {
            binding.activityMainTopScrollBtn.visibility = if (isTop) View.GONE else View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupRecyclerView()
        listener()
        subscribe()
        viewModel.fetchCollaborationPhoto()
    }

    private fun setupRecyclerView() {
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.activityMainRecyclerview.addItemDecoration(decoration)
        adapter = PhotoContentsAdapter()
        binding.activityMainRecyclerview.adapter = adapter
        pageScrollListener.setPreLoading(PRELOADING_COUNT)
        binding.activityMainRecyclerview.addOnScrollListener(pageScrollListener)
    }

    private fun subscribe() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is UiState.Uninitialized -> {
                    binding.activityMainSkeletonContainer.visibility = View.VISIBLE
                }

                is UiState.Empty -> {
                    binding.activityMainEmptyView.visibility = View.VISIBLE
                    binding.activityMainRefreshLayout.isRefreshing = false
                    binding.activityMainSkeletonContainer.visibility = View.GONE
                }

                is UiState.Success -> {
                    pageScrollListener.pagingEnable = state.isMore
                    adapter?.submitList(state.data)
                    binding.activityMainEmptyView.visibility = View.GONE
                    binding.activityMainRefreshLayout.isRefreshing = false
                    binding.activityMainSkeletonContainer.visibility = View.GONE
                }

                is UiState.Error -> {
                    Toast.makeText(this, "${state.exception?.message}", Toast.LENGTH_SHORT).show()
                    binding.activityMainRefreshLayout.isRefreshing = false
                    binding.activityMainSkeletonContainer.visibility = View.GONE
                }
            }
        }
    }

    private fun listener() {
        binding.activityMainTopScrollBtn.setOnClickListener {
            binding.activityMainRecyclerview.stopScroll()
            binding.activityMainRecyclerview.layoutManager?.scrollToPosition(0)
        }

        binding.activityMainRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }
}