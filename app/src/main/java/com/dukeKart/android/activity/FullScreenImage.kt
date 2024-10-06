package com.dukeKart.android.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.dukeKart.android.R
import com.dukeKart.android.common.AppUtils
import com.dukeKart.android.common.ZoomImageView
import com.dukeKart.android.database.AppConstants
import com.dukeKart.android.views.BaseActivity
import java.util.*

class FullScreenImage : BaseActivity() {
    var vp_zoom: ViewPager? = null
    var tb_zoom: TabLayout? = null
    var setSelection = 0
    var adapter: ZoomImageAdapter? = null
    var imageList: ArrayList<String>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)
        setIds()
        val i = intent
        imageList = i.getStringArrayListExtra(AppConstants.arrayImages)
        setSelection = i.getIntExtra(AppConstants.imageSelected, 0)
        setAdapters()
    }

    private fun setIds() {
        vp_zoom = findViewById(R.id.vp_zoom)
        tb_zoom = findViewById(R.id.tb_zoom)
    }

    fun setAdapters() {
        adapter = ZoomImageAdapter(imageList)
        vp_zoom!!.adapter = adapter
        tb_zoom!!.setupWithViewPager(vp_zoom)
        val length = tb_zoom!!.tabCount
        for (i in 0 until length) {
            val tab = (tb_zoom!!.getChildAt(0) as ViewGroup).getChildAt(i)
            val p = tab.layoutParams as MarginLayoutParams
            p.setMargins(0, 0, 15, 0)
            tab.requestLayout()
            tb_zoom!!.getTabAt(i)!!.customView = adapter!!.getTabView(i)
        }
        vp_zoom!!.setCurrentItem(setSelection, true)
    }

    inner class ZoomImageAdapter     // constructor
    (var imageList: ArrayList<String>?) : PagerAdapter() {
        private var inflater: LayoutInflater? = null
        override fun getCount(): Int {
            return this.imageList!!.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val btnClose: ImageView
            val imgDisplay: ZoomImageView
            var rlayoutLikeShare: RelativeLayout
            inflater = this@FullScreenImage
                    .getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val viewLayout = inflater!!.inflate(R.layout.layout_fullscreen, container,
                    false)
            imgDisplay = viewLayout.findViewById(R.id.imgDisplay)
            btnClose = viewLayout.findViewById(R.id.btnClose)
            if (imageList!![position].length > 0) {
                AppUtils.setImgPicasso(imageList!![position], this@FullScreenImage, imgDisplay)
            }
            // close button click event
            btnClose.setOnClickListener { this@FullScreenImage.finish() }
            container.addView(viewLayout)
            return viewLayout
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as RelativeLayout)
        }

        fun getTabView(position: Int): View {
            val view = LayoutInflater.from(this@FullScreenImage).inflate(R.layout.tab_layout, null)
            val icon = view.findViewById<ImageView>(R.id.icon)
            if (imageList!![position].length > 0) {
                AppUtils.setImgPicasso(imageList!![position], this@FullScreenImage, icon)
            }
            return view
        }
    }
}