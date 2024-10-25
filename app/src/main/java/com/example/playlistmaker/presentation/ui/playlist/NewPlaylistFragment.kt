package com.example.playlistmaker.presentation.ui.playlist

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.example.playlistmaker.presentation.ui.main.MainActivity
import com.example.playlistmaker.presentation.viewmodel.NewPlaylistViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment() {
    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!
    private var coverUriSelect: Uri? = null
    private var showedDialog: Boolean = false
    private val vm by viewModel<NewPlaylistViewModel>()
    private lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setNavBarVis(false)

        vm.savedCoverUri.observe(viewLifecycleOwner) { savedUri ->
            coverUriSelect = savedUri
        }

        val chooseCover =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { previewUri ->
                if (previewUri != null) {
                    vm.saveImageToStorage(requireContext(), previewUri)
                    binding.newPlaylistCover.setImageURI(previewUri)
                    showedDialog = true
                }
            }

        binding.newPlaylistCover.setOnClickListener {
            chooseCover.launch(PickVisualMediaRequest((ActivityResultContracts.PickVisualMedia.ImageOnly)))
        }

        binding.createButton.setOnClickListener {
            newPlaylistAdd(coverUriSelect)
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_created),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigateUp()
        }

        binding.newPlaylistNameEditTxt.doOnTextChanged { text, _, _, _ ->
            if (text!!.isNotEmpty()) {
                val color = ContextCompat.getColor(requireContext(), R.color.yp_blue)
                showedDialog = true
                binding.createButton.isEnabled = true
                binding.createButton.setBackgroundColor(color)
            } else {
                val color = ContextCompat.getColor(requireContext(), R.color.yp_light_gray)
                binding.createButton.isEnabled = false
                binding.createButton.setBackgroundColor(color)
            }

        }

        binding.backButton.setOnClickListener {
            if (showedDialog) {
                showDialog()
            } else {
                parentFragmentManager.popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (showedDialog) {
                showDialog()
            } else {
                parentFragmentManager.popBackStack()
            }
        }

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (showedDialog) {
                    showDialog()
                } else {
                    (activity as? MainActivity)?.setNavBarVis(true)
                    findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun showDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.BaseDialogStyle)
            .setTitle(getString(R.string.stop_playlist_creation))
            .setMessage(getString(R.string.all_unsaved_data_will_be_lost))
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.exit)) { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .show()
    }

    private fun newPlaylistAdd(coverUri: Uri?) {
        val playlistName = binding.newPlaylistNameEditTxt.text.toString()
        val playlistDescription = binding.newPlaylistDescriptionEditTxt.text.toString()
        vm.createPlaylist(playlistName, playlistDescription, coverUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::callback.isInitialized) {
            callback.remove()
        }
    }
}