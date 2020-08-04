package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
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
    private lateinit var noContentLayout: LinearLayout
    private lateinit var noContentTextView: TextView
    private lateinit var addCrimeNoContentButton: Button

    // I propose that this is a better way of setting up the observer because setting the observer
    // up in onViewCreated procs a change twice.  Once when onViewCreated is called for the initial
    // call and once again after the data is updated.  Slight performance increase this way....
    // HOWEVER, this observer isn't destroyed automatically when the view is destroyed because it is
    // not linked to a lifecycle!  So this observer will always exist until it is explicitly
    // removed.  I think this is fine, though as long as the recycler exists
    /*
    private val observer = Observer<List<Crime>> { crimes ->
        crimes?.let {
            Log.d(TAG, "Observer called!")
            if (crimeRecyclerView != null) {
                updateUI(crimes)
            }
        }
    }

     */

    private val crimeListViewModel: CrimeListViewModel by lazy {
        Log.d(TAG, "crimeListViewModel by Lazy")
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

        // if running the way I propose above, we set up the observer onAttach, so it is only set up
        // once
        //crimeListViewModel.crimesListLiveData.observeForever(observer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Need to let FragmentManager know that we need to call
        // onCreateOptionsMenu
        setHasOptionsMenu(true)
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
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false) as FrameLayout

        // Set up RecyclerView with empty crime list
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        noContentLayout = view.findViewById(R.id.no_content_layout)
        noContentTextView = view.findViewById(R.id.no_content_text_view)
        addCrimeNoContentButton = view.findViewById(R.id.add_crime_no_content_button)

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
        // Unfortunately, since this is running in onViewCreated, it will always be ran once when
        // switching to this fragment, whether or not the data has changed.  Meaning it is ran TWICE
        // when the data is actually changed.  I'm not a fan, but tying this to the fragment
        // lifecycle means the observer is not running when the fragment is destroyed, which is
        // nice.  Plus, the UI is not updated by virtue of using a ListAdapter as the adapter to
        // recyclerview.  This will still have less performance, but probably not noticeable.
        crimeListViewModel.crimesListLiveData.observe(
                viewLifecycleOwner,
        androidx.lifecycle.Observer { crimes ->
            crimes?.let {
                Log.i(TAG, "Observer CrimesList called")
                updateUI(crimes)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        addCrimeNoContentButton.setOnClickListener {
            val crime = Crime()
            crimeListViewModel.addCrime(crime)
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach called")
        super.onDetach()
        // Null out callbacks
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // populate the menu with items in fragment_crime_list menu
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(crimes: List<Crime>) {
        adapter?.submitList(crimes)
        if (crimes.isEmpty()) {
            crimeRecyclerView.visibility = View.GONE
            noContentLayout.visibility = View.VISIBLE
        }
        else {
            crimeRecyclerView.visibility = View.VISIBLE
            noContentLayout.visibility = View.GONE
        }
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
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    // This is the adapter to the RecyclerView.  This conducts the CrimeHolders
    private inner class CrimeAdapter(var crimes: List<Crime>) : ListAdapter<Crime, CrimeHolder>(CrimeItemDiffCallback()) {

        // Need to override this function. This creates the crimeHolder and returns it
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            // inflate the viewholder and set it as view variable
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)

            // return the CrimeHolder with the view passed in
            return CrimeHolder(view)
        }

        // Need this function to know how many CrimeHolders there will be
        //override fun getItemCount() = crimes.size

        // Need this function to set the data at each position of the
        // RecyclerView
        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            holder.bind(getItem(position))
        }

    }

    private inner class CrimeItemDiffCallback: DiffUtil.ItemCallback<Crime?>() {
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }
    }
}