package edu.utap.stocknewsapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.utap.stocknewsapp.api.NewsData
import edu.utap.stocknewsapp.databinding.RowNewsBinding
import edu.utap.stocknewsapp.glide.Glide

class NewsRowAdapter(private val viewModel: MainViewModel)
    : ListAdapter<NewsData, NewsRowAdapter.VH>(NewsDiff()){

    inner class VH(val rowNewsBinding: RowNewsBinding)
        : RecyclerView.ViewHolder(rowNewsBinding.root) {
        init {
            rowNewsBinding.root.setOnClickListener {
                MainViewModel.goToNews(rowNewsBinding.root.context,getItem(adapterPosition))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = RowNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        // Handle views in row binding
        val binding = holder.rowNewsBinding
        val news = getItem(position)
        Glide.glideFetch(news.image_url!!,binding.rowNewsIV)
        binding.rowNewsTitle.text = news.title
        val description = news.description
        if (description?.length!! < 140) {
            binding.rowNewsDescription.text = description
        } else {
            binding.rowNewsDescription.text =
                String.format("%s...(read more)",description.subSequence(0,120))
        }
        val concat = news.entities?.asSequence()?.map(NewsData::symbol)?.joinToString(", ")
        binding.rowNewsSymbol.text = concat
    }

    class NewsDiff : DiffUtil.ItemCallback<NewsData>() {
        override fun areItemsTheSame(oldItem: NewsData, newItem: NewsData): Boolean {
            return oldItem.uuid == newItem.uuid
        }
        override fun areContentsTheSame(oldItem: NewsData, newItem: NewsData): Boolean {
            return NewsData.spannableStringsEqual(oldItem.title, newItem.title) &&
                    NewsData.spannableStringsEqual(oldItem.description, newItem.description) //&&
                    //NewsData.spannableStringsEqual(oldItem.entity, newItem.entity) &&
                    //NewsData.spannableStringsEqual(oldItem.companyName, newItem.companyName) &&
                    //NewsData.spannableStringsEqual(oldItem.exchange, newItem.exchange)
        }
    }
}