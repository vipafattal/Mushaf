package com.brilliancesoft.mushaf.utils.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*

/**
 * Created by ${User} on ${Date}
 */


inline fun <T> LiveData<T>.observer(owner: LifecycleOwner, crossinline doOnObserver: (T) -> Unit) =
    observe(owner, Observer {
        doOnObserver(it)
    })


inline fun <reified  T : ViewModel> Fragment.viewModelOf() =
    ViewModelProvider(activity!!).get(T::class.java)


inline fun <reified  T : ViewModel> AppCompatActivity.viewModelOf() =
    ViewModelProvider(this).get(T::class.java)

fun <T : ViewModel> AppCompatActivity.viewModelOf(
    viewModelClass: Class<T>,
    factoryViewModel: ViewModelProvider.Factory
) =
    ViewModelProvider(this, factoryViewModel).get(viewModelClass)

fun <T : ViewModel> Fragment.viewModelOf(
    viewModelClass: Class<T>,
    factoryViewModel: ViewModelProvider.Factory
) =
    ViewModelProvider(this.activity!!, factoryViewModel).get(viewModelClass)
