package com.example.testapp.ui.skill

import android.view.View
import com.example.testapp.R
import com.example.testapp.custom_view.outline_corner.OutlineProviders
import com.example.testapp.ui.SelectableData
import com.example.testapp.db.entity.Skill.Skill

import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_skill.view.*

class SkillItem(
    val skill: SelectableData<Skill>,
    val colorActive: Int,
    val colorInactive: Int
) : Item() {

    lateinit var rootView: View

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.apply {
            root.outlineProvider = OutlineProviders(8f, OutlineProviders.OutlineType.ROUND_RECT)
            root.clipToOutline = true
            root.item_skill_id.text = skill.data.id.toString()
            root.item_skill_name.text = skill.data.name
            root.item_skill_default.text = skill.data.difficulty
            root.item_skill_prereq.text = skill.data.nameLoc

            if (!skill.select) {
                root.setBackgroundColor(colorInactive)
            } else {
                root.setBackgroundColor(colorActive)
            }

            rootView = root
        }
    }

    override fun getLayout(): Int = R.layout.item_skill
}