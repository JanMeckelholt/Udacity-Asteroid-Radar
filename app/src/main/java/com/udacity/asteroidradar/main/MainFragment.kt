package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import timber.log.Timber

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(this, MainViewModel.Factory(activity.application)).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.asteroidRecycler.adapter = AsteroidListAdatper(AsteroidListAdatper.OnClickListener {
            viewModel.displayAsteroidDetails(it)
        })
        viewModel.navigateToSelectedAsteroid.observe(viewLifecycleOwner, Observer {
            it?.let {
                val bundle = Bundle()
                bundle.putParcelable("selectedAsteroid", it)
                Timber.i("selectedAsteroid name ${it.codename}")
                this.findNavController().navigate(R.id.action_showDetail, bundle)
                viewModel.navigationDone()
            }
        })
        viewModel.status.observe(viewLifecycleOwner, Observer {
            if (it == AsteroidApiStatus.LOADING) {
                binding.statusLoadingWheel.visibility = View.VISIBLE
            } else {
                binding.statusLoadingWheel.visibility = View.GONE
            }

        })
        val menuHost : MenuHost = requireActivity()
        menuHost.addMenuProvider(OverflowMenu(viewModel), viewLifecycleOwner, Lifecycle.State.RESUMED)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return true
    }

    class OverflowMenu(private val viewModel: MainViewModel) : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.main_overflow_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            viewModel.filterAsteroids(menuItem.itemId)
            return true
        }
    }
}
