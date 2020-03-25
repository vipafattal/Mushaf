package com.brilliancesoft.mushaf.utils.extensions

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by ${User} on ${Date}
 */

inline fun <T : Any> Observable<T>.observeOnMainThread(subscribeOn: Scheduler = Schedulers.io(), crossinline doOnNotify: (T) -> Unit): Disposable =
    subscribeOn(subscribeOn)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            doOnNotify.invoke(it)
        }
