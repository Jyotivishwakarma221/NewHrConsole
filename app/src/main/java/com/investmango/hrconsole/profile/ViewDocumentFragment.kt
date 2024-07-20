package com.investmango.hrconsole.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.investmango.hrconsole.R
import com.investmango.hrconsole.databinding.FragmentDocumentUploadBinding
import com.investmango.hrconsole.databinding.FragmentViewDocumentBinding


class ViewDocumentFragment : Fragment() {
    private lateinit var binding: FragmentViewDocumentBinding

    var url: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.takeIf { it.containsKey("url") }?.apply {
            Log.e("arguments", "onViewCreated: " + getString("url"))

            url = getString("url").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_view_document, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext()).load(Uri.parse(url)).into(binding.docimage)
    }
}