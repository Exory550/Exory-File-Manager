package com.exory550.exoryfilemanager.interfaces

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

interface MyActionModeCallback {
    fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean
    fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean
    fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean
    fun onDestroyActionMode(mode: ActionMode)
    fun onItemSelected()
    fun onItemUnselected()
}
