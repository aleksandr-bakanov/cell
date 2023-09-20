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
import bav.onecell.databinding.ItemRowCellInBattleResultsBinding
import bav.onecell.model.hexes.Hex

class BattleResultsColumnRecyclerViewAdapter(private val presenter: BattleResults.Presenter,
                                             private val drawUtils: DrawUtils,
                                             private val resourceProvider: Common.ResourceProvider) :
        androidx.recyclerview.widget.RecyclerView.Adapter<BattleResultsColumnRecyclerViewAdapter.ViewHolder>() {

    var groupId: Int = Consts.MAIN_CHARACTERS_GROUP_ID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRowCellInBattleResultsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, presenter)
    }

    override fun getItemCount() = presenter.cellsCount(groupId)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        presenter.getCell(groupId, position)?.let {
            holder.binding.preview.setImageResource(resourceProvider.getAvatarDrawableId(it.data.id.toInt()))
            holder.binding.cellName.text = presenter.getCellName(it.data.name)
        }
        holder.binding.deadOrAlive.visibility = if (!presenter.getDeadOrAlive(groupId, position)) View.VISIBLE else View.INVISIBLE
        
        for (type in arrayOf(Hex.Type.LIFE, Hex.Type.ATTACK, Hex.Type.ENERGY, Hex.Type.DEATH_RAY, Hex.Type.OMNI_BULLET)) {
            val reward = presenter.getRewardByType(groupId, position, type.ordinal)
            val rewardView = when (type) {
                Hex.Type.LIFE -> holder.binding.lifeHexReward
                Hex.Type.ATTACK -> holder.binding.attackHexReward
                Hex.Type.ENERGY -> holder.binding.energyHexReward
                Hex.Type.DEATH_RAY -> holder.binding.deathRayHexReward
                Hex.Type.OMNI_BULLET -> holder.binding.omniBulletHexReward
                else -> null
            }
            rewardView?.visible = reward > 0
            rewardView?.setHexCount(reward)
        }
    }

    class ViewHolder(val binding: ItemRowCellInBattleResultsBinding, private val presenter: BattleResults.Presenter)
        : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        init {
            binding.lifeHexReward.buttonHex.setImageResource(R.drawable.ic_hex_life)
            binding.attackHexReward.buttonHex.setImageResource(R.drawable.ic_hex_attack)
            binding.energyHexReward.buttonHex.setImageResource(R.drawable.ic_hex_energy)
            binding.deathRayHexReward.buttonHex.setImageResource(R.drawable.ic_hex_death_ray)
            binding.omniBulletHexReward.buttonHex.setImageResource(R.drawable.ic_hex_omni_bullet)
        }
    }

    companion object {
        private const val TAG = "ColumnAdapter"
    }
}
