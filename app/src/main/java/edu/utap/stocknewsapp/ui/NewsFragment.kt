package edu.utap.stocknewsapp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import edu.utap.stocknewsapp.MainActivity
import edu.utap.stocknewsapp.R
import edu.utap.stocknewsapp.databinding.FragmentNewsBinding

class NewsFragment : Fragment(R.layout.fragment_news) {
    companion object {
        private const val titleKey = "News"
        fun newInstance(title: String): NewsFragment {
            val frag = NewsFragment()
            val bundle = Bundle()
            // XXX set the fragment's arguments
            bundle.putString(titleKey, title)
            frag.arguments = bundle
            return frag
        }
    }

    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentNewsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun initSwipeLayout(swipe : SwipeRefreshLayout) {
        swipe.setOnRefreshListener {
            swipe.isRefreshing = false
            viewModel.newsUpdated()
            viewModel.fetchNews()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNewsBinding.bind(view)
        Log.d(javaClass.simpleName, "News Frag Created")
        val rv = binding.newsRV
        val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        rv.layoutManager = layoutManager
        val adapter = NewsRowAdapter(viewModel)
        rv.adapter = adapter
        initSwipeLayout(binding.swipeRefreshLayout)
        viewModel.observeLiveNews().observe(viewLifecycleOwner) {
            val news = viewModel.observeLiveNews().value
            adapter.submitList(news)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}