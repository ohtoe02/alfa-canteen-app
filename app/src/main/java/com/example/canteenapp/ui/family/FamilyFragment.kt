package com.example.canteenapp.ui.family

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentFamilyBinding
import com.example.canteenapp.utils.adapters.KidsListAdapter
import com.example.canteenapp.utils.betterNavigate
import com.example.canteenapp.utils.models.KidData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FamilyFragment : Fragment(), KidsListAdapter.KidsAdapterClickInterface {

    private var _binding: FragmentFamilyBinding? = null

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var currentSchool: String? = null
    private var userLogin: String? = null
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var kidsListAdapter: KidsListAdapter
    private lateinit var kidList: MutableList<KidData>


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
        findNavController().backQueue.forEach { item -> println("${item.destination.label} ///") }


        return root
    }

    private fun init() {
        sharedPreferences = this.requireActivity().getSharedPreferences(
            sharedPrefsId,
            Context.MODE_PRIVATE
        )
        currentSchool = sharedPreferences.getString("school", "")
        userLogin = sharedPreferences.getString("login", "")

        val familyHintContainer = binding.familyHint
        familyHintContainer.actionTitle.text = "Моя семья"
        familyHintContainer.actionCategoryTitle.text =
            sharedPreferences.getString("familySurname", "Дети")

        binding.goToSettingsButton.root.text = "Мои заявки"
        binding.addChildButton.root.text = "Добавить ребенка"

        firebaseDatabase =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/$currentSchool/users/$userLogin")

        binding.goToSettingsButton.root.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_family_to_myRequestsFragment)
        }

        setRecycleView()
        getKidsFromFirebase()
    }

    private fun getKidsFromFirebase() {
        firebaseDatabase.child("family/kids").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kidList.clear()
                for (kidSnapshot in snapshot.children) {
                    val kid = kidSnapshot.key?.let {
                        val name: String = kidSnapshot.value as String

                        KidData(it, name, userLogin!!)
                    }

                    if (kid != null) {
                        kidList.add(kid)
                    }
                }

                kidsListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setRecycleView() {
        binding.kidsRecycler.setHasFixedSize(true)
        binding.kidsRecycler.layoutManager = LinearLayoutManager(context)

        kidList = mutableListOf()
        kidsListAdapter = KidsListAdapter(kidList)
        kidsListAdapter.setOnItemLongClickListener { adapter, view, position ->
            Toast.makeText(context, adapter.getItem(position)?.name, Toast.LENGTH_SHORT).show()
            true
        }
        kidsListAdapter.setListener(this)

        binding.kidsRecycler.adapter = kidsListAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAddToOrderClicked(kidData: KidData) {
        val bundle = Bundle()
        bundle.putSerializable("currentKid", kidData)
        bundle.putSerializable("currentCart", null)

//        findNavController().popBackStack()
//        betterNavigate(findNavController(), R.id.goToOrder, bundle)
        findNavController().navigate(R.id.goToOrder, bundle)
    }

}

