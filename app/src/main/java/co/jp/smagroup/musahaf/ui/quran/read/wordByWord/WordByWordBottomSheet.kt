package co.jp.smagroup.musahaf.ui.quran.read.wordByWord


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import co.jp.smagroup.musahaf.R
import co.jp.smagroup.musahaf.ui.commen.sharedComponent.MushafApplication
import co.jp.smagroup.musahaf.ui.quran.sharedComponent.BaseActivity
import co.jp.smagroup.musahaf.utils.extensions.observer
import co.jp.smagroup.musahaf.utils.extensions.viewModelOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_word_by_word.*

class WordByWordBottomSheet : BottomSheetDialogFragment() {
    
    companion object {
        const val TAG = "WordByWordBottomSheet"
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as BaseActivity).systemUiVisibility(false)

        viewModelOf(WordByWordViewModel::class.java).getWordByWord().observer(viewLifecycleOwner) {
            recycler_wordByWord.adapter = WordByWordAdapter(it)
        }
        val closeIcon = if (MushafApplication.isDarkThemeEnabled) R.drawable.ic_close_light else R.drawable.ic_close_dark
        close_image.setImageResource(closeIcon)
        close_image.setOnClickListener {
            dismiss()
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dialog_word_by_word, container, false)
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        
        dialog.setOnShowListener { Dialog ->
            val d = Dialog as BottomSheetDialog
            
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset > 0.5) {
                        close_image.visibility = View.VISIBLE
                    } else {
                        close_image.visibility = View.GONE
                    }
                }
                
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN)
                        dismiss()
                }
            })
        }
        return dialog
    }
    
    override fun onDestroy() {
        super.onDestroy()
       (activity as BaseActivity).systemUiVisibility(true)
    }
}
