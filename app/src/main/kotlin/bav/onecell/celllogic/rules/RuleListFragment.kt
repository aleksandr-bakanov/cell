package bav.onecell.celllogic.rules

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import bav.onecell.R
import bav.onecell.celllogic.CellLogic
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_rule_list.buttonAddNewRule
import kotlinx.android.synthetic.main.fragment_rule_list.recyclerViewRulesList

class RuleListFragment : Fragment(), CellLogic.PresenterProvider {

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
        return inflater.inflate(R.layout.fragment_rule_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonAddNewRule.setOnClickListener { host.provideCellLogicPresenter().createNewRule() }

        recyclerViewRulesList.layoutManager = LinearLayoutManager(context)
        recyclerViewRulesList.adapter = RulesRecyclerViewAdapter(
                host.provideCellLogicPresenter())

        disposables.addAll(
                host.provideCellLogicPresenter().rulesUpdateNotifier().subscribe {
                    recyclerViewRulesList.adapter.notifyDataSetChanged()
                }
        )
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    override fun provideCellLogicPresenter(): CellLogic.Presenter = host.provideCellLogicPresenter()

    companion object {
        @JvmStatic
        fun newInstance() = RuleListFragment()
    }
}
