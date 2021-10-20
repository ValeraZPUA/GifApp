package com.example.gifapp.ui.fragments.gifLIst

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.gifapp.databinding.FragmentGifListBinding
import com.example.gifapp.databinding.FragmentGifListBinding.inflate
import com.example.gifapp.ui.fragments.gifLIst.recyclerViewTools.GifListAdapter
import com.example.gifapp.ui.fragments.gifLIst.recyclerViewTools.OnBottomReachedListener
import com.example.gifapp.ui.fragments.gifLIst.recyclerViewTools.OnItemClickListener
import com.example.gifapp.ui.fragments.gifLIst.recyclerViewTools.OnItemLongClickListener
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class GifListFragment : Fragment(), OnBottomReachedListener, OnItemLongClickListener, OnItemClickListener {

    private lateinit var binding: FragmentGifListBinding
    private lateinit var viewModel: GifListViewModel

    private lateinit var adapter: GifListAdapter

    private var isSearchEnabled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(GifListViewModel::class.java)

        adapter = GifListAdapter(viewModel.getGifList(), this, this, this)
        binding.rvGifs.adapter = adapter

        binding
            .etSearch
            .textChanges()
            .debounce(1000,  TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (isSearchEnabled) {
                    adapter.clearGifList()
                    viewModel.getGifs(it.toString(), 0)
                }
                isSearchEnabled = true
            }

        viewModel.getGifsData().observe(viewLifecycleOwner, { adapter.notifyItemInserted(adapter.itemCount) })
    }

    override fun onBottomReached() {
        viewModel.getGifs(null, null)
    }

    override fun onItemLongClick(gifId: String) {
        viewModel.setDeleted(gifId)
    }

    override fun onItemClick(gifPosition: Int) {
        findNavController().navigate(GifListFragmentDirections.actionGifListFragmentToOneGifFragment(gifPosition))
    }

    override fun onStop() {
        super.onStop()
        isSearchEnabled = false
    }
}