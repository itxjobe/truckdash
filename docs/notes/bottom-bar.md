# The system bottom bar

- The 280 px bottom bar is not the AOSP navigation bar. It is the vendor CustomerUI layout
  `layout_navi_bar_zxw_default` drawn inside SystemUI (`com.szchoiceway.view.NaviBarView`).
- No setting hides individual buttons (back, recents, volume).
- So the dash hides the system bars while it is showing (immersive mode) and draws its own Home and
  Apps buttons in the same strip (`ui/NavButtons.kt`). Other apps still get the normal vendor bar.
