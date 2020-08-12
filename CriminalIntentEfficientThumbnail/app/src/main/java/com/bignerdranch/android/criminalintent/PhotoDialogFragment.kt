package com.bignerdranch.android.criminalintent

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_photo_dialog.*
import java.io.File

private const val ARG_PHOTO_FILE = "photo_file"
private const val ARG_PHOTO_URI = "photo_uri"
class PhotoDialogFragment(): DialogFragment() {

    private lateinit var photoView: ImageView
    private lateinit var photoLayout: LinearLayout
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var viewTreeObserver: ViewTreeObserver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoFile = arguments?.getSerializable((ARG_PHOTO_FILE)) as File
        photoUri = Uri.parse((arguments?.getSerializable(ARG_PHOTO_URI) as String))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_dialog, container, false)
        photoView = view.findViewById(R.id.photo_view) as ImageView
        photoLayout = view.findViewById(R.id.photo_view_linear_layout)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (photoFile.exists()) {
            val bitmap =
                getScaledBitmap(
                    photoFile.path,
                    requireActivity(),
                    requireContext()
                )
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }


    }

    companion object {
        fun newInstance(photoFile: File, photoUri: Uri): PhotoDialogFragment {
            val args = Bundle().apply {
                putSerializable(ARG_PHOTO_FILE, photoFile)
                putSerializable(ARG_PHOTO_URI, photoUri.toString())
            }

            return PhotoDialogFragment().apply {
                arguments = args
            }
        }
    }
}