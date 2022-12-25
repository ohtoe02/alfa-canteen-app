package com.example.canteenapp.ui.teacher.class_requests

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.canteenapp.databinding.FragmentClassRequestsBinding
import com.example.canteenapp.ui.canteen.all_dishes_list.AllDishesListFragment
import com.example.canteenapp.utils.adapters.ClassVisitAdapter
import com.example.canteenapp.utils.createSuccessfulSnack
import com.example.canteenapp.utils.models.KidData
import com.example.canteenapp.utils.models.KidVisitData
import com.example.canteenapp.utils.models.OrderData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class ClassRequestsFragment : Fragment() {
    private var _binding: FragmentClassRequestsBinding? = null

    private val binding get() = _binding!!

    private val sharedPrefsId: String = "sharedPrefs"
    private lateinit var sharedPreferences: SharedPreferences
    private var currentSchool: String? = null
    private var userLogin: String? = null
    private var classId: String? = null
    private var requests: HashMap<String, OrderData> = hashMapOf()

    private lateinit var classDatabaseReference: DatabaseReference
    private lateinit var schoolRequestsDatabaseReference: DatabaseReference
    private lateinit var kidAdapter: ClassVisitAdapter
    private lateinit var kidList: MutableList<KidVisitData>

    companion object {
        fun newInstance() = AllDishesListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassRequestsBinding.inflate(inflater, container, false)
        sharedPreferences = this.requireActivity().getSharedPreferences(
            sharedPrefsId,
            Context.MODE_PRIVATE
        )
        currentSchool = sharedPreferences.getString("school", "")
        userLogin = sharedPreferences.getString("login", "")
        classId = sharedPreferences.getString("classID", "")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        binding.hint.actionTitle.text = "Посещаемость"
        binding.hint.actionCategoryTitle.text = "7Б Класс"

        classDatabaseReference =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/$currentSchool/classes/$classId")

        schoolRequestsDatabaseReference =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/$currentSchool/requests")

        setListeners()
        setRecycleView()
        getKidsData()
        getRequestsData()
    }

    private fun setRecycleView() {
        kidList = mutableListOf()
        kidAdapter = context?.let { ClassVisitAdapter(kidList, it) }!!

        binding.checkboxList.adapter = kidAdapter
    }

    private fun setListeners() {
        binding.submitRequestButton.setOnClickListener {
            val attendingStudentsIds = mutableListOf<String>()
            kidList.forEach {
                if (!it.checked) {
                    attendingStudentsIds.add(it.kidData.id)
                }
            }
            val possibleRequests = hashMapOf<String, OrderData>()
            requests.forEach { (key, item) ->
                if (attendingStudentsIds.contains(item.info["kidId"])) {
                    possibleRequests[key] = item
                }
            }
            submitRequests(possibleRequests)
        }
    }

    private fun submitRequests(possibleRequests: HashMap<String, OrderData>) {
        val cleanRequests = hashMapOf<String, Any?>()
        possibleRequests.keys.forEach { cleanRequests[it] = null }

        classDatabaseReference.child("orderRequests").updateChildren(cleanRequests)

        schoolRequestsDatabaseReference.updateChildren(possibleRequests as Map<String, Any>).addOnCompleteListener {
            if (it.isSuccessful) {
                view?.let { it1 -> createSuccessfulSnack(it1, "Отметка сделана!") }
            }
        }
    }

    private fun getKidsData() {
        classDatabaseReference.child("students").get().addOnSuccessListener {
            if (it.exists()) {
                kidList.clear()
                val kids = it.value as HashMap<String, HashMap<String, String>>
                kids.values.forEach { value ->
                    value["id"]?.let { it1 ->
                        value["parent"]?.let { it2 ->
                            value["name"]?.let { it3 ->
                                KidData(
                                    it1, it3, it2
                                )
                            }
                        }
                    }?.let { it2 -> KidVisitData(it2, false) }?.let { it3 ->
                        run {
                            kidList.add(it3)
                        }
                    }
                }
            }
            binding.childrenCounter.text =
                String.format("В школе %s из %s детей", kidList.size, kidList.size)
            kidAdapter.notifyDataSetChanged()
        }
    }

    private fun getRequestsData() {
        classDatabaseReference.child("orderRequests").get().addOnSuccessListener {
            if (it.exists()) {
                requests.clear()
                val reqs = it.value as HashMap<String, HashMap<String, String>>
                reqs.forEach { (key, item) ->
                    val dishes = item["dishes"] as HashMap<String, String?>
                    val info = item["info"] as HashMap<String, String>
                    info["class"] = classId as String
                    info["teacher"] = userLogin as String
                    requests[key] = OrderData(dishes, info)
                }
            }
        }
        binding.childrenCounter.text =
            String.format("В школе %s из %s детей", kidList.size, kidList.size)
        kidAdapter.notifyDataSetChanged()
    }
}