package bav.onecell.celllogic.rules

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
import kotlinx.android.synthetic.main.fragment_rule_list.buttonAddNewRule
import kotlinx.android.synthetic.main.fragment_rule_list.recyclerViewRulesList
import javax.inject.Inject

class RuleListFragment : Fragment() {

    @Inject
    lateinit var presenter: Rules.Presenter
    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rule_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()
        presenter.initialize(arguments?.getInt(CELL_INDEX) ?: -1)

        buttonAddNewRule.setOnClickListener { presenter.createNewRule() }

        recyclerViewRulesList.layoutManager = LinearLayoutManager(context)
        recyclerViewRulesList.adapter = RulesRecyclerViewAdapter(presenter)

        disposables.addAll(
                presenter.rulesUpdateNotifier().subscribe {
                    recyclerViewRulesList.adapter.notifyDataSetChanged()
                }
        )
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(CellLogicModule())
                .inject(this)
    }

    companion object {
        const val CELL_INDEX = "cell_index"

        @JvmStatic
        fun newInstance(bundle: Bundle?): RuleListFragment {
            val fragment = RuleListFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
