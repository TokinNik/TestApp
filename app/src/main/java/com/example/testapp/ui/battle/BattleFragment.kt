package com.example.testapp.ui.battle


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testapp.R
import com.example.testapp.databinding.FragmentBattleBinding
import com.example.testapp.db.entity.Character
import com.example.testapp.db.entity.Skill.Skill
import com.example.testapp.getThemeColor
import com.example.testapp.ui.SelectableData
import com.example.testapp.ui.character.CharacterCard
import com.example.testapp.ui.character.CharacterHorizontalItem
import com.example.testapp.ui.settings.ColorScheme
import com.example.testapp.ui.skill.observe.single.SkillObserveSingleFragment
import com.example.testapp.util.GurpsCalculations
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.card_character_all.*
import kotlinx.android.synthetic.main.fragment_battle.*
import toothpick.Toothpick
import toothpick.ktp.delegate.inject
import toothpick.smoothie.viewmodel.installViewModelBinding

class BattleFragment : Fragment() {

    private val viewModel: BattleFragmentViewModel by inject()
    private val gurpsCalculations: GurpsCalculations by inject()

    private val groupAdapterQueue = GroupAdapter<GroupieViewHolder>()
    private val groupAdapterSkills = GroupAdapter<GroupieViewHolder>()

    private var activeCharacterPos = 0
    private var isActionsCollapsed = true

    private lateinit var characterCard: CharacterCard
    private lateinit var battleBinding: FragmentBattleBinding
    private lateinit var activeCharacter: CharacterHorizontalItem

    private val navController: NavController?
        get() = activity?.let { Navigation.findNavController(it, R.id.nav_host_fragment) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        battleBinding = FragmentBattleBinding.inflate(inflater, container, false)
        return battleBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val scope = Toothpick.openScope("APP")
        scope.installViewModelBinding<BattleFragmentViewModel>(this)
        scope.inject(this)

        viewModel.clearEvents()
        viewModel.getColorScheme()

        characterCard = CharacterCard(
            cardRoot = character_card_root,
            groupAdapter = groupAdapterSkills,
            colorActive = activity!!.getThemeColor(R.attr.colorSecondary),
            colorInactive = activity!!.getThemeColor(R.attr.colorPrimaryVariant),
            onSkillClick = {
                setSkillInfoDialog(it)
            }
        )

        observeCharacters()
        observeColorScheme()
        observeCharacterSkillsById()
        observeSkillByNames()

        recyclerViewInit()
        initOnClick()

        viewModel.getItems()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.forceClear()
    }

    private fun initOnClick() {
        battle_skip.setOnClickListener {
            switchSelect()
        }
        battle_action.setOnClickListener {
            switchSelect()
        }
        battle_defence.setOnClickListener {
            switchSelect()
        }
        battle_attack.setOnClickListener {
            switchSelect()
        }
        battle_move.setOnClickListener {
            switchSelect()
        }
        battle_collapse_actions.setOnClickListener {
            if (isActionsCollapsed) {
                isActionsCollapsed = false
                battle_collapse_actions.text = getString(R.string.gt)//todo in image
                battle_actions_layout.visibility = View.VISIBLE
            } else {
                isActionsCollapsed = true
                battle_collapse_actions.text = getString(R.string.lt)
                battle_actions_layout.visibility = View.GONE
            }
        }
    }

    private fun recyclerViewInit() {
        recyclerView_characters_queue.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            adapter = groupAdapterQueue
        }
    }

    private fun addItems(items: List<Character>)
    {
        groupAdapterQueue.clear()
        val colorActive = activity!!.getThemeColor(R.attr.colorSecondary)
        val colorInactive = activity!!.getThemeColor(R.attr.colorPrimaryVariant)
        for(i in items)
        {
            groupAdapterQueue.apply {
                add(
                    CharacterHorizontalItem(
                        character = SelectableData(i),
                        colorActive = colorActive,
                        colorInactive = colorInactive,
                        onClick = {
//                            val bundle = Bundle()
//                            bundle.putInt("id", it.data.id)
//                            navController?.navigate(R.id.action_startFragment_to_characterFragment, bundle)
                        }
                    )
                )
            }
        }
    }

    private fun setSkillInfoDialog(skill: Skill) {
        val selectSkillDialog = SkillObserveSingleFragment(skill)
        selectSkillDialog.setTargetFragment(this, 1)
        selectSkillDialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.dialogFragmentStyle)
        selectSkillDialog.show(fragmentManager!!, null)
    }

    private fun switchSelect() {
        if (::activeCharacter.isInitialized) {
            activeCharacter.character.select = false
            character_card_root.visibility = View.GONE
            showProgressBar()
            groupAdapterQueue.notifyItemChanged(activeCharacterPos)
        }
        activeCharacterPos = if (activeCharacterPos == groupAdapterQueue.itemCount - 1) 0 else activeCharacterPos + 1
        setSelect(activeCharacterPos)
    }

    private fun setSelect(position: Int) {
        if (groupAdapterQueue.itemCount > 0) {
            activeCharacter = (groupAdapterQueue.getItem(position) as CharacterHorizontalItem)
            activeCharacter.character.select = true
            recyclerView_characters_queue.layoutManager?.scrollToPosition(activeCharacterPos)
            groupAdapterQueue.notifyItemChanged(activeCharacterPos)
            battleBinding.character = gurpsCalculations.getReMathCharacter(activeCharacter.character.data)
            character_card_root.visibility = View.VISIBLE
            viewModel.getCharacterSkillsById(activeCharacter.character.data.id)
            characterCard.setImage(activeCharacter.character.data.portrait)
        }
    }

    private fun observeCharacterSkillsById() {
        viewModel.characterSkillsByIdComplete.observe(this, Observer {
            viewModel.getSkillByNames(it)
        })
    }

    private fun observeSkillByNames() {
        viewModel.getSkillByNamesComplete.observe(this, Observer {
            characterCard.setItems(it)
            hideProgressBar()
        })
    }

    private fun observeColorScheme() {
        viewModel.colorScheme.observe(this, Observer {
            battle_actions_layout.setBackgroundColor(
                if (it == ColorScheme.NIGHT)
                    resources.getColor(R.color.night_background_dark)//todo with attr activity!!.getThemeColor(R.attr.???)
                else
                    resources.getColor(R.color.background_light)
            )
            characterCard.setColorScheme(it)
        })
    }

    private fun observeCharacters() {
        viewModel.characters.observe(this, Observer {
            addItems(it)
            setSelect(activeCharacterPos)
        })
    }

    private fun hideProgressBar() {
        battle_fragment_progress_bar.visibility = View.GONE
    }

    private fun showProgressBar() {
        //battle_fragment_progress_bar.visibility = View.VISIBLE
    }
}
