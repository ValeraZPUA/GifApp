package com.example.gifapp.ui.fragments.oneGif

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.gifapp.R
import com.example.gifapp.ui.fragments.oneGif.tools.GifPagerAdapter
import com.example.gifapp.ui.fragments.oneGif.tools.OnEndOfListReached
import kotlinx.android.synthetic.main.fragment_one_gif.*

class OneGifFragment : Fragment(), OnEndOfListReached {

    private lateinit var gifPagerAdapter: GifPagerAdapter
    private lateinit var viewPagerGif: ViewPager

    private lateinit var viewModel: OneGifViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_one_gif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(OneGifViewModel::class.java)

        viewPagerGif = vpGif
        gifPagerAdapter = GifPagerAdapter(viewModel.getGifsList(), this)
        viewPagerGif.adapter = gifPagerAdapter
        viewPagerGif.currentItem = OneGifFragmentArgs.fromBundle(requireArguments()).gifItemPosition

        viewModel.gifsData.observe(viewLifecycleOwner, { gifPagerAdapter.notifyDataSetChanged() })
    }

    override fun onEndReached(offset: Int) {
        viewModel.getGifs(null, offset)
    }
}