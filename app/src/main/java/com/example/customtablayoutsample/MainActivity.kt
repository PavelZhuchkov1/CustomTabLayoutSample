package com.example.customtablayoutsample

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    companion object {
        private const val FRAGMENT_ONE_TAG = "Fragment1"
        private const val FRAGMENT_TWO_TAG = "Fragment2"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pagerIndicator = findViewById<PagerIndicator>(R.id.pager_indicator)
        val viewPagerCover = findViewById<ViewPager2>(R.id.view_pager_cover)
        val fractionText = findViewById<TextView>(R.id.fraction)
        val stateText = findViewById<TextView>(R.id.state)

        viewPagerCover.adapter = ViewPagerAdapter()
        viewPagerCover.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                pagerIndicator.fraction = position + positionOffset
            }
        })

        pagerIndicator.onTextClick = { pagerIndicator.setTextState() }
        pagerIndicator.onAudioClick = { pagerIndicator.setAudioState() }

        pagerIndicator.fractionListener = { fraction ->
            fractionText.text = fraction.toString()
            stateText.text = pagerIndicator.state.toString()

            // fragment container view
            val transition = supportFragmentManager.beginTransaction()
            val showOne = fraction < 0.5f
            val showTwo = fraction > 0.5f
            if (showOne || showTwo) {
                val sourceFragmentTag = if (showOne) FRAGMENT_TWO_TAG else FRAGMENT_ONE_TAG
                val destinationFragmentTag = if (showOne) FRAGMENT_ONE_TAG else FRAGMENT_TWO_TAG
                if (supportFragmentManager.findFragmentByTag(destinationFragmentTag) == null) {
                    transition
                        .setCustomAnimations(
                            android.R.animator.fade_in,
                            android.R.animator.fade_out
                        )
                        .add(
                            R.id.fragment_container,
                            TextFragment.newInstance(destinationFragmentTag),
                            destinationFragmentTag
                        )
                } else {
                    transition
                        .setCustomAnimations(
                            android.R.animator.fade_in,
                            android.R.animator.fade_out
                        )
                        .show(supportFragmentManager.findFragmentByTag(destinationFragmentTag)!!)
                }

                if (supportFragmentManager.findFragmentByTag(sourceFragmentTag) != null) {
                    transition
                        .setCustomAnimations(
                            android.R.animator.fade_in,
                            android.R.animator.fade_out
                        )
                        .hide(supportFragmentManager.findFragmentByTag(sourceFragmentTag)!!)
                }
                transition.commit()
            }
        }
    }
}

class TextFragment : Fragment() {

    companion object {
        private const val ARG_TEXT = "text"

        fun newInstance(text: String): TextFragment {
            return TextFragment().apply {
                arguments = bundleOf(ARG_TEXT to text)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.main_text)
        textView.text = requireArguments().getString(ARG_TEXT)
        view.setBackgroundColor(Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)))
    }
}

class ViewPagerAdapter : RecyclerView.Adapter<ViewPagerAdapter.SimpleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
        return SimpleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_cover, parent, false))
    }

    override fun getItemCount(): Int = 2

    override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
        // noop
    }

    class SimpleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}