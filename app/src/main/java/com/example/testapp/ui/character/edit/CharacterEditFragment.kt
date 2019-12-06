package com.example.testapp.ui.character.edit


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.testapp.R
import com.example.testapp.db.entity.Character
import com.example.testapp.ui.character.observe.CharacterFragmentViewModel
import kotlinx.android.synthetic.main.fragment_character_edit.*
import toothpick.Toothpick
import toothpick.ktp.delegate.inject
import toothpick.smoothie.viewmodel.installViewModelBinding

class CharacterEditFragment : Fragment() {

    private var character: Character = Character()

    private var mode: String = "update"//need enum?

    private val viewModel: CharacterEditFragmentViewModel by inject()

    private val navController: NavController?
        get() = activity?.let { Navigation.findNavController(it, R.id.nav_host_fragment) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_character_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scope = Toothpick.openScope("APP")
        scope.installViewModelBinding<CharacterEditFragmentViewModel>(this)
        scope.inject(this)

        mode = arguments?.getString("mode", "update") ?: "update"
        if (mode == "update"){
            val id = arguments?.getInt("id", 0) ?: 0
            viewModel.getCharacterById(id)
            observeCharacterById()
            observeUpdateComplete()
        } else {
            observeAddComplete()
        }

        observeErrors()

        initOnClick()
    }

    private fun initOnClick()
    {
        button_cancel.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("id", character.id)
            val optionsBuilder = NavOptions.Builder()
            val options = optionsBuilder.setPopUpTo(R.id.startFragment, false).build()
            navController?.navigate(R.id.characterFragment, bundle, options)
        }

        button_accept.setOnClickListener {
            if (mode == "update") {
                onClickUpdate()
                val bundle = Bundle()
                bundle.putInt("id", character.id)
                navController?.navigate(R.id.characterFragment, bundle)
            } else {
                onClickAdd()
                navController?.navigateUp()
            }
        }
    }

    private fun observeCharacterById()
    {
        viewModel.characterById.observe(this, Observer {
            character = it
            setDataInFields(it)
        })
    }

    private fun observeUpdateComplete() {
        viewModel.updateComplete.observe(this, Observer {
            Toast.makeText(activity, "updated", Toast.LENGTH_SHORT).show()
        })
    }
    private fun observeAddComplete() {
        viewModel.addComplete.observe(this, Observer {
            Toast.makeText(activity, "added", Toast.LENGTH_SHORT).show()
        })
    }

    private fun observeErrors() {
        viewModel.error.observe(this, Observer {
            Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show()
            println("ERROR!!! $it")
        })
    }

    private fun onClickAdd() {
        viewModel.addCharacter(getCharacterFromFields())
    }

    private fun onClickUpdate() {
        viewModel.updateCharacter(getCharacterFromFields())
    }

    private fun setDataInFields(ch: Character) {
        textView_id.text = ch.id.toString()
        textView_name.setText(ch.name)
        textView_st.setText(ch.st.toString())
        textView_dx.setText(ch.dx.toString())
        textView_iq.setText(ch.iq.toString())
        textView_ht.setText(ch.ht.toString())
    }

    private fun getCharacterFromFields(): Character{
        return character.apply {
            name = textView_name.text.toString()
            st = textView_st.text.toString().toInt()
            dx = textView_dx.text.toString().toInt()
            iq = textView_iq.text.toString().toInt()
            ht = textView_ht.text.toString().toInt()
        }
    }
}