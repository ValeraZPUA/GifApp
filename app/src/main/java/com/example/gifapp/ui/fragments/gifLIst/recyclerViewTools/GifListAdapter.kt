package com.example.gifapp.ui.fragments.gifLIst.recyclerViewTools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.gifapp.R
import com.example.gifapp.databinding.RvItemGifBinding
import com.example.gifapp.db.entities.GifItemEntity

class GifListAdapter(private val onBottomReachedListener: OnBottomReachedListener,
                     private val onItemLongClickListener: OnItemLongClickListener,
                     private val onItemClickListener: OnItemClickListener) : ListAdapter<GifItemEntity, GifListAdapter.GifViewHolder>(DiffSubmission()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifViewHolder {
        return GifViewHolder(RvItemGifBinding.inflate(LayoutInflater.from(parent.context), parent,false))
    }

    override fun onBindViewHolder(holder: GifViewHolder, position: Int) {
        if (position == itemCount - 1) {
            onBottomReachedListener.onBottomReached()
        }

        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    private fun deleteItemByPosition(position: Int): Boolean {
        currentList.toMutableList().filter { it.id != currentList[position].id }.also {
            submitList(it)
        }
        return true
    }

    inner class GifViewHolder(private val binding: RvItemGifBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnLongClickListener {
                onItemLongClickListener.onItemLongClick(currentList[bindingAdapterPosition].id)
                deleteItemByPosition(bindingAdapterPosition)
            }
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(bindingAdapterPosition)
            }
        }

        fun bind(gifItemEntity: GifItemEntity) {
            Glide
                .with(binding.root)
                .load(gifItemEntity.image_url)
                .apply(RequestOptions().override(200))
                //.apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true))
                .thumbnail(Glide
                    .with(binding.root)
                    .load(R.raw.loader))
                .into(binding.ivGif)
        }
    }

    internal class DiffSubmission : DiffUtil.ItemCallback<GifItemEntity>() {
        override fun areItemsTheSame(oldItem: GifItemEntity, newItem: GifItemEntity): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: GifItemEntity, newItem: GifItemEntity): Boolean {
            return oldItem == newItem
        }

    }
}