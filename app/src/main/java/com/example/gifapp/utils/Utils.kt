package com.example.gifapp.utils

import io.reactivex.disposables.Disposable
import javax.inject.Inject

class Utils @Inject constructor() {

    fun isDisposed(disposable: Disposable?): Boolean {
        return disposable?.isDisposed == true
    }

}