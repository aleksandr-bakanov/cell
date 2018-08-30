package bav.onecell.battle.results

import android.os.Bundle
import android.support.v4.app.Fragment
import bav.onecell.OneCellApplication
import javax.inject.Inject

class BattleResultsFragment: Fragment(), BattleResults.View {

    @Inject lateinit var presenter: BattleResults.Presenter

    //region Lifecycle methods
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()
    }
    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(BattleResultsModule(this))
                .inject(this)
    }
    //endregion
}
