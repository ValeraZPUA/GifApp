package com.example.gifapp.ui.fragments.gifLIst.recyclerViewTools

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gifapp.R
import com.example.gifapp.databinding.RvItemGifBinding
import com.example.gifapp.db.entities.GifItemEntity

class GifListAdapter(private val gifsList: ArrayList<GifItemEntity>,
                     private val onBottomReachedListener: OnBottomReachedListener,
                     private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<GifListAdapter.GifViewHolder>() {

    private lateinit var binding: RvItemGifBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifViewHolder {
        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.rv_item_gif, parent, false)
        return GifViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GifViewHolder, position: Int) {
        if (position == itemCount - 1) {
            onBottomReachedListener.onBottomReached(itemCount)
        }
        holder.bind(gifsList[position])
    }

    override fun getItemCount(): Int {
        return gifsList.size
    }

    fun addItems(newGifList: ArrayList<GifItemEntity>) {
        gifsList.addAll(newGifList)
        notifyItemInserted(0)
    }

    fun clearGifList() {
        notifyItemRangeRemoved(0, gifsList.size)
        gifsList.clear()
    }

    private fun deleteItemByPosition(position: Int): Boolean {
        gifsList.removeAt(position)
        notifyItemRemoved(position)
        return true
    }
    inner class GifViewHolder(private val binding: RvItemGifBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnLongClickListener {
                onItemClickListener.onItemLongClick(gifsList[bindingAdapterPosition].id)
                deleteItemByPosition(bindingAdapterPosition)
            }
        }

        fun bind(gifItemEntity: GifItemEntity) {
            Log.d("tag22", "bind, gifItemEntity.is_deleted: ${gifItemEntity.id} ${gifItemEntity.is_deleted}")
            if (!gifItemEntity.is_deleted) {

                Glide
                    .with(binding.root)
                    .load(gifItemEntity.image_url)
                    //.apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE).skipMemoryCache(true))
                    .thumbnail(Glide
                        .with(binding.root)
                        .load(R.raw.loader))
                    .into(binding.ivGif)
            } else {
                itemView.layoutParams.height = 0
            }
        }

    }
}