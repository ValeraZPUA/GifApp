package com.example.gifapp.ui.fragments.gifLIst

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gifapp.App
import com.example.gifapp.BuildConfig
import com.example.gifapp.R
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
import javax.inject.Inject

class GifListFragment : Fragment(), OnBottomReachedListener, OnItemLongClickListener, OnItemClickListener {

    private lateinit var binding: FragmentGifListBinding
    private lateinit var adapter: GifListAdapter
    private var isSearchEnabled = false

    @Inject
    lateinit var viewModel: GifListViewModel

    init {
        App.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initRequiredData(requireContext().cacheDir.absolutePath + BuildConfig.CACHE_DIR)
        lifecycle.addObserver(viewModel)

        configRecycler()
        setObservers()

        binding
            .etSearch
            .textChanges()
            .debounce(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (isSearchEnabled) {
                    adapter.submitList(arrayListOf())
                    viewModel.getGifs(it.toString(), 0)
                }
                isSearchEnabled = true
            }
    }

    private fun setObservers() {
        viewModel.getGifsData().observe(viewLifecycleOwner) {
            val tempList = adapter.currentList.toMutableList()
            tempList.addAll(it)
            adapter.submitList(tempList)
        }
        viewModel.getIsInternetConnectionError().observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
        }
    }

    private fun configRecycler() {
        adapter = GifListAdapter(this, this, this)
        adapter.submitList(viewModel.getGifList())
        binding.rvGifs.adapter = adapter
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
        viewModel.getGifsData().value?.clear()
    }
}