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
import kotlinx.android.synthetic.main.fragment_condition_list.buttonAddNewCondition
import kotlinx.android.synthetic.main.fragment_condition_list.recyclerViewConditionsList

class ConditionListFragment : Fragment(), CellLogic.PresenterProvider {

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
        return inflater.inflate(R.layout.fragment_condition_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonAddNewCondition.setOnClickListener { host.provideCellLogicPresenter().createNewCondition() }
        buttonAddNewCondition.visibility = View.INVISIBLE

        recyclerViewConditionsList.layoutManager = LinearLayoutManager(context)
        recyclerViewConditionsList.adapter = ConditionsRecyclerViewAdapter(host.provideCellLogicPresenter())

        disposables.addAll(
                host.provideCellLogicPresenter().conditionsUpdateNotifier().subscribe {
                    buttonAddNewCondition.visibility = View.VISIBLE
                    recyclerViewConditionsList.adapter.notifyDataSetChanged()
                },
                host.provideCellLogicPresenter().conditionsEditNotifier().subscribe { condition ->
                    val conditionEditorDialogFragment = ConditionEditorDialogFragment()
                    // TODO: check we attached to activity
                    conditionEditorDialogFragment.show(requireActivity().fragmentManager, CONDITION_EDITOR_DIALOG_TAG)
                },
                host.provideCellLogicPresenter().actionEditNotifier().subscribe {
                    val actionEditorDialogFragment = ActionEditorDialogFragment()
                    actionEditorDialogFragment.show(requireActivity().fragmentManager, ACTION_EDITOR_DIALOG_TAG)
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
        private const val ACTION_EDITOR_DIALOG_TAG = "action_editor_dialog"

        @JvmStatic
        fun newInstance() = ConditionListFragment()
    }
}
