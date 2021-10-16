package com.example.gifapp.ui.fragments.gifLIst

import android.annotation.SuppressLint
import android.app.Application
import android.database.DataSetObservable
import android.util.Log
import androidx.lifecycle.*
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.example.gifapp.App
import com.example.gifapp.api.ApiInterface
import com.example.gifapp.api.models.gifs.gifItem.GifItem
import com.example.gifapp.db.AppDatabase
import com.example.gifapp.db.entities.GifItemEntity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@SuppressLint("CheckResult")
class GifListViewModel(private val app: Application, state: SavedStateHandle) : AndroidViewModel(app), LifecycleObserver {

    val TAG = "tag22"

    private var previousKeyWord: String = ""
    private var offset: Int = 0

    private var isInterConnected = false

    private val _gifsData = MutableLiveData<ArrayList<GifItemEntity>>()
    val gifsData: LiveData<ArrayList<GifItemEntity>>
        get() = _gifsData

    @Inject
    lateinit var apiInterface: ApiInterface
    @Inject
    lateinit var database: AppDatabase

    init {
        App.appComponent.inject(this)

        ReactiveNetwork
            .observeInternetConnectivity()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ isConnected -> isInterConnected = isConnected}
    }

    @SuppressLint("CheckResult")
    fun getGifs(keyWord: String?, offset: Int?) {

        Log.d(TAG, "getGifs, isInterConnected: $isInterConnected")
        if (keyWord != null) {
            previousKeyWord = keyWord
        }

        if (offset == 0) {
            this.offset = 0
        }


        Log.d(TAG, "keyWord: $keyWord")
        if (isInterConnected) {
            apiInterface
                .getGifs(keyWord ?: previousKeyWord, offset ?: this.offset)
                .map { it -> convertToDBEntity(it.data)}
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ gifList -> addGifList(gifList)
                    saveToInternalStorage(gifList)
                    addGifListToDB(gifList)},
                    { error -> parseError(error) })
        } else {
            if (keyWord != null && keyWord.isNotBlank()) {
                database
                    .gifItemDao()
                    .searchInDB("%$keyWord%")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableSingleObserver<List<GifItemEntity>>() {
                        override fun onSuccess(gifEntityList: List<GifItemEntity>) {
                            addGifList(ArrayList(gifEntityList))
                        }

                        override fun onError(p0: Throwable) {

                        }

                    })
            }


        }
    }

    @SuppressLint("CheckResult")
    private fun addGifListToDB(gifList: ArrayList<GifItemEntity>) {
        if (gifList.isNotEmpty()) {
            Observable
                .just(gifList)
                //.map(this::convertToDBEntity)
                .subscribeOn(Schedulers.io())
                .subscribe( { gifEntityList -> database.gifItemDao().insertGifs(gifEntityList) }, { error -> parseError(error) })
        }
    }

    private fun convertToDBEntity(gifList: ArrayList<GifItem>): ArrayList<GifItemEntity> {
        val gifListEntity: ArrayList<GifItemEntity> = arrayListOf()
        gifList.forEach {
            //gifListEntity.add(GifItemEntity(it.id, it.title, app.cacheDir.absolutePath + "/gif_app_cache/${it.id}.gif", false))
            gifListEntity.add(GifItemEntity(it.id, it.title, it.images.original["url"]!!, false))
        }
        return gifListEntity
    }

    private fun saveToInternalStorage(gifList: ArrayList<GifItemEntity>) {

        viewModelScope.launch {
            if (gifList.isNotEmpty()) {

                gifList.forEach {
                    PRDownloader
                        .download(it.image_url, app.cacheDir.absolutePath + "/gif_app_cache/", "${it.id}.gif")
                        .build()
                        .start(object : OnDownloadListener {
                            override fun onDownloadComplete() {
                            }

                            override fun onError(error: Error?) {
                            }
                        })
                }
            }

        }
    }

    private fun addGifList(gifList: ArrayList<GifItemEntity>) {
            offset += gifList.size

        if (gifList.isNotEmpty()) {
            Log.d(TAG, "addGifList: ")
            gifList.forEach {
                database
                    .gifItemDao()
                    .isGifDeleted(it.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableSingleObserver<Boolean>() {
                        override fun onSuccess(isDeleted: Boolean) {


                            if (isDeleted) {
                                Log.d(TAG, "onSuccess: ${it.is_deleted}")
                                it.is_deleted = true
                            }

                            if (gifList.last().id == it.id) {
                                Log.d(TAG, "onSuccess: SET LIST")
                                _gifsData.postValue(gifList)
                            }
                        }

                        override fun onError(e: Throwable) {

                        }

                    })
            }
        }


        //_gifsData.value = gifList
        

    }

    private fun parseError(error: Throwable) {
        //TODO
        Log.d("tag22", error.message.toString())
    }

    fun setDeleted(gifId: String) {
        Observable
            .just(gifId)
            .subscribeOn(Schedulers.io())
            .subscribe {
                database.gifItemDao().setGifDeleted(true, it)
            }

    }

}