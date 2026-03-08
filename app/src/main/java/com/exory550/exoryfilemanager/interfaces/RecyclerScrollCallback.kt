package com.exory550.exoryfilemanager.interfaces

interface RecyclerScrollCallback {
    fun onScrollUp()
    fun onScrollDown()
    fun onScrollStateChanged(scrolling: Boolean)
    fun onScrollPosition(firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int)
    fun onScrollToTop()
    fun onScrollToBottom()
    fun onFastScrollStarted()
    fun onFastScrollFinished()
}
