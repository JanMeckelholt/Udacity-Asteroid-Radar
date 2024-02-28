package com.udacity.asteroidradar.detail


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {

    private val viewModel: DetailViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(this, DetailViewModel.Factory(activity.application, args.selectedAsteroid)).get(DetailViewModel::class.java)
    }

    private val args: DetailFragmentArgs by navArgs()
    private lateinit var binding: FragmentDetailBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater)
        binding.lifecycleOwner = this

        val asteroid = args.selectedAsteroid

        binding.asteroid = asteroid
        binding.viewModel = viewModel

        binding.helpButton.setOnClickListener {
            displayAstronomicalUnitExplanationDialog()
        }

        viewModel.isSaved.observe(viewLifecycleOwner, Observer {
            setSaveButtonEnabled(it)
        })

        viewModel.requestConfirmDelete.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                showConfirmDeleteDialog(asteroid)
            }
        })

        return binding.root
    }

    private fun displayAstronomicalUnitExplanationDialog() {
        val builder = AlertDialog.Builder(requireActivity())
            .setMessage(getString(R.string.astronomica_unit_explanation))
            .setPositiveButton(android.R.string.ok, null)
        builder.create().show()
    }

    private fun setSaveButtonEnabled(enabled: Boolean?) {
        if (enabled == null) {
            binding.btnSaveAsteroid.visibility = View.GONE
            binding.tvSavedStatus.visibility = View.INVISIBLE
        } else {
            binding.btnSaveAsteroid.visibility = View.VISIBLE
            if (enabled) {
                binding.btnSaveAsteroid.setImageResource(R.drawable.baseline_delete_24)
                binding.tvSavedStatus.visibility = View.VISIBLE
            } else {
                binding.btnSaveAsteroid.setImageResource(R.drawable.baseline_save_24)
                binding.tvSavedStatus.visibility = View.INVISIBLE
            }
        }
    }

    private fun showConfirmDeleteDialog(asteroid: Asteroid) {
        android.app.AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.are_you_sure, asteroid.codename))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                viewModel.requestDelete()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                viewModel.cancelDelete()
                dialog.dismiss()
            }
            .show()
    }
}
