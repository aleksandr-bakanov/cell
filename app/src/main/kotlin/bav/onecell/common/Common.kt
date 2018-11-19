package bav.onecell.common

import android.graphics.drawable.Drawable
import bav.onecell.model.cell.logic.Action
import bav.onecell.model.cell.logic.Condition

interface Common {
    interface ResourceProvider {
        fun getActionRepresentation(action: Action): String

        fun getFieldToCheckRepresentation(fieldToCheck: Condition.FieldToCheck): String

        fun getOperationRepresentation(operation: Condition.Operation): String

        fun getExpectedValueRepresentation(fieldToCheck: Condition.FieldToCheck, expected: Int): String

        fun getConditionRepresentation(condition: Condition): String

        fun getAvatarDrawable(index: Int): Drawable?

        fun getAvatarDrawableId(index: Int): Int

        fun getDrawableIdentifier(name: String?): Int

        fun getIdIdentifier(name: String?): Int

        fun getStringIdentifier(name: String?): Int

        fun getDrawable(id: Int): Drawable?
        fun getDrawable(name: String?): Drawable?

        fun getString(id: Int): String?
        fun getString(name: String?): String?
    }

    interface GameState {
        fun dropGameState()

        fun getLastNavDestinationId(): Int
        fun setLastNavDestinationId(id: Int)

        fun isFirstLaunch(): Boolean

        fun setDecision(field: String, value: Boolean)
        fun getDecision(field: String): Decision
        fun isDecisionPositive(field: String): Boolean

        fun setCurrentFrame(index: Int)
        fun getCurrentFrame(): Int

        companion object {
            const val BATTLE_LOGIC_AVAILABLE = "battle_logic_available"
            const val ATTACK_HEXES_AVAILABLE = "attack_hexes_available"
            const val ENERGY_HEXES_AVAILABLE = "energy_hexes_available"
            const val DEATH_RAY_HEXES_AVAILABLE = "death_ray_hexes_available"
            const val OMNI_BULLET_HEXES_AVAILABLE = "omni_bullet_hexes_available"
            const val HEX_TRANSFORMATION_AVAILABLE = "hex_transformation_available"
            const val HELP_GONATO = "help_gonato"
        }

        enum class Decision {
            NOT_TAKEN, YES, NO
        }
    }
}
