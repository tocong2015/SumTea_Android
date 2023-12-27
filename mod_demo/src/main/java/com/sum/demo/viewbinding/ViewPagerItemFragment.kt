package com.sum.demo.viewbinding

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sum.demo.databinding.FragmentViewPagerItemBinding
import com.sum.framework.base.BaseDataBindFragment


class ViewPagerItemFragment : BaseDataBindFragment<FragmentViewPagerItemBinding>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val index = arguments?.getString("index") ?: ""
        Handler(Looper.getMainLooper()).postDelayed({
            mBinding?.custom?.setText(index)
            mBinding?.tv?.setText(index)
        }, 2000)
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("oliver", "$this onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("oliver", "$this onDestroyView")
    }


}