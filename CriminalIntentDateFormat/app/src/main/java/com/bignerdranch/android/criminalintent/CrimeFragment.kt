package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.media.ExifInterface
import android.provider.MediaStore
import android.view.ViewTreeObserver
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.sql.Time
import java.util.*


private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 2
private const val REQUEST_PERMISSION_CODE = 3
private const val REQUEST_PHOTO = 4
private const val SHOW_LARGE_PHOTO = "LargePhoto"

private const val DATE_FORMAT = "EEE, MMM, dd"


class CrimeFragment : Fragment(), DatePickerFragment.Callbacks {

    // Create variables
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckbox: CheckBox
    private lateinit var timeButton: Button
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var callSuspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView




    // Get a new or existing ViewModel that will persist
    // after fragment is destroyed
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        Log.d(TAG, "TEST!!!!!!!!!")
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
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        callSuspectButton = view.findViewById(R.id.call_suspect) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView

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
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        photoFile)
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

        // Here we are starting an activity with this button.  We are saying we
        // want to do an ACTION_SEND and letting OS show the options
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)
                )
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        // setting up suspect button
        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK,
            ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            // Disable this button if device does not have a way to pick a contact
            // pickContactIntent.addCategory(Intent.CATEGORY_HOME)
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                 PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                //isEnabled = false
            }
        }

        callSuspectButton.apply {
            setOnClickListener {
                // Use REQUEST_PERMISSION_CODE here so we can do the action
                // in onRequestPermissionResult below.
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    REQUEST_PERMISSION_CODE
                )
            }
            if (crime.suspectPhoneNumber == "") {
                //isEnabled = false
            }
        }

        // Set up intent for taking photos
        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager

            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // disable button if no ACTION_IMAGE_CAPTURE program
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                // place for full res image to be stored
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                // find all photo taking apps
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY)

                // Grant permission to every app in list
                for(cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        photoView.setOnClickListener {
            val fragment = PhotoDialogFragment.newInstance(photoFile, photoUri)
            val activity = context as FragmentActivity
            val manager = activity.supportFragmentManager
            fragment.show(manager, SHOW_LARGE_PHOTO)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // See if it is the code for Calling the suspect
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (permissions[0] == Manifest.permission.READ_CONTACTS &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                val cursor = requireActivity().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + crime.suspectPhoneNumber,
                    null,
                    null
                )
                cursor?.use {
                    // If there is no data here, the contact has no phone numbers
                    if (it.count == 0)
                    {
                        // Build a simple alert
                        val dialogBuilder = AlertDialog.Builder(context)
                        dialogBuilder.apply{
                            setMessage("There is no phone number for" +
                                " this contact")
                            setCancelable(false)
                            setPositiveButton("Ok", DialogInterface.OnClickListener{
                                dialog, _ -> dialog.cancel()
                            })
                        }.run {
                            create()
                            show()
                        }
                        return
                    }

                    it.moveToFirst()
                    var phoneNumber = it?.getString(it.getColumnIndex(ContactsContract
                        .CommonDataKinds.Phone.NUMBER)) ?: ""
                    phoneNumber = phoneNumber.removePrefix("dat=")
                    phoneNumber = "tel:${phoneNumber}"
                    var hasPhoneNumber = it?.getString(it.getColumnIndex(ContactsContract.
                        CommonDataKinds.Phone.HAS_PHONE_NUMBER))
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber))

                    startActivity(intent)
                }

            }
        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop called")
        super.onStop()
        // When we destroy the view, save any changes to the crime to the database
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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

        // update suspect button text if it is not blank
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }

        updatePhotoView()

    }

    private fun updatePhotoView() {
            if (photoFile.exists()) {
                val bitmap = getScaledBitmap(photoFile.path, requireActivity(), requireContext())
                photoView.setImageBitmap(bitmap)
            } else {
                photoView.setImageDrawable(null)
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri = data.data ?: return
                // Specify which fields you want query to return values for
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID)
                // Perform query. The contactURI is like a where clause here
                val cursor = requireActivity().contentResolver
                    .query(contactUri, queryFields, null, null, null)
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }

                    // Pull out the first column of the first row of data
                    // That's the name
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect

                    val id = it.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))

                    crime.suspectPhoneNumber = id


                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }

            requestCode == REQUEST_PHOTO -> {
                updatePhotoView()
            }
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        }
        else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
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