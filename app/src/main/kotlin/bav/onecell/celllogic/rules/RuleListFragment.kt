package bav.onecell.celllogic.rules

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
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
        Log.d(TAG, "$this onCreateView")
        return inflater.inflate(R.layout.fragment_rule_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "$this onActivityCreated")
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
        Log.d(TAG, "$this onDestroyView")
        disposables.dispose()
        super.onDestroyView()
    }

    private fun inject() {
        (requireActivity().application as OneCellApplication).appComponent
                .plus(CellLogicModule())
                .inject(this)
    }

    companion object {
        private const val TAG = "RuleListFragment"
        const val CELL_INDEX = "cell_index"

        @JvmStatic
        fun newInstance(bundle: Bundle?): RuleListFragment {
            val fragment = RuleListFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, "$this onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$this onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "$this onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "$this onResume")
    }

    override fun onPause() {
        Log.d(TAG, "$this onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "$this onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "$this onDestroy")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "$this onDetach")
        super.onDetach()
    }
}
