package app.resolvekit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment

class ResolveKitChatFragment : Fragment() {
    private val hostId: String
        get() = requireArguments().getString(ARG_HOST_ID)
            ?: error("ResolveKitChatFragment requires a host id")

    private val runtime by lazy(LazyThreadSafetyMode.NONE) {
        val factory = ResolveKitChatHostRegistry.resolve(hostId)
            ?: error("No ResolveKit runtime factory registered for id=$hostId")
        factory.create(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { ResolveKitChatView(runtime = runtime) }
    }

    override fun onDestroy() {
        runtime.stop()
        val changingConfigurations = activity?.isChangingConfigurations == true
        if (!changingConfigurations) {
            ResolveKitChatHostRegistry.unregister(hostId)
        }
        super.onDestroy()
    }

    companion object {
        private const val ARG_HOST_ID = "resolvekit.host_id"

        fun newInstance(factory: ResolveKitRuntimeFactory): ResolveKitChatFragment {
            val hostId = ResolveKitChatHostRegistry.register(factory)
            return ResolveKitChatFragment().apply {
                arguments = Bundle().apply { putString(ARG_HOST_ID, hostId) }
            }
        }

        fun newInstance(configuration: ResolveKitConfiguration): ResolveKitChatFragment =
            newInstance { context -> ResolveKitRuntime(configuration, context) }
    }
}
