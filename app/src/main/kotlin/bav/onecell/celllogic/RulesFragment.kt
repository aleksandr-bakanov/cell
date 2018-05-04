package bav.onecell.celllogic

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_rules.buttonAddNewCondition

import kotlinx.android.synthetic.main.fragment_rules.buttonAddNewRule
import kotlinx.android.synthetic.main.fragment_rules.recyclerViewConditionsList
import kotlinx.android.synthetic.main.fragment_rules.recyclerViewRulesList

class RulesFragment : Fragment(), CellLogic.PresenterProvider {

    private lateinit var host: CellLogic.PresenterProvider
    private val disposables = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CellLogic.PresenterProvider) {
            host = context
        } else {
            throw RuntimeException(context.toString() + " must implement CellLogic.PresenterProvider")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rules, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonAddNewRule.setOnClickListener { host.provideCellLogicPresenter().createNewRule() }

        buttonAddNewCondition.setOnClickListener { host.provideCellLogicPresenter().createNewCondition() }
        buttonAddNewCondition.visibility = View.INVISIBLE

        recyclerViewRulesList.layoutManager = LinearLayoutManager(context)
        recyclerViewRulesList.adapter = RulesRecyclerViewAdapter(host.provideCellLogicPresenter())

        recyclerViewConditionsList.layoutManager = LinearLayoutManager(context)
        recyclerViewConditionsList.adapter = ConditionsRecyclerViewAdapter(host.provideCellLogicPresenter())

        disposables.addAll(
                host.provideCellLogicPresenter().rulesUpdateNotifier().subscribe {
                    recyclerViewRulesList.adapter.notifyDataSetChanged()
                },
                host.provideCellLogicPresenter().conditionsUpdateNotifier().subscribe {
                    buttonAddNewCondition.visibility = View.VISIBLE
                    recyclerViewConditionsList.adapter.notifyDataSetChanged()
                },
                host.provideCellLogicPresenter().conditionsEditNotifier().subscribe {
                    val conditionEditorDialogFragment = ConditionEditorDialogFragment()
                    // TODO: check we attached to activity
                    conditionEditorDialogFragment.show(requireActivity().fragmentManager, CONDITION_EDITOR_DIALOG_TAG)
                }
        )
    }

    override fun onDestroyView() {
        host.provideCellLogicPresenter().openConditionsList(-1)
        disposables.dispose()
        super.onDestroyView()
    }

    override fun provideCellLogicPresenter(): CellLogic.Presenter = host.provideCellLogicPresenter()

    companion object {
        private const val CONDITION_EDITOR_DIALOG_TAG = "condition_editor_dialog"

        @JvmStatic
        fun newInstance() = RulesFragment()
    }
}
