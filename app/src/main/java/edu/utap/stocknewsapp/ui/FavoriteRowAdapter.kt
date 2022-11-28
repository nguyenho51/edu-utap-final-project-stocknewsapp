package edu.utap.stocknewsapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.stocknewsapp.R
import edu.utap.stocknewsapp.api.NewsData
import edu.utap.stocknewsapp.databinding.RowFavoriteBinding

class FavoriteRowAdapter(private val viewModel: MainViewModel)
    : ListAdapter<NewsData, FavoriteRowAdapter.VH>(NewsRowAdapter.NewsDiff()) {

    inner class VH(val rowFavBinding: RowFavoriteBinding)
        : RecyclerView.ViewHolder(rowFavBinding.root)


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

        val loadingData = "Loading..."
        val price = getItem(position).currentPrice ?: loadingData
        if (price != loadingData) {
            binding.rowFavPrice.text = String.format("$%.2f", price.toFloat())
        } else {
            binding.rowFavPrice.text = price
        }
        val priceChange = getItem(position).percentChange ?: loadingData
        if (priceChange != loadingData) {
            if (priceChange.toFloat() >= 0) {
                binding.rowFavPriceChange.setBackgroundResource(R.drawable.rounded_corner_green)
                binding.rowFavPriceChange.text =
                    String.format("+%.2f%s", priceChange.toFloat(), "%")
            } else {
                binding.rowFavPriceChange.setBackgroundResource(R.drawable.rounded_corner_red)
                binding.rowFavPriceChange.text =
                    String.format("%.2f%s", priceChange.toFloat(), "%")
            }
        } else {
            binding.rowFavPriceChange.text = priceChange
        }
    }
}