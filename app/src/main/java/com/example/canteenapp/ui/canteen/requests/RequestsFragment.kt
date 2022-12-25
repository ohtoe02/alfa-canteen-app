package com.example.canteenapp.ui.canteen.requests

import android.content.Context
import android.content.SharedPreferences
import android.databinding.tool.ext.capitalizeUS
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentAllRequestsBinding
import com.example.canteenapp.utils.adapters.RequestDishCardAdapter
import com.example.canteenapp.utils.models.DishCountData
import com.example.canteenapp.utils.models.DishData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RequestsFragment : Fragment() {
    private var _binding: FragmentAllRequestsBinding? = null

    private val binding get() = _binding!!

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var currentSchool: String? = null

    private lateinit var requestsDatabaseReference: DatabaseReference
    private lateinit var dishAdapter: RequestDishCardAdapter
    private lateinit var ordersList: MutableList<DishCountData>
    private lateinit var schoolMenu: HashMap<String, HashMap<String, HashMap<String, String>>>
    private var allDishes: MutableMap<String, MutableMap<String, DishCountData>> =
        mutableMapOf(
            "drink" to mutableMapOf(),
            "mainDish" to mutableMapOf(),
            "secondDish" to mutableMapOf()
        )

    private val dishClasses: Map<String, String> = mapOf(
        "Все блюда" to "allDishes", "Классы" to "classes"
    )

    private val convertClassNamings: Map<String, String> = mapOf(
        "mainDish" to "mainDishes", "secondDish" to "secondaryDishes",
        "drink" to "drinks"
    )

    companion object {
        fun newInstance() = RequestsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllRequestsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = this.requireActivity().getSharedPreferences(
            sharedPrefsId,
            Context.MODE_PRIVATE
        )
        currentSchool = sharedPreferences.getString("school", "")

        init()

    }

    private fun init() {
        binding.hint.actionCategoryTitle.text = "Столовая"
        binding.hint.actionTitle.text = "Заявки на питание"

        requestsDatabaseReference =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/school1/requests")

//        setListeners()
        setRecycleView()
        setupSpinner()
        getSchoolMenu()
    }

    private fun setRecycleView() {
        binding.dishesRecycler.setHasFixedSize(true)
        binding.dishesRecycler.layoutManager = LinearLayoutManager(context)

        ordersList = mutableListOf()
        dishAdapter = RequestDishCardAdapter(ordersList)

        binding.dishesRecycler.adapter = dishAdapter

//        dishAdapter.setOnItemClickListener { _, _, position ->
//            val bundle = Bundle()
//            bundle.putSerializable("dish", dishList[position])
//            bundle.putSerializable("dishClass", currentDishClass)
//
//            findNavController().navigate(
//                R.id.action_allDishesListFragment_to_editDishFragment, bundle
//            )
//        }
    }

    private fun setListeners() {
//        binding.submitRequestButton.setOnClickListener {
//            findNavController().navigate(
//                R.id.action_allDishesListFragment_to_addDishFragment
//            )
//        }
    }

    private fun setupSpinner() {
        val names = dishClasses.keys.toList()

        val spinnerAdapter = context?.let { ArrayAdapter(it, R.layout.custom_spinner_item, names) }
        spinnerAdapter?.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.dishClassPicker.adapter = spinnerAdapter
//        binding.dishClassPicker.onItemSelectedListener = object : OnItemSelectedListener {
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                currentDishClass = Pair(
//                    dishClasses.keys.elementAt(position),
//                    dishClasses.values.elementAt(position)
//                )
//                getDishData()
//            }
//        }
    }

    private fun getSchoolMenu() {
        val menuReference =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/$currentSchool/menu/dishes/")

        menuReference.get().addOnSuccessListener {
            if (it.exists()) {
                schoolMenu = it.value as HashMap<String, HashMap<String, HashMap<String, String>>>

                getDishData()
            }
        }
    }

    private fun getDishData() {
        requestsDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allDishes =
                    mutableMapOf(
                        "drink" to mutableMapOf(),
                        "mainDish" to mutableMapOf(),
                        "secondDish" to mutableMapOf()
                    )
                for (requestSnapshot in snapshot.children) {
                    val key = requestSnapshot.key!!
                    val requestData =
                        requestSnapshot.value as HashMap<String, HashMap<String, String>>
                    val dishes = requestData["dishes"]!!

                    dishes.forEach { (key, value) ->
                        run {
                            val dishMap =
                                schoolMenu[convertClassNamings[key]]?.get(value) ?: return@run

                            val weight = dishMap["weight"].toString()
                            val category = dishMap["category"]!!
                            val price = dishMap["price"].toString()
                            val title = dishMap["title"]!!.capitalize()
                            val picture = dishMap["picture"]!!

                            val dish = DishData(value, weight, title, category, price, picture)

                            val count = allDishes[key]?.get(value)?.count ?: 0
                            allDishes[key]?.set(value, DishCountData(dish, count + 1))
                        }
                    }

                }

                allDishes["mainDish"]?.values?.forEach {
                    ordersList.add(it)
                }
                allDishes["drink"]?.values?.forEach {
                    ordersList.add(it)
                }
                allDishes["secondDish"]?.values?.forEach {
                    ordersList.add(it)
                }

                ordersList.sortByDescending { it.count }
                println(ordersList)
                dishAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}