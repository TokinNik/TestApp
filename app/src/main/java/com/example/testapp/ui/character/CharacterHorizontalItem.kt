package com.example.testapp.ui.character

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import com.example.testapp.R
import com.example.testapp.custom_view.outline_corner.OutlineProviders
import com.example.testapp.ui.SelectableData
import com.example.testapp.db.entity.Character
import com.example.testapp.util.Base64RequestHandler
import com.squareup.picasso.Picasso
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.card_character_all.*
import kotlinx.android.synthetic.main.item_character.view.*
import kotlinx.android.synthetic.main.item_character_horizontal.view.*


class CharacterHorizontalItem(
    val character: SelectableData<Character>,
    val colorActive: Int,
    val colorInactive: Int,
    private val onClick: (character: SelectableData<Character>) -> Unit
) : Item() {

    lateinit var rootView: View

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.apply {
            root.item_horizontal_character_name.text = character.data.name
            Picasso
                .get()
                .load("${Base64RequestHandler.BASE_64_SCHEME}${character.data.portrait}")
                .placeholder(R.drawable.gm_logo_original)
                .error(R.drawable.gm_logo_original)
                .into(root.item_horizontal_character_image)

            root.outlineProvider = OutlineProviders(8f, OutlineProviders.OutlineType.ROUND_RECT)
            root.clipToOutline = true

            if (!character.select) {
                root.setBackgroundColor(colorInactive)
            } else {
                root.setBackgroundColor(colorActive)
            }

            rootView = root
        }
    }

    override fun getLayout(): Int = R.layout.item_character_horizontal

}