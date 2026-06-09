package com.example.remarket_project.ui.wishlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.remarket_project.R
import com.example.remarket_project.auth.IntroActivity
import com.example.remarket_project.auth.SessionManager
import com.example.remarket_project.databinding.FragmentWishlistBinding
import com.example.remarket_project.viewmodel.WishlistViewModel

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WishlistViewModel by viewModels()

    private val adapter = WishlistAdapter(
        onItemClick = { product ->
            findNavController().navigate(
                R.id.action_wishlistFragment_to_productDetailFragment,
                bundleOf("productId" to product.id)
            )
        },
        onRemoveClick = { product ->
            viewModel.removeWishlist(product.id)
            Toast.makeText(requireContext(), "찜 목록에서 제거했습니다.", Toast.LENGTH_SHORT).show()
        }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.wishlist.observe(viewLifecycleOwner) { wishlist ->
            adapter.submitList(wishlist)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // 토큰 만료 or 미로그인 → 세션 초기화 후 로그인 화면으로
        viewModel.sessionExpired.observe(viewLifecycleOwner) { expired ->
            if (expired) {
                viewModel.clearSessionExpired()
                SessionManager.clearSession(requireContext())
                Toast.makeText(requireContext(), "세션이 만료됐습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show()
                startActivity(Intent(requireContext(), IntroActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
    }

    // 돌아올 때 찜 목록 새로고침
    override fun onResume() {
        super.onResume()
        viewModel.loadWishlist()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
