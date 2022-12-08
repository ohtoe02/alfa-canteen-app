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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canteenapp.LoginActivity
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

class SettingsFragment : Fragment(), AddDishDialogFragment.OnDialogNextBtnClickListener {

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

        showUserData()
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

        binding.addDishButton.setOnClickListener {
            if (frag != null) {
                childFragmentManager.beginTransaction().remove(frag!!).commit()
            }
            frag = AddDishDialogFragment()
            frag!!.setListener(this)

            frag!!.show(
                childFragmentManager,
                AddDishDialogFragment.TAG
            )
        }
    }

    private fun clearSharedPrefs() {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.clear()
        editor.apply()
    }

    private fun showUserData() {
        val roleTextView: TextView = binding.textRole
        roleTextView.text =
            String.format("Ваша роль: %s", sharedPreferences.getString("role", "not-found"))
    }

    private fun uploadImage(dishKey: DatabaseReference, imageUri: Uri?) {
        storageRef = storageRef.child(System.currentTimeMillis().toString())
        imageUri?.let {
            println(storageRef)
            storageRef.putFile(it).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        dishKey.child("picture").setValue(uri.toString())
                        Toast.makeText(context, "Uploaded", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT).show()

                }
            }
        }
    }



    override fun saveDish(dish: DishData?, dishEdit: FragmentAddDishDialogBinding, imageUri: Uri?, dishClass: String) {

        val dishKey = firebaseDatabase.child(dishClass).push()
        dish?.id = dishKey.key.toString()
        uploadImage(dishKey, imageUri)
        dishKey.setValue(dish).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task Added Successfully", Toast.LENGTH_SHORT).show()
                dishEdit.dishTitle.text = null
                dishEdit.dishCategory.text = null
                dishEdit.dishPrice.text = null
                dishEdit.dishWeight.text = null

            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        frag!!.dismiss()
    }

    override fun updateDish(dishData: DishData) {
        TODO("Not yet implemented")
    }
}