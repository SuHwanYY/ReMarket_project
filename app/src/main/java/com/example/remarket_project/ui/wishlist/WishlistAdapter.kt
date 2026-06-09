package com.example.remarket_project.ui.wishlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.remarket_project.R
import com.example.remarket_project.databinding.ItemWishlistBinding
import com.example.remarket_project.network.dto.ProductItem

// 찜 목록 어댑터 (찜 해제 버튼 포함)
class WishlistAdapter(
    private val onItemClick: (ProductItem) -> Unit = {},
    private val onRemoveClick: (ProductItem) -> Unit = {}
) : ListAdapter<ProductItem, WishlistAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemWishlistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductItem) {
            binding.tvTitle.text  = product.title
            binding.tvSeller.text = product.seller.nickname
            binding.tvPrice.text  = String.format("%,d원", product.price)

            bindImages(product)

            binding.btnRemoveWish.setOnClickListener { onRemoveClick(product) }
            binding.root.setOnClickListener { onItemClick(product) }
        }

        private fun bindImages(product: ProductItem) {
            val images = product.images
            val ctx = binding.root.context
            val categoryColor = when (product.category) {
                "의류"   -> ctx.getColor(R.color.categoryClothing)
                "전자기기" -> ctx.getColor(R.color.categoryElectronics)
                "도서"   -> ctx.getColor(R.color.categoryBooks)
                "운동용품" -> ctx.getColor(R.color.categorySports)
                else     -> ctx.getColor(R.color.mainColor)
            }

            binding.ivImg2.visibility    = android.view.View.GONE
            binding.dividerV1.visibility = android.view.View.GONE
            binding.dividerH1.visibility = android.view.View.GONE
            binding.layoutRow2.visibility = android.view.View.GONE
            binding.tvMoreCount.visibility = android.view.View.GONE

            if (images.isEmpty()) {
                binding.ivImg1.setImageDrawable(null)
                binding.ivImg1.setBackgroundColor(categoryColor)
                return
            }

            binding.ivImg1.setBackgroundColor(0)
            Glide.with(ctx).load(images[0]).centerCrop().placeholder(R.color.divider).into(binding.ivImg1)

            if (images.size >= 2) {
                binding.ivImg2.visibility    = android.view.View.VISIBLE
                binding.dividerV1.visibility = android.view.View.VISIBLE
                Glide.with(ctx).load(images[1]).centerCrop().placeholder(R.color.divider).into(binding.ivImg2)
            }

            if (images.size >= 3) {
                binding.dividerH1.visibility  = android.view.View.VISIBLE
                binding.layoutRow2.visibility = android.view.View.VISIBLE
                Glide.with(ctx).load(images[2]).centerCrop().placeholder(R.color.divider).into(binding.ivImg3)

                if (images.size >= 4) {
                    Glide.with(ctx).load(images[3]).centerCrop().placeholder(R.color.divider).into(binding.ivImg4)
                    if (images.size > 4) {
                        binding.tvMoreCount.visibility = android.view.View.VISIBLE
                        binding.tvMoreCount.text = "+${images.size - 4}"
                    }
                } else {
                    binding.ivImg4.setImageDrawable(null)
                    binding.ivImg4.setBackgroundColor(categoryColor)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ProductItem>() {
        override fun areItemsTheSame(oldItem: ProductItem, newItem: ProductItem) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ProductItem, newItem: ProductItem) =
            oldItem == newItem
    }
}
