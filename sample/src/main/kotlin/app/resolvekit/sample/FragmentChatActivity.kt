package app.resolvekit.sample

import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import app.resolvekit.ui.ResolveKitChatFragment

class FragmentChatActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val containerId = android.view.View.generateViewId()
        setContentView(
            FrameLayout(this).apply { id = containerId },
            FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        )

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(containerId, ResolveKitChatFragment.newInstance(SampleRuntimeFactory.createConfiguration(this)))
                .commit()
        }
    }
}
