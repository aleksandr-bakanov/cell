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
        presenter.initializeConditionList(arguments?.getInt(CELL_INDEX) ?: -1,
                                          arguments?.getInt(RULE_INDEX) ?: -1)

        buttonAddNewCondition.setOnClickListener { presenter.createNewCondition() }

        recyclerViewConditionsList.layoutManager = LinearLayoutManager(context)
        recyclerViewConditionsList.adapter = ConditionsRecyclerViewAdapter(presenter)

        disposables.addAll(
                presenter.conditionsUpdateNotifier().subscribe {
                    recyclerViewConditionsList.adapter.notifyDataSetChanged()
                }
        )
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent.plus(
                CellLogicModule()).inject(this)
    }

    companion object {
        private const val TAG = "ConditionListFragment"
        const val CELL_INDEX = "cell_index"
        const val RULE_INDEX = "rule_index"

        @JvmStatic
        fun newInstance(bundle: Bundle?): ConditionListFragment {
            val fragment = ConditionListFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
