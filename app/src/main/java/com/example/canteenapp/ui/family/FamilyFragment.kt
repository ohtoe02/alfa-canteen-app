package com.example.canteenapp.ui.family

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentFamilyBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout

class FamilyFragment : Fragment() {

    private var _binding: FragmentFamilyBinding? = null

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFamilyBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()

        return root
    }

    private fun init() {
        sharedPreferences = this.requireActivity().getSharedPreferences(sharedPrefsId,
            Context.MODE_PRIVATE
        )

        val familyHintContainer = binding.familyHint
        familyHintContainer.actionTitle.text = "Моя семья"
        familyHintContainer.actionCategoryTitle.text = sharedPreferences.getString("familySurname", "Дети")

        binding.goToSettingsButton.root.text = "Общие настройки"
        binding.addChildButton.root.text = "Добавить ребенка"



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}