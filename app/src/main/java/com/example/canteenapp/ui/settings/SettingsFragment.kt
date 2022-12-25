package com.example.canteenapp.ui.settings

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavigatorProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canteenapp.LoginActivity
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentAddDishDialogBinding
import com.example.canteenapp.databinding.FragmentSettingsBinding
import com.example.canteenapp.ui.dialogs.AddDishDialogFragment
import com.example.canteenapp.utils.adapters.SettingsAdapter
import com.example.canteenapp.utils.models.DishData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var frag: AddDishDialogFragment? = null

    private var currentSchool: String? = null
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseDatabase: DatabaseReference

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        findNavController().backQueue.forEach {item -> println("${item.destination.label} ///")}


        binding.settingsHint.actionTitle.text = "Настройки"
        binding.settingsHint.actionCategoryTitle.text = "и информация"

        binding.logoutButton.setOnClickListener {
            clearSharedPrefs()
            Toast.makeText(context, "Successfully logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

//        showUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init(view: View) {
        sharedPreferences = this.requireActivity().getSharedPreferences(sharedPrefsId, MODE_PRIVATE)
        currentSchool = sharedPreferences.getString("school", "")
        storageRef = FirebaseStorage.getInstance("gs://alfa-canteen.appspot.com").reference.child("Images/$currentSchool")
        firebaseDatabase = Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("schools/$currentSchool/menu/dishes")


        val settingsList = listOf("Основные", "Уведомления", "Помощь", "О приложении")

        val settingsAdapter = SettingsAdapter(settingsList as MutableList<String>)

        binding.settingsButtons.setHasFixedSize(true)
        binding.settingsButtons.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.settingsButtons.adapter = settingsAdapter

        binding.submitRequestButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_navigation_settings_to_addDishFragment
            )
        }

        if (sharedPreferences.getString("role", "") != "admin" && sharedPreferences.getString("role", "") != "cook") {
            binding.submitRequestButton.visibility = View.INVISIBLE
        }
    }

    private fun clearSharedPrefs() {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.clear()
        editor.apply()
    }

//    private fun showUserData() {
//        val roleTextView: TextView = binding.textRole
//        roleTextView.text =
//            String.format("Ваша роль: %s", sharedPreferences.getString("role", "not-found"))
//    }
}