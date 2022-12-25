package com.example.canteenapp.ui.admin

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentAdminPanelBinding

class AdminPanelFragment : Fragment() {
    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AdminPanelViewModel

    companion object {
        fun newInstance() = AdminPanelFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        setBindings()
        setListeners()
    }



    private fun setBindings() {
        binding.settingsHint.actionCategoryTitle.text = "Панель"
        binding.settingsHint.actionTitle.text = "Администратора"

        binding.schoolParamsButton.settingsButton.text = "Параметры школы"
        binding.allUsersButton.settingsButton.text = "Все пользователи"
        binding.allDishesButton.settingsButton.text = "Список блюд"
        binding.scheduleButton.settingsButton.text = "Расписание питания"
        binding.allRequestsButton.settingsButton.text = "Посмотреть заявки"
    }

    private fun setListeners() {
        binding.schoolParamsButton.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminPanelFragment_to_schoolSettingsFragment)
        }

        binding.allDishesButton.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.toAllDishes)
        }

        binding.allRequestsButton.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.goToRequests)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[AdminPanelViewModel::class.java]
    }

}