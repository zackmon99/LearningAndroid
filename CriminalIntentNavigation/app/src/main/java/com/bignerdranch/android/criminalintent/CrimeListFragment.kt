package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.*



private const val TAG = "CrimeListFragment"

class CrimeListFragment: Fragment() {

    /**
     * Required interface for hosting activities
     */

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null


    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        val factory = CrimeListViewModelFactory()
        ViewModelProvider(this@CrimeListFragment, factory).get(CrimeListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach called")
        // set the context (Which is the main activity, activity extends Context
        // to this variable as Callbacks so the variable will know about the
        // onCrimeSelected() function
        callbacks = context as Callbacks?
    }

    // newInstance() function declared here
    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateViewCalled")
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false) as RecyclerView

        // Set up RecyclerView with empty crime list
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        // We are creating an observer here to note if crimesListLiveData has changed,
        // if it has, call updateUI
        // But why should it change? We are only setting crimeListLiveData once!
        // Because we are running a query on the background
        // thread to get the list of crimes, so the main thread might get passed
        // this point before the query is done.  So now if crimesListLiveData
        // changes, updateUI is called!
        // So, as soon as the query is finished, updateUI is called.
        crimeListViewModel.crimesListLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            })
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach called")
        super.onDetach()
        // Null out callbacks
        callbacks = null
    }

    private fun updateUI(crimes: List<Crime>) {
        adapter = CrimeAdapter(crimes)
        // changing the adapter will update the RecyclerView
        crimeRecyclerView.adapter = adapter
    }

    // Crime holder is the UI element of a single item in RecyclerView
    // Set up the Views here!
    private inner class CrimeHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        // Set an onClick listener on the CrimeHolder
        init {
            // itemView is declared in RecyclerView.ViewHolder
            itemView.setOnClickListener(this)
        }

        // bind sets the values in the CrimeHolder
        fun bind(crime: Crime) {
            this.crime = crime

            titleTextView.text = this.crime.title
            val dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            dateTextView.text = dateFormat.format(this.crime.date)
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        // When it is click, call onCrimeSelected on MainActivity.  We set
        // callbacks? to the context object from onAttach(), which IS the
        // MainActivity (Activity extends context)
        // We do this so this onClick listener can call do things to the MainActivity
        // in this case, change the fragment (see MainActivity.onCrimeSelected()
        override fun onClick(v: View?) {
            //callbacks?.onCrimeSelected(crime.id)
            val action = CrimeListFragmentDirections.actionNavigationCrimeListToNavigationCrime(crime.id)
            findNavController().navigate(action)
        }
    }

    // This is the adapter to the RecyclerView.  This conducts the CrimeHolders
    private inner class CrimeAdapter(var crimes: List<Crime>) : RecyclerView.Adapter<CrimeHolder>() {

        // Need to override this function. This creates the crimeHolder and returns it
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            // inflate the viewholder and set it as view variable
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)

            // return the CrimeHolder with the view passed in
            return CrimeHolder(view)
        }

        // Need this function to know how many CrimeHolders there will be
        override fun getItemCount() = crimes.size

        // Need this function to set the data at each position of the
        // RecyclerView
        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

    }
}