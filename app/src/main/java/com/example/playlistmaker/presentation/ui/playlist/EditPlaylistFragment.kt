package com.example.playlistmaker.presentation.ui.playlist

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.PlayList
import com.example.playlistmaker.presentation.ui.main.MainActivity
import com.example.playlistmaker.presentation.viewmodel.EditPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class EditPlaylistFragment : NewPlaylistFragment() {
    private val vm by viewModel<EditPlaylistViewModel>()
    private var playlist: PlayList? = null
    private var coverUriSelect: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.enableBottomPanel()

        playlist = arguments?.getParcelable("modify_playlist") as? PlayList

        val chooseCover =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { previewUri ->
                if (previewUri != null) {
                    vm.saveImageToStorage(requireContext(),previewUri)
                    binding.newPlaylistCover.setImageURI(previewUri)
                }
            }

        binding.topPanelText.text = getString(R.string.edit)

        binding.createButton.text = getString(R.string.save)

        binding.newPlaylistCover.setOnClickListener {
            chooseCover.launch(PickVisualMediaRequest((ActivityResultContracts.PickVisualMedia.ImageOnly)))
        }

        binding.topPanel.setOnClickListener() {
            findNavController().navigateUp()
        }

        vm.savedCoverUri.observe(viewLifecycleOwner, Observer { uri ->
            coverUriSelect = uri
        })

        vm.playlist.observe(viewLifecycleOwner) { playlist ->
            vm.getPlaylist(playlist)
        }

        binding.createButton.setOnClickListener {
            playlist.let { playlist ->
                modifyPlaylist(coverUriSelect, playlist!!)
                findNavController().popBackStack()
            }
        }

        playlist.let { playlist ->
            binding.newPlaylistNameEditTxt.setText(playlist?.name)
            binding.newPlaylistDescriptionEditTxt.setText(playlist?.description)
            if (!playlist?.imageUri.isNullOrEmpty()) {
                val imageUri = Uri.parse(playlist?.imageUri)
                coverUriSelect = imageUri
                binding.newPlaylistCover.setImageURI(imageUri)
            }
        }
    }

    private fun modifyPlaylist(coverUri: Uri?, originalPlayList: PlayList) {
        val name = binding.newPlaylistNameEditTxt.text.toString()
        val description = binding.newPlaylistDescriptionEditTxt.text.toString()
        val cover = vm.getCover().toString()
        vm.modifyData(name, description, cover, coverUri, originalPlayList)
    }
}