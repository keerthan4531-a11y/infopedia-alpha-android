package org.wikipedia.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.wikipedia.compose.theme.BaseTheme

class InixaAlphaFragment : Fragment() {

    private val viewModel: InixaAlphaViewModel by viewModels()

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

    companion object {
        fun newInstance(): InixaAlphaFragment {
            return InixaAlphaFragment()
        }
    }
}
