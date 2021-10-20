package com.example.gifapp.ui.fragments.oneGif

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gifapp.databinding.FragmentOneGifBinding
import com.example.gifapp.ui.fragments.oneGif.tools.GifPagerAdapter
import com.example.gifapp.ui.fragments.oneGif.tools.OnEndOfListReached

class OneGifFragment : Fragment(), OnEndOfListReached {

    private lateinit var binding: FragmentOneGifBinding
    private lateinit var gifPagerAdapter: GifPagerAdapter

    private lateinit var viewModel: OneGifViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOneGifBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(OneGifViewModel::class.java)

        gifPagerAdapter = GifPagerAdapter(viewModel.getGifsList(), this)
        binding.vpGif.adapter = gifPagerAdapter
        binding.vpGif.currentItem = OneGifFragmentArgs.fromBundle(requireArguments()).gifItemPosition

        viewModel.getGifsData().observe(viewLifecycleOwner, { gifPagerAdapter.notifyDataSetChanged() })
    }

    override fun onEndReached(offset: Int) {
        viewModel.getGifs(null, offset)
    }
}