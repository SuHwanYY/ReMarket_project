package com.example.remarket_project.ui.product

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.remarket_project.R

// 상품 상세 이미지 슬라이더 어댑터 (ViewPager2)
class ImagePagerAdapter(
    private val imageUrls: List<String>,
    private val context: Context
) : RecyclerView.Adapter<ImagePagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val imageView = ImageView(parent.context).apply {
            // ViewPager2에서 각 페이지가 전체를 채우려면 RecyclerView.LayoutParams 써야 함
            // (일반 LayoutParams 쓰면 높이 0으로 나옴)
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context)
            .load(imageUrls[position])
            .centerCrop()
            .placeholder(R.color.divider)
            .into(holder.imageView)
    }

    override fun getItemCount() = imageUrls.size

    class ViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}
