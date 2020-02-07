package com.example.testapp.ui.character.observe


import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Outline
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testapp.R
import com.example.testapp.custom_view.outline_corner.OutlineProviders
import com.example.testapp.db.entity.Character
import com.example.testapp.db.entity.CharacterSkills
import com.example.testapp.db.entity.Skill.Skill
import com.example.testapp.ui.SelectableData
import com.example.testapp.ui.skill.SkillItem
import com.example.testapp.ui.skill.observe.single.SkillObserveSingleFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.card_character_all.*
import kotlinx.android.synthetic.main.fragment_character.*
import kotlinx.android.synthetic.main.item_page_test.*
import toothpick.Toothpick
import toothpick.ktp.delegate.inject
import toothpick.smoothie.viewmodel.installViewModelBinding

class CharacterFragment : Fragment() {

    private lateinit var character: Character
    private lateinit var characterSkills: List<CharacterSkills>
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private var isInfoCollapsed = false

    private val viewModel: CharacterFragmentViewModel by inject()

    private val navController: NavController?
        get() = activity?.let { Navigation.findNavController(it, R.id.nav_host_fragment) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_character, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scope = Toothpick.openScope("APP")
        scope.installViewModelBinding<CharacterFragmentViewModel>(this)
        scope.inject(this)

        viewModel.clearEvents()

        character_card_pager.adapter = ViewPagerAdapter()
        character_card_pager.offscreenPageLimit = 4

        val radius = 32f
        character_card_pager.outlineProvider = OutlineProviders(radius, OutlineProviders.OutlineType.ROUND_RECT_TOP)
        character_card_pager.clipToOutline = true

        recyclerViewInit()

        observeCharacterById()
        observeErrors()
        observeDeleteComplete()
        observeCharacterSkillsById()
        observeSkillByName()
        observeSkillByNames()

        initOnClick()

        val id = arguments?.getInt("id", 0) ?: 0
        viewModel.getCharacterById(id)
        viewModel.getCharacterSkillsById(id)
    }

    private fun initOnClick() {
        button_edit.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("id", character.id)
            navController?.navigate(R.id.action_characterFragment_to_characterEditFragment, bundle)
        }

        haracter_card_collapse_info.setOnClickListener {
            if (isInfoCollapsed) {
                isInfoCollapsed = false
                character_card_tl.visibility = View.VISIBLE//todo move in class CharacterCardView
                character_card_age.visibility = View.VISIBLE
                character_card_eye.visibility = View.VISIBLE
                character_card_hairs.visibility = View.VISIBLE
                character_card_skin.visibility = View.VISIBLE
                character_card_height.visibility = View.VISIBLE
                character_card_weight.visibility = View.VISIBLE
                character_card_gender.visibility = View.VISIBLE
                character_card_race.visibility = View.VISIBLE
                character_card_sm.visibility = View.VISIBLE
            } else {
                isInfoCollapsed = true
                character_card_tl.visibility = View.GONE
                character_card_age.visibility = View.GONE
                character_card_eye.visibility = View.GONE
                character_card_hairs.visibility = View.GONE
                character_card_skin.visibility = View.GONE
                character_card_height.visibility = View.GONE
                character_card_weight.visibility = View.GONE
                character_card_gender.visibility = View.GONE
                character_card_race.visibility = View.GONE
                character_card_sm.visibility = View.GONE
            }
        }
    }

    private fun recyclerViewInit() {
        groupAdapter.setOnItemClickListener { item, view ->
            val selectSkillDialog = SkillObserveSingleFragment((item as SkillItem).skill.data)
            selectSkillDialog.setTargetFragment(this, 1)
            selectSkillDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogFragmentStyle)
            selectSkillDialog.show(fragmentManager!!, null)
        }

        (character_card_pager.adapter as ViewPagerAdapter).groupAdapter = groupAdapter
    }

    private fun setItems(items: List<Skill>)
    {
        groupAdapter.clear()

        for(i in items)
        {
            groupAdapter.apply {
                add(
                    SkillItem(
                        skill = SelectableData(i),
                        colorActive = ContextCompat.getColor(context!!, R.color.accent),
                        colorInactive = ContextCompat.getColor(context!!, R.color.primary_light)
                    )
                )
            }
        }
    }

    private fun observeCharacterById() {
        viewModel.characterById.observe(this, Observer {
            character = it
            setDataInFields(it)
        })
    }

    private fun observeSkillByName() {
        viewModel.getSkillByNameComplete.observe(this, Observer {
            println(it)
        })
    }

    private fun observeCharacterSkillsById() {
        viewModel.characterSkillsByIdComplete.observe(this, Observer {
            viewModel.getSkillByNames(it)
        })
    }

    private fun observeSkillByNames()
    {
        viewModel.getSkillByNamesComplete.observe(this, Observer {
            setItems(it)
        })
    }

    private fun observeDeleteComplete() {
        viewModel.deleteComplete.observe(this, Observer {
            Toast.makeText(activity, "deleted", Toast.LENGTH_SHORT).show()
        })
    }

    private fun observeErrors() {
        viewModel.error.observe(this, Observer {
            Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show()
            println("ERROR!!! ${it.printStackTrace()}")
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setDataInFields(ch: Character) {
        character_card_id.text = ch.id.toString()
        character_card_name.text = "${resources.getString(R.string.name)}: ${ch.name}"
        character_card_player_name.text = "${resources.getString(R.string.player_name)}: ${ch.playerName}"//todo and this too
        character_card_world.text = "${resources.getString(R.string.world)}: ${ch.world}"
        character_card_tl.text = "${resources.getString(R.string.tl)}: ${ch.tl}"
        character_card_age.text = "${resources.getString(R.string.age)}: ${ch.age}"
        character_card_eye.text = "${resources.getString(R.string.eyes)}: ${ch.eyes}"
        character_card_hairs.text = "${resources.getString(R.string.hairs)}: ${ch.hairs}"
        character_card_skin.text = "${resources.getString(R.string.skin)}: ${ch.skin}"
        character_card_height.text = "${resources.getString(R.string.height)}: ${ch.height}"
        character_card_weight.text = "${resources.getString(R.string.weight)}: ${ch.weight}"
        character_card_gender.text = "${resources.getString(R.string.gender)}: ${ch.gender}"
        character_card_race.text = "${resources.getString(R.string.race)}: ${ch.race}"
        character_card_sm.text ="${resources.getString(R.string.sm)}: ${ch.sm}"
        character_card_st.text = ch.st.toString()
        character_card_dx.text = ch.dx.toString()
        character_card_iq.text = ch.iq.toString()
        character_card_ht.text = ch.ht.toString()
        character_card_hp.text = ch.hp.toString()
        character_card_move.text = ch.move.toString()
        character_card_speed.text = ch.speed.toString()
        character_card_will.text = ch.will.toString()
        character_card_per.text = ch.per.toString()
        character_card_fp.text = ch.fp.toString()
        val bytes = Base64.decode(ch.portrait, Base64.DEFAULT)
        val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        character_card_image.setImageBitmap(image)
    }
}
