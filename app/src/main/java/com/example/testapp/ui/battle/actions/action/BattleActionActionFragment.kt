package com.example.testapp.ui.battle.actions.action

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.example.testapp.R
import toothpick.Toothpick
import toothpick.ktp.delegate.inject
import toothpick.smoothie.viewmodel.installViewModelBinding

class BattleActionActionFragment : DialogFragment() {

    private val viewModel: BattleActionActionFragmentViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setTitle(R.string.action)
        return inflater.inflate(R.layout.fragment_battle_action_action, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val scope = Toothpick.openScope("APP")
        scope.installViewModelBinding<BattleActionActionFragmentViewModel>(this)
        scope.inject(this)

        viewModel.clearEvents()

        observeErrors()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.forceClear()
    }

    private fun initOnClick() {

    }

    private fun observeErrors() {
        viewModel.error.observe(this, Observer {
            Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show()
            println("ERROR!!! $it")
        })
    }
}
