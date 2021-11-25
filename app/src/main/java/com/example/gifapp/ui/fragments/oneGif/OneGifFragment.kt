package com.example.gifapp.ui.fragments.oneGif

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gifapp.App
import com.example.gifapp.BuildConfig
import com.example.gifapp.R
import com.example.gifapp.databinding.FragmentOneGifBinding
import com.example.gifapp.ui.fragments.oneGif.tools.GifPagerAdapter
import com.example.gifapp.ui.fragments.oneGif.tools.OnEndOfListReached
import javax.inject.Inject

class OneGifFragment : Fragment(), OnEndOfListReached {

    private lateinit var binding: FragmentOneGifBinding
    private lateinit var gifPagerAdapter: GifPagerAdapter

    @Inject
    lateinit var viewModel: OneGifViewModel

    init {
        App.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentOneGifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initRequiredData(requireContext().cacheDir.absolutePath + BuildConfig.CACHE_DIR)
        lifecycle.addObserver(viewModel)

        configViewPager()
        setObservers()
    }

    private fun setObservers() {
        viewModel.getGifsData().observe(viewLifecycleOwner, { gifPagerAdapter.addItems(it)})
        viewModel.getIsInternetConnectionError().observe(viewLifecycleOwner, { Toast.makeText(requireContext(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show() })
    }

    private fun configViewPager() {
        gifPagerAdapter = GifPagerAdapter(viewModel.getGifsList(), this)
        binding.vpGif.adapter = gifPagerAdapter
        binding.vpGif.currentItem = OneGifFragmentArgs.fromBundle(requireArguments()).gifItemPosition
    }

    override fun onEndReached(offset: Int) {
        viewModel.getGifs(null, null)
    }
}