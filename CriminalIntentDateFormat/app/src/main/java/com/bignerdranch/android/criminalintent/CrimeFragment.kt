package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.sql.Time
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks {

    // Create variables
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckbox: CheckBox
    private lateinit var timeButton: Button

    // Get a new or existing ViewModel that will persist
    // after fragment is destroyed
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        val factory = CrimeDetailViewModelFactory()
        ViewModelProvider(this@CrimeFragment, factory).get(CrimeDetailViewModel::class.java)
    }

    // This is the first thing that happens when creating fragment.
    // we can do anything related to just the Bundle here.  We can also
    // do any other setup that does not need a container or inflater here
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        // Get value from the fragment arguments
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        Log.d(TAG, "args bundle crime ID: $crimeId")

        // Use the loadCrime function change the crime in the view model
        crimeDetailViewModel.loadCrime(crimeId)
    }

    // We set up the UI here
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        // inflate the view
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        // set the various components to the elements in the layout
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckbox = view.findViewById(R.id.crime_solved) as CheckBox
        timeButton = view.findViewById(R.id.crime_time) as Button

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreatedCalled")
        // Now that view is created, set up an observer that listens
        // for changes to crimeDetailView.crimeLiveData.  If there is a change
        // to crimeLiveData, run the lambda function
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { crime ->
                crime?.let {
                    Log.i(TAG, "crimeLiveData observed")
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    // Last thing called when creating fragment.  Good place to add listeners to
    // view elements
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart Called")
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = sequence.toString()
                Log.d(TAG, "titleWatcher detects change, changing crime.title")
            }

            override fun afterTextChanged(sequence: Editable?) {
            }
        }

        // Add titleWatcher to titleField so we detect changes
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckbox.apply{
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireActivity().supportFragmentManager, DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireActivity().supportFragmentManager, DIALOG_TIME)
            }
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop called")
        super.onStop()
        // When we destroy the view, save any changes to the crime to the database
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    // Code for updating the text fields and text box
    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckbox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
    }

    // newInstance() with crimeId passed in declared here
    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                // put the crimeId (UUID object, so serializable) the args variable
                // by using the putSerializable function in Bundle
                putSerializable(ARG_CRIME_ID, crimeId)
            }

            // Then return a new instance of the fragment with it's argument
            // member variable set to args
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}