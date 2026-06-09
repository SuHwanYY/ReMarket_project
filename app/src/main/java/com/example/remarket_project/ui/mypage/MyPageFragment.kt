package com.example.remarket_project.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.remarket_project.R
import com.example.remarket_project.auth.IntroActivity
import com.example.remarket_project.auth.SessionManager
import com.example.remarket_project.databinding.FragmentMypageBinding
import com.example.remarket_project.viewmodel.MyPageViewModel

class MyPageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyPageViewModel by viewModels()

    // 프로필 수정 후 돌아오면 닉네임 등 갱신
    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            loadUserInfo()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuMySales.tvMenuTitle.text = "내 판매 목록"
        binding.menuWishlist.tvMenuTitle.text = "찜 목록"
        binding.menuEditProfile.tvMenuTitle.text = "프로필 수정"
        binding.menuLogout.tvMenuTitle.text = "로그아웃"

        loadUserInfo()

        binding.btnEditProfile.setOnClickListener {
            editProfileLauncher.launch(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.menuMySales.root.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_mySalesFragment)
        }

        binding.menuWishlist.root.setOnClickListener {
            findNavController().navigate(R.id.wishlistFragment)
        }

        binding.menuEditProfile.root.setOnClickListener {
            editProfileLauncher.launch(Intent(requireContext(), EditProfileActivity::class.java))
        }

        binding.menuLogout.root.setOnClickListener {
            SessionManager.clearSession(requireContext())
            startActivity(Intent(requireContext(), IntroActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        viewModel.onSaleCount.observe(viewLifecycleOwner) { count ->
            binding.tvSalesCount.text = count.toString()
        }

        viewModel.soldOutCount.observe(viewLifecycleOwner) { count ->
            binding.tvSoldOutCount.text = count.toString()
        }

        viewModel.wishCount.observe(viewLifecycleOwner) { count ->
            binding.tvWishCount.text = count.toString()
        }
    }

    // 돌아올 때마다 통계 새로고침
    override fun onResume() {
        super.onResume()
        viewModel.loadStats()
    }

    private fun loadUserInfo() {
        binding.tvNickname.text = SessionManager.getNickname(requireContext())
        binding.tvEmail.text = SessionManager.getEmail(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
