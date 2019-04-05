package bav.onecell.common

class Consts {
    companion object {
        const val NEXT_SCENE = "next_scene"
        const val YES_NEXT_SCENE = "yes_next_scene"
        const val NO_NEXT_SCENE = "no_next_scene"
        const val BATTLE_PARAMS = "battleParams"
        const val BATTLE_REWARD = "reward"
        const val BATTLE_GROUND_RESOURCE = "ground_resource"
        const val SCENE_ID = "id"
        const val HERO_GROUP_ID = 0
        const val GAME_STATE_CHANGES = "gameStateChanges"

        const val KITTARO_INDEX = 0
        const val ZOI_INDEX = 1
        const val AIMA_INDEX = 2

        const val MAIN_CHARACTERS_GROUP_ID = 0

        val ZERO: () -> Int = {0}
    }

    enum class SceneId(val value: String) {
        INTRODUCTION("cut_scene_introduction"),
        BATTLE_GOPNIKS("battle_gopniks"),
        AFTER_GOPNIKS("cut_scene_after_gopniks"),
        BEFORE_SKILOS("cut_scene_before_skilos"),
        BATTLE_SKILOS("battle_skilos"),
        AFTER_SKILOS("cut_scene_after_skilos"),
        GONATO("cut_scene_gonato"),
        BATTLE_BELOS("battle_belos"),
        AFTER_BELOS("cut_scene_after_belos"),
        ANALAFRO("cut_scene_analafro"),
        BATTLE_OMIKHLI("battle_omikhli"),
        AFTER_OMIKHLI("cut_scene_after_omikhli"),
        KILIA("cut_scene_kilia"),
        BATTLE_NIKHTERIBS("battle_nikhteribs"),
        AFTER_NIKHTERIBS("cut_scene_after_nikhteribs"),
        KARDIA("cut_scene_kardia"),
        BATTLE_DRUNKARDS("battle_drunkards"),
        AFTER_DRUNKARDS("cut_scene_after_drunkards"),
        LAIMO("cut_scene_laimo"),
        BATTLE_KATOFI_PONU("battle_katofi_ponu"),
        AFTER_KATOFI_PONU("cut_scene_after_katofi_ponu"),
        ENKEFALIO("cut_scene_enkefalio"),
        BATTLE_ENKEFALIO("battle_enkefalio"),
        FINAL_ACT("cut_scene_final_act")
    }
}