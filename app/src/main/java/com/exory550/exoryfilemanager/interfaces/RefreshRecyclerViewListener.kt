package com.exory550.exoryfilemanager.interfaces

interface RefreshRecyclerViewListener {
    fun onRefresh()
    fun onRefreshComplete()
    fun onLoadMore()
    fun onLoadMoreComplete()
    fun onRetry()
}
