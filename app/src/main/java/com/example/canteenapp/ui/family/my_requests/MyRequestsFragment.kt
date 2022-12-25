package com.example.canteenapp.ui.family.my_requests

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentAllRequestsBinding
import com.example.canteenapp.databinding.FragmentMyRequestsBinding
import com.example.canteenapp.utils.adapters.MyRequestsAdapter
import com.example.canteenapp.utils.adapters.RequestDishCardAdapter
import com.example.canteenapp.utils.models.CategoryData
import com.example.canteenapp.utils.models.DishCountData
import com.example.canteenapp.utils.models.MyRequestData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyRequestsFragment : Fragment() {

    companion object {
        fun newInstance() = MyRequestsFragment()
    }

    private var _binding: FragmentMyRequestsBinding? = null

    private val binding get() = _binding!!

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var currentSchool: String? = null
    private var userLogin: String? = null
    private lateinit var viewModel: MyRequestsViewModel
    private lateinit var firebaseDatabase: DatabaseReference


    private lateinit var requestsList: MutableList<MyRequestData>
    private lateinit var requestsAdapter: MyRequestsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRequestsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = this.requireActivity().getSharedPreferences(
            sharedPrefsId,
            Context.MODE_PRIVATE
        )
        currentSchool = sharedPreferences.getString("school", "")
        userLogin = sharedPreferences.getString("login", "")

        init()
    }

    private fun init() {
        binding.hint.actionTitle.text = "Моя семья"
        binding.hint.actionCategoryTitle.text = "Заявки"

        firebaseDatabase =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/$currentSchool/users/$userLogin/requests")

        binding.goBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setRecycleView()
        getMyRequests()
    }

    private fun setRecycleView() {
        binding.requestsRecycler.setHasFixedSize(true)
        binding.requestsRecycler.layoutManager = LinearLayoutManager(context)

        requestsList = mutableListOf()
        requestsAdapter = MyRequestsAdapter(requestsList)

        binding.requestsRecycler.adapter = requestsAdapter
    }

    private fun getMyRequests() {
        firebaseDatabase
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    requestsList.clear()
                    for (requestSnapshot in snapshot.children) {
                        val request = requestSnapshot.key?.let {
                            val dishes =
                                requestSnapshot.child("dishes").value as HashMap<String, HashMap<String, String>>
                            val info: HashMap<String, String> =
                                requestSnapshot.child("info").value as HashMap<String, String>

                            val dishesMap: HashMap<String, Pair<String?, String?>> = hashMapOf()

                            dishes.forEach { (key, value) ->
                                run {
                                    dishesMap[key] = Pair(value["first"], value["second"])

                                }
                            }

                            MyRequestData(it, dishesMap, info)
                        }

                        if (request != null) {
                            requestsList.add(request)
                        }
                    }

                    requestsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[MyRequestsViewModel::class.java]
    }

}