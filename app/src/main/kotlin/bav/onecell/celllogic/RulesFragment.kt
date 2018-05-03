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

import kotlinx.android.synthetic.main.fragment_rules.buttonAddNewRule
import kotlinx.android.synthetic.main.fragment_rules.recyclerViewRulesList

class RulesFragment : Fragment() {

    private lateinit var host: OnRulesFragmentInteractionListener
    private val disposables = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRulesFragmentInteractionListener) {
            host = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnRulesFragmentInteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rules, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        buttonAddNewRule.setOnClickListener { host.provideCellLogicPresenter().createNewRule() }

        recyclerViewRulesList.layoutManager = LinearLayoutManager(context)
        recyclerViewRulesList.adapter = RulesRecyclerViewAdapter(host.provideCellLogicPresenter())

        disposables.add(host.provideCellLogicPresenter().rulesUpdateNotifier().subscribe {
            recyclerViewRulesList.adapter.notifyDataSetChanged()
        })
    }

    override fun onDestroyView() {
        disposables.dispose()
        super.onDestroyView()
    }

    interface OnRulesFragmentInteractionListener {
        fun provideCellLogicPresenter(): CellLogic.Presenter
    }

    companion object {
        @JvmStatic
        fun newInstance() = RulesFragment()
    }
}
