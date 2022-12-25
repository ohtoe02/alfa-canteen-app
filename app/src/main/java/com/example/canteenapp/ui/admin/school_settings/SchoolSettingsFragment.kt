package com.example.canteenapp.ui.admin.school_settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentClassesBinding
import com.example.canteenapp.databinding.FragmentSchoolSettingsBinding
import com.example.canteenapp.databinding.FragmentSettingsBinding
import com.example.canteenapp.utils.createErrorSnack
import com.example.canteenapp.utils.createSuccessfulSnack
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SchoolSettingsFragment : Fragment() {
    private var _binding: FragmentSchoolSettingsBinding? = null
    private val binding get() = _binding!!

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var currentSchool: String? = null
    private var userLogin: String? = null

    private var schoolInfo: MutableMap<String, String>? = null

    private lateinit var currentSchoolDatabase: DatabaseReference
    private lateinit var currentSchoolRefDatabase: DatabaseReference

    companion object {
        fun newInstance() = SchoolSettingsFragment()
    }

    private lateinit var viewModel: SchoolSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchoolSettingsBinding.inflate(inflater, container, false)

        sharedPreferences = this.requireActivity().getSharedPreferences(
            sharedPrefsId,
            Context.MODE_PRIVATE
        )
        currentSchool = sharedPreferences.getString("school", "")
        userLogin = sharedPreferences.getString("login", "")

        currentSchoolDatabase =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/$currentSchool/info")

        currentSchoolRefDatabase =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("allSchools/$currentSchool")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        setBindings()
        getSchoolInfo()
    }

    private fun setBindings() {
        binding.hint.actionCategoryTitle.text = "Настройки"
        binding.hint.actionTitle.text = "Параметры школы"

        binding.classButton.settingsButton.text = "Список классов"
        binding.schoolTitle.setText(schoolInfo?.get("title") ?: "")
        binding.defaultPass.setText(schoolInfo?.get("defaultPassword") ?: "")

        binding.classButton.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_schoolSettingsFragment_to_classes_fragment)
        }

        binding.goBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.applyChangesButton.setOnClickListener {
            updateSchoolInfo()
        }
    }

    private fun getSchoolInfo() {
        currentSchoolDatabase.get().addOnSuccessListener {
            if (it.exists()) {
                schoolInfo = it.value as MutableMap<String, String>
                binding.schoolTitle.setText(schoolInfo?.get("title") ?: "")
                binding.defaultPass.setText(schoolInfo?.get("defaultPassword") ?: "")
            }
        }
    }

    private fun updateSchoolInfo() {
        val title = binding.schoolTitle.text.toString()
        val defaultPass = binding.defaultPass.text.toString()

        if (title == "" || defaultPass == "") {
            view?.let { createErrorSnack(it, "Какое-то из полей не заполнено") }
            return
        }

        schoolInfo?.set("title", binding.schoolTitle.text.toString())
        schoolInfo?.set("defaultPassword", binding.defaultPass.text.toString())

        binding.applyChangesButton.text = "Загрузка..."
        binding.applyChangesButton.isEnabled = false
        binding.applyChangesButton.setTextColor(resources.getColor(R.color.dark_main))

        currentSchoolRefDatabase.setValue(title)

        currentSchoolDatabase.setValue(schoolInfo).addOnCompleteListener {
            if (it.isSuccessful) {
                view?.let { it1 ->
                    createSuccessfulSnack(it1, "Изменения применены")
                }
                binding.applyChangesButton.text = "Применить"
                binding.applyChangesButton.isEnabled = true
                binding.applyChangesButton.setTextColor(resources.getColor(R.color.beige_main))
            } else {
                view?.let { it1 -> createErrorSnack(it1, it.exception.toString()) }
            }
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[SchoolSettingsViewModel::class.java]
    }

}