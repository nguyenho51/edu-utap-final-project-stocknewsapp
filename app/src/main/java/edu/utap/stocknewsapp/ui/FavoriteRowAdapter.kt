package edu.utap.stocknewsapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.stocknewsapp.api.NewsData
import edu.utap.stocknewsapp.databinding.RowFavoriteBinding

class FavoriteRowAdapter(private val viewModel: MainViewModel)
    : ListAdapter<NewsData, FavoriteRowAdapter.VH>(NewsRowAdapter.NewsDiff()) {

    inner class VH(val rowFavBinding: RowFavoriteBinding)
        : RecyclerView.ViewHolder(rowFavBinding.root) {
        init {
            // Swipe left to remove
            // XXX
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowFavoriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val binding = holder.rowFavBinding
        binding.rowFavSymbol.text = getItem(position).symbol
        binding.rowFavStock.text = getItem(position).name
        binding.rowFavExchange.text = getItem(position).exchange
    }

}