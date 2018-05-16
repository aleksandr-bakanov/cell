package bav.onecell.celllogic.conditions

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.OneCellApplication
import bav.onecell.R
import bav.onecell.celllogic.CellLogicModule
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_condition_list.buttonAddNewCondition
import kotlinx.android.synthetic.main.fragment_condition_list.recyclerViewConditionsList
import javax.inject.Inject

class ConditionListFragment : Fragment() {

    @Inject
    lateinit var presenter: Conditions.Presenter
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_condition_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()

        buttonAddNewCondition.setOnClickListener { presenter.createNewCondition() }

        recyclerViewConditionsList.layoutManager = LinearLayoutManager(context)
        recyclerViewConditionsList.adapter = ConditionsRecyclerViewAdapter(presenter)

        disposables.addAll(
                presenter.conditionsUpdateNotifier().subscribe {
                    recyclerViewConditionsList.adapter.notifyDataSetChanged()
                },
                presenter.conditionsEditNotifier().subscribe { condition ->
                    val conditionEditorDialogFragment = ConditionEditorDialogFragment()
                    // TODO: check we attached to activity
                    conditionEditorDialogFragment.show(requireActivity().fragmentManager,
                                                       CONDITION_EDITOR_DIALOG_TAG)
                }
        )
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent.plus(CellLogicModule()).inject(this)
    }

    companion object {
        private const val CONDITION_EDITOR_DIALOG_TAG = "condition_editor_dialog"
        private const val ACTION_EDITOR_DIALOG_TAG = "action_editor_dialog"

        private const val CELL_INDEX = "cell_index"
        private const val RULE_INDEX = "rule_index"

        @JvmStatic
        fun newInstance(cellIndex: Int, ruleIndex: Int): ConditionListFragment {
            val bundle = Bundle()
            bundle.putInt(CELL_INDEX, cellIndex)
            bundle.putInt(RULE_INDEX, ruleIndex)
            val fragment = ConditionListFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
