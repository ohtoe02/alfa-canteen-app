package com.example.canteenapp.ui.admin.classes

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canteenapp.R
import com.example.canteenapp.databinding.FragmentClassRequestsBinding
import com.example.canteenapp.databinding.FragmentClassesBinding
import com.example.canteenapp.utils.adapters.ClassAdapter
import com.example.canteenapp.utils.adapters.CompactDishAdapter
import com.example.canteenapp.utils.models.ClassData
import com.example.canteenapp.utils.models.DishData
import com.example.canteenapp.utils.models.KidData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ClassesFragment : Fragment() {

    companion object {
        fun newInstance() = ClassesFragment()
    }

    private var _binding: FragmentClassesBinding? = null

    private val binding get() = _binding!!

    private lateinit var databaseMenuReference: DatabaseReference
    private lateinit var classAdapter: ClassAdapter
    private lateinit var classList: MutableList<ClassData>

    private lateinit var viewModel: ClassesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[ClassesViewModel::class.java]
    }

    private fun init() {
        binding.hint.actionCategoryTitle.text = "Список классов"
        binding.hint.actionTitle.text = "Параметры школы"

        binding.backwardsArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        databaseMenuReference =
            Firebase.database("https://alfa-canteen-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("schools/school1/classes")

        setRecycleView()
        getClassesData()
    }

    private fun setRecycleView() {
        binding.classesRecycler.setHasFixedSize(true)
//        binding.classesRecycler.layoutManager = GridLayoutManager(context, 2)

        classList = mutableListOf()
        classAdapter = ClassAdapter(classList)

        binding.classesRecycler.adapter = classAdapter
    }

    private fun getClassesData() {
        databaseMenuReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    classList.clear()
                    for (dishSnapshot in snapshot.children) {
                        val classItem = dishSnapshot.key?.let {
                            val classId = it
                            val title: String = dishSnapshot.child("title").value as String
                            val headTeacher = dishSnapshot.child("headTeacher").value as String
                            val students = dishSnapshot.child("students").value as HashMap<String, String>
                            val studentsCount = students.keys.size

                            ClassData(classId, title, studentsCount, headTeacher, students)
                        }

                        if (classItem != null) {
                            classList.add(classItem)
                        }
                    }

                    classAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}