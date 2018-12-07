package bav.onecell.battle.results

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.common.Consts
import bav.onecell.common.extensions.visible
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.hexes.Hex
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.attackHexReward
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.preview
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.cellName
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.deadOrAlive
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.deathRayHexReward
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.energyHexReward
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.lifeHexReward
import kotlinx.android.synthetic.main.item_row_cell_in_battle_results.view.omniBulletHexReward
import kotlinx.android.synthetic.main.view_hex_picker.view.buttonHex

class BattleResultsColumnRecyclerViewAdapter(private val presenter: BattleResults.Presenter,
                                             private val drawUtils: DrawUtils,
                                             private val resourceProvider: Common.ResourceProvider) :
        androidx.recyclerview.widget.RecyclerView.Adapter<BattleResultsColumnRecyclerViewAdapter.ViewHolder>() {

    var groupId: Int = Consts.MAIN_CHARACTERS_GROUP_ID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_row_cell_in_battle_results, parent, false)
        return ViewHolder(view, presenter)
    }

    override fun getItemCount() = presenter.cellsCount(groupId)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        presenter.getCell(groupId, position)?.let {
            holder.view.preview.setImageResource(resourceProvider.getAvatarDrawableId(it.data.id.toInt()))
            holder.view.cellName.text = presenter.getCellName(it.data.name)
        }
        holder.view.deadOrAlive.visibility = if (!presenter.getDeadOrAlive(groupId, position)) View.VISIBLE else View.INVISIBLE
        
        for (type in arrayOf(Hex.Type.LIFE, Hex.Type.ATTACK, Hex.Type.ENERGY, Hex.Type.DEATH_RAY, Hex.Type.OMNI_BULLET)) {
            val reward = presenter.getRewardByType(groupId, position, type.ordinal)
            val rewardView = when (type) {
                Hex.Type.LIFE -> holder.view.lifeHexReward
                Hex.Type.ATTACK -> holder.view.attackHexReward
                Hex.Type.ENERGY -> holder.view.energyHexReward
                Hex.Type.DEATH_RAY -> holder.view.deathRayHexReward
                Hex.Type.OMNI_BULLET -> holder.view.omniBulletHexReward
                else -> null
            }
            rewardView?.visible = reward > 0
            rewardView?.setHexCount(reward)
        }
    }

    class ViewHolder(val view: View, private val presenter: BattleResults.Presenter)
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        init {
            view.lifeHexReward.buttonHex.setImageResource(R.drawable.ic_hex_life)
            view.attackHexReward.buttonHex.setImageResource(R.drawable.ic_hex_attack)
            view.energyHexReward.buttonHex.setImageResource(R.drawable.ic_hex_energy)
            view.deathRayHexReward.buttonHex.setImageResource(R.drawable.ic_hex_death_ray)
            view.omniBulletHexReward.buttonHex.setImageResource(R.drawable.ic_hex_omni_bullet)
        }
    }

    companion object {
        private const val TAG = "ColumnAdapter"
    }
}
