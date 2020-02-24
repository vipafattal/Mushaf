package co.jp.smagroup.musahaf.ui.quran.sharedComponent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.codebox.lib.android.views.utils.gone
import com.codebox.lib.android.views.utils.visible
import kotlinx.android.synthetic.main.layout_connection.*

/**
 * Created by ${User} on ${Date}
 */
abstract class BaseFragment : Fragment() {
    abstract val layoutId: Int
    

    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(layoutId, container, false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        activity?.retryButton?.setOnClickListener {
            activity?.connectionErrorView!!.gone()
            loadData()
        }
    }
    
    @CallSuper
    open fun loadData() {
        loadingStarted()
    }
    
    
    fun loadingCompleted(withError: Boolean) {
        hideLoading()
        if (withError)
            activity?.connectionErrorView!!.visible()
    }
    
    fun loadingStarted() {
        showLoading()
        activity?.connectionErrorView!!.gone()
    }
    
    private fun showLoading() = activity?.loadingView!!.visible()
    
    
    private fun hideLoading() = activity?.loadingView!!.gone()
    
    
    fun errorViewVisible() {
        activity?.connectionErrorView!!.visible()
    }
    
    
}