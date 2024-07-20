package com.HrConsole.tv.official.console.premium

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding

interface RecyclerViewInterface<T:ViewDataBinding> {

  fun getViewBinding(viewGroup: ViewGroup,viewType:Int): T
  fun bindView(viewBind: T, position:Int)
  fun getListCount():Int
}