package com.zeroqore.mutualfundapp.ui.placeholder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.zeroqore.mutualfundapp.R // Make sure this import is correct

class PlaceholderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_placeholder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Optionally set text to show which fragment it is, useful for debugging
        view.findViewById<TextView>(R.id.placeholder_text)?.text = "This is the ${arguments?.getString("label") ?: ""}"
    }

    companion object {
        // Factory method to create a new instance with a label
        fun newInstance(label: String) =
            PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putString("label", label)
                }
            }
    }
}