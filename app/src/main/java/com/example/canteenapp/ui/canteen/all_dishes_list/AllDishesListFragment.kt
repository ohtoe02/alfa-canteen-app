package com.example.canteenapp.ui.canteen.all_dishes_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentAllDishesListBinding
import com.example.canteenapp.ui.canteen.requests.RequestsFragment
import com.example.canteenapp.utils.adapters.CompactDishAdapter
import com.example.canteenapp.utils.models.DishData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AllDishesListFragment : Fragment() {
    private var _binding: FragmentAllDishesListBinding? = null

    private val binding get() = _binding!!

    private lateinit var databaseMenuReference: DatabaseReference
    private lateinit var dishAdapter: CompactDishAdapter
    private lateinit var dishList: MutableList<DishData>
    private var currentDishClass: Pair<String, String> = Pair("Основное блюдо", "mainDishes")

    private val dishClasses: Map<String, String> = mapOf(
        "Основное блюдо" to "mainDishes", "Второе блюдо" to "secondaryDishes",
        "Напитки" to "drinks"
    )

    companion object {
        fun newInstance() = AllDishesListFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllDishesListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

    }

    private fun init() {
        binding.hint.actionCategoryTitle.text = "Все блюда"
        binding.hint.actionTitle.text = "Столовая"

        databaseMenuReference =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/school1/menu")

        setListeners()
        setRecycleView()
        setupSpinner()
        getDishData()
    }

    private fun setRecycleView() {
        binding.dishesRecycler.setHasFixedSize(true)
        binding.dishesRecycler.layoutManager = LinearLayoutManager(context)

        dishList = mutableListOf()
        dishAdapter = CompactDishAdapter(dishList)

        binding.dishesRecycler.adapter = dishAdapter

        dishAdapter.setOnItemClickListener { _, _, position ->
            val bundle = Bundle()
            bundle.putSerializable("dish", dishList[position])
            bundle.putSerializable("dishClass", currentDishClass)

            findNavController().navigate(
                R.id.action_allDishesListFragment_to_editDishFragment, bundle
            )
        }
    }

    private fun setListeners() {
        binding.addDishButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_allDishesListFragment_to_addDishFragment
            )
        }
    }

    private fun setupSpinner() {
        val names = dishClasses.keys.toList()

        val spinnerAdapter = context?.let { ArrayAdapter(it, R.layout.custom_spinner_item, names) }
        spinnerAdapter?.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.dishClassPicker.adapter = spinnerAdapter
        binding.dishClassPicker.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentDishClass = Pair(
                    dishClasses.keys.elementAt(position),
                    dishClasses.values.elementAt(position)
                )
                getDishData()
            }
        }
    }

    private fun getDishData() {
        databaseMenuReference.child("dishes/${currentDishClass.second}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dishList.clear()
                    for (dishSnapshot in snapshot.children) {
                        val dish = dishSnapshot.key?.let {
                            val weight: String = dishSnapshot.child("weight").value.toString()
                            val title: String = dishSnapshot.child("title").value as String
                            val category: String = dishSnapshot.child("category").value as String
                            val price: String = dishSnapshot.child("price").value.toString()
                            val picture: String = dishSnapshot.child("picture").value as String

                            DishData(it, weight, title, category, price, picture)
                        }

                        if (dish != null) {
                            dishList.add(dish)
                        }
                    }

                    dishAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}