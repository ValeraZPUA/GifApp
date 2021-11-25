package com.example.gifapp.utils

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class NetworkChecker @Inject constructor() {

    private var isInterConnected = true

    private val networkDisposable: Disposable = ReactiveNetwork
        .observeInternetConnectivity()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::setIsInternetConnected)

    private fun setIsInternetConnected(isConnected: Boolean) {
        isInterConnected = isConnected
    }

    fun getIsInternetConnected(): Boolean {
        return isInterConnected
    }

    fun getNetworkDisposable(): Disposable {
        return networkDisposable
    }
}