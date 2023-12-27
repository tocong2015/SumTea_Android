package com.sum.demo.viewbinding

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.sum.common.constant.DEMO_ACTIVITY_VIEWBINDING
import com.sum.demo.databinding.ActivityViewPagerBinding
import com.sum.framework.base.BaseDataBindActivity

/**
 * 这个实例证明了
 *fragment 走了 onDestroyView() ，将binding=null,销毁与它相关的视图，当再次切回到该fragment,会走 onCreateView()，重新创建视图
 * 那么之前视图上的组件的 数据都没了。
 * */
@Route(path = DEMO_ACTIVITY_VIEWBINDING)
class ViewPagerActivity : BaseDataBindActivity<ActivityViewPagerBinding>() {


    override fun initView(savedInstanceState: Bundle?) {

        mBinding.viewPager.apply {
            val adapter = ViewPagerItemFragmentAdapter(supportFragmentManager, createFragments())
            setAdapter(adapter)

        }

    }

    private fun createFragments(): List<ViewPagerItemFragment> {
        val fragments = mutableListOf<ViewPagerItemFragment>()
        for (i in 0..5) {
            val fragment = ViewPagerItemFragment().apply {
                arguments = Bundle().apply {
                    putString("index", "$i")
                }
            }
            fragments.add(fragment)
        }
        return fragments
    }
}