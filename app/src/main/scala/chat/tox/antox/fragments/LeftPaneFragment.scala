package chat.tox.antox.fragments

import android.os.{Build, Bundle}
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.view.ViewGroup.LayoutParams
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{FrameLayout, ImageView, RelativeLayout}
import chat.tox.antox.R
import chat.tox.antox.activities.MainActivity
import chat.tox.antox.theme.ThemeManager
import com.astuetz.PagerSlidingTabStrip
import com.astuetz.PagerSlidingTabStrip.CustomTabProvider
import com.balysv.materialripple.MaterialRippleLayout

class LeftPaneFragment extends Fragment {

  var pager: ViewPager = _

  class LeftPagerAdapter(fm: FragmentManager) extends FragmentPagerAdapter(fm) with CustomTabProvider {

    // modified by Gong
    //val ICONS: Array[Int] = Array(R.drawable.ic_chat_white_24dp, R.drawable.ic_person_white_24dp)
    val ICONS: Array[Int] = Array(R.drawable.ic_chat_white_24dp, R.drawable.ic_person_white_24dp, R.drawable.ic_swipe_like)
    // modified by Gong

    override def getCustomTabView(parent: ViewGroup, position: Int): View = {
      //hack to center the image only for left pane
      // modified by Gong
      val params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
      // modified by Gong
      params.addRule(RelativeLayout.CENTER_HORIZONTAL)
      params.addRule(RelativeLayout.CENTER_VERTICAL)

      //disable the material ripple layout on pre-honeycomb devices
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
        val customTabLayout: FrameLayout = LayoutInflater.from(getActivity).inflate(R.layout.custom_tab_old, parent, false).asInstanceOf[FrameLayout]
        val imageView = customTabLayout.findViewById(R.id.image).asInstanceOf[ImageView]
        imageView.setImageResource(ICONS(position))
        imageView.setLayoutParams(params)
        return customTabLayout
      } else {
        val materialRippleLayout: MaterialRippleLayout = LayoutInflater.from(getActivity).inflate(R.layout.custom_tab, parent, false).asInstanceOf[MaterialRippleLayout]
        val imageView = materialRippleLayout.findViewById(R.id.image)
        imageView.asInstanceOf[ImageView].setImageResource(ICONS(position))
        imageView.setLayoutParams(params)
        return materialRippleLayout
      }

      null
    }

    override def getPageTitle(position: Int): CharSequence = {
      position match {
        case 0 => return "Recent"
          // modified by Gong
        //case _ => return "Contacts"
        case 1 => return "Contacts"
        case _ => return "SwipeEval"
          // modified by Gong
      }

      null
    }

    override def getItem(pos: Int): Fragment = pos match {
      case 0 => new RecentFragment()
        // modified by Gong
      //case _ => new ContactsFragment()
      case 1 => new ContactsFragment()
      case _ => new EvalFragment()
        // modified by Gong
    }

    override def getCount: Int = 3
  }


  override def onSaveInstanceState(outState: Bundle): Unit = {
    super.onSaveInstanceState(outState)
    if (pager != null) outState.putInt("tab_position", pager.getCurrentItem)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val thisActivity = this.getActivity.asInstanceOf[MainActivity]
    val actionBar = thisActivity.getSupportActionBar
    ThemeManager.applyTheme(thisActivity, actionBar)

    val rootView = inflater.inflate(R.layout.fragment_leftpane, container, false)
    pager = rootView.findViewById(R.id.pager).asInstanceOf[ViewPager]
    val tabs = rootView.findViewById(R.id.pager_tabs).asInstanceOf[PagerSlidingTabStrip]

    pager.setAdapter(new LeftPagerAdapter(getFragmentManager))

    val defaultViewPagerTab = 1
    pager.setCurrentItem(Option(savedInstanceState)
      .map(_.getInt("tab_position", defaultViewPagerTab))
      .getOrElse(defaultViewPagerTab)) // start on contacts view by default

    tabs.setViewPager(pager)
    tabs.setBackgroundColor(ThemeManager.primaryColor)

    rootView
  }
}
