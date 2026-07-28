package org.wikipedia.ai

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.wikipedia.compose.theme.BaseTheme
import org.wikipedia.main.MainFragment

class InixaAlphaFragment : Fragment() {

    private val viewModel: InixaAlphaViewModel by viewModels()
    private var isKeyboardVisible = false
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BaseTheme {
                    InixaAlphaScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Foolproof keyboard detection using ViewTreeObserver.OnGlobalLayoutListener.
        // This works on ALL Android versions unlike WindowInsetsCompat.isVisible(ime())
        // which is API 30+ only and may not fire in ViewPager2 fragments.
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rootView = view.rootView
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom
            // Keyboard is visible if it occupies more than 15% of the screen
            val keyboardNowVisible = keypadHeight > screenHeight * 0.15

            if (keyboardNowVisible != isKeyboardVisible) {
                isKeyboardVisible = keyboardNowVisible
                (parentFragment as? MainFragment)?.setBottomNavVisible(!keyboardNowVisible)
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    override fun onDestroyView() {
        // Remove listener and restore bottom nav
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(globalLayoutListener)
        globalLayoutListener = null
        (parentFragment as? MainFragment)?.setBottomNavVisible(true)
        super.onDestroyView()
    }

    companion object {
        fun newInstance(): InixaAlphaFragment {
            return InixaAlphaFragment()
        }
    }
}
