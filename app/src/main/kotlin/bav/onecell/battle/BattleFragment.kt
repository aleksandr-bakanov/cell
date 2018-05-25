package bav.onecell.battle

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.common.view.DrawUtils
import bav.onecell.model.BattleFieldSnapshot
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_battle.battleCanvasView
import kotlinx.android.synthetic.main.fragment_battle.buttonFinishBattle
import kotlinx.android.synthetic.main.fragment_battle.buttonNextStep
import kotlinx.android.synthetic.main.fragment_battle.buttonPreviousStep
import kotlinx.android.synthetic.main.fragment_battle.seekBar
import javax.inject.Inject

class BattleFragment : Fragment(), Battle.View {

    @Inject
    lateinit var hexMath: HexMath
    @Inject
    lateinit var presenter: Battle.Presenter
    @Inject
    lateinit var drawUtils: DrawUtils
    private val disposables = CompositeDisposable()
    private val seekBarProgress = PublishSubject.create<Int>()

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                updateBattleView(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    //region Lifecycle methods
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_battle, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        Log.d(TAG, "onActivityCreated: presenter = $presenter")

        buttonFinishBattle.setOnClickListener { presenter.finishBattle() }
        buttonNextStep.setOnClickListener {
            val next = battleCanvasView.currentSnapshotIndex + 1
            if (next < battleCanvasView.snapshots.size) {
                seekBar.progress = next
                updateBattleView(next)
            }
        }
        buttonPreviousStep.setOnClickListener {
            val next = battleCanvasView.currentSnapshotIndex - 1
            if (next >= 0) {
                seekBar.progress = next
                updateBattleView(next)
            }
        }

        seekBar.setOnSeekBarChangeListener(seekBarListener)

        battleCanvasView.hexMath = hexMath
        battleCanvasView.drawUtils = drawUtils
        battleCanvasView.presenter = presenter

        seekBar.max = 0
        disposables.addAll(presenter.snapshotsCounter()
                                   .subscribeOn(Schedulers.io())
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .subscribe {
                                       seekBar.max = it - 1
                                   })

        presenter.initialize(arguments?.getIntegerArrayList(EXTRA_CELL_INDEXES) ?: arrayListOf())
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    //endregion

    //region Private methods
    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(BattleModule(this))
                .inject(this)
    }
    //endregion

    //region Overridden methods
    override fun setBackgroundFieldRadius(radius: Int) {
        battleCanvasView.backgroundFieldRadius = radius
        battleCanvasView.invalidate()
    }

    override fun updateBattleView(snapshotIndex: Int) {
        battleCanvasView.currentSnapshotIndex = snapshotIndex
        battleCanvasView.invalidate()
    }

    override fun setRing(ring: List<Hex>) {
        battleCanvasView.ring = ring
    }

    override fun reportBattleEnd() {
        activity?.runOnUiThread {
            Toast.makeText(activity, "Battle is over", Toast.LENGTH_SHORT).show()
            buttonFinishBattle.visibility = View.VISIBLE
        }
    }

    override fun setSnapshots(snapshots: List<BattleFieldSnapshot>) {
        battleCanvasView.snapshots = snapshots
    }
    //endregion

    companion object {
        private const val TAG = "BattleFragment"
        const val EXTRA_CELL_INDEXES = "cell_indexes"

        fun newInstance(bundle: Bundle?): BattleFragment {
            val fragment = BattleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
