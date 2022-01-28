package com.example.gifapp.models

import com.example.gifapp.api.ApiInterface
import com.example.gifapp.api.models.gifs.gifItem.GifItem
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.dataManager.IDataManager
import com.example.gifapp.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class NetworkHelper @Inject constructor(
    private val utils: Utils,
    private val apiInterface: ApiInterface): DataRepo {

    private var getGifsDisposable: Disposable? = null
    private lateinit var iDataManager: IDataManager.Helper

    fun initInterface(iDataManager: IDataManager.Helper) {
        this.iDataManager = iDataManager
    }

    override fun getGifList(keyWord: String?, offset: Int?) {
        getGifsDisposable = apiInterface
            .getGifs(keyWord!!, offset!!)
            .map { convertToDBEntity(it.data)}
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ gifList -> iDataManager.checkDeletedGifs(gifList) },
                       { error -> parseError(error) } )
    }

    private fun convertToDBEntity(gifList: ArrayList<GifItem>): ArrayList<GifItemEntity> {
        val gifListEntity: ArrayList<GifItemEntity> = arrayListOf()
        gifList.forEach {
            gifListEntity.add(GifItemEntity(it.id, it.title, it.images.original["url"]!!, false))
        }
        return gifListEntity
    }

    private fun parseError(error: Throwable) {
        //TODO
    }

    fun stopAllRxRequests() {
        if (!utils.isDisposed(getGifsDisposable)) {
            getGifsDisposable?.dispose()
        }
    }
}