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


fun <T: ViewModel> Fragment.viewModelOf(viewModelClass: Class<T>)=
    ViewModelProviders.of(activity!!).get(viewModelClass)


fun <T:ViewModel> AppCompatActivity.viewModelOf(viewModelClass: Class<T>)=
    ViewModelProviders.of(this).get(viewModelClass)

fun <T:ViewModel> AppCompatActivity.viewModelOf(viewModelClass: Class<T>,factoryViewModel:ViewModelProvider.Factory)=
        ViewModelProviders.of(this,factoryViewModel).get(viewModelClass)

fun <T:ViewModel> Fragment.viewModelOf(viewModelClass: Class<T>,factoryViewModel:ViewModelProvider.Factory)=
        ViewModelProviders.of(this.activity!!,factoryViewModel).get(viewModelClass)
