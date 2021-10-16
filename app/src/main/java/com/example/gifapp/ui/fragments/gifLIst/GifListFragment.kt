package com.example.gifapp.ui.fragments.gifLIst

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gifapp.R
import com.example.gifapp.databinding.FragmentGifListBinding
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.ui.fragments.gifLIst.recyclerViewTools.GifListAdapter
import com.example.gifapp.ui.fragments.gifLIst.recyclerViewTools.OnBottomReachedListener
import com.example.gifapp.ui.fragments.gifLIst.recyclerViewTools.OnItemClickListener
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class GifListFragment : Fragment(), OnBottomReachedListener, OnItemClickListener {

    private lateinit var binding: FragmentGifListBinding
    private lateinit var viewModel: GifListViewModel

    private lateinit var adapter: GifListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gif_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = GifListAdapter(arrayListOf(), this, this)
        binding.rvGifs.adapter = adapter

        viewModel = ViewModelProvider(this).get(GifListViewModel::class.java)
        lifecycle.addObserver(viewModel)

        binding
            .etSearch
            .textChanges()
            .debounce(500,  TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                adapter.clearGifList()
                viewModel.getGifs(it.toString(), 0)
            }

        viewModel.gifsData.observe(requireActivity(), { gifsList ->
            gifsList?.let { addGifs(it) }
        })
    }

    private fun addGifs(gifsList: ArrayList<GifItemEntity>) {
        adapter.addItems(gifsList)
    }

    override fun onBottomReached(itemQuantity: Int) {
        Log.d("tag22", "onBottomReached: ")
        viewModel.getGifs(null, null)
    }

    override fun onItemLongClick(gifId: String) {
        Toast.makeText(context, gifId, Toast.LENGTH_SHORT).show()
        viewModel.setDeleted(gifId)
    }
}