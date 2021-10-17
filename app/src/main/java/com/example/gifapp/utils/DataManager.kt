package com.example.gifapp.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.example.gifapp.App
import com.example.gifapp.api.ApiInterface
import com.example.gifapp.api.models.gifs.gifItem.GifItem
import com.example.gifapp.db.AppDatabase
import com.example.gifapp.db.entities.GifItemEntity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@SuppressLint("CheckResult")
class DataManager(private val _gifsData: MutableLiveData<ArrayList<GifItemEntity>>,
                  private val dirPath: String) {

    @Inject
    lateinit var apiInterface: ApiInterface
    @Inject
    lateinit var database: AppDatabase
    @Inject
    lateinit var stateRepository: StateRepository

    init {
        Log.d("tag22", "init data manager")
        App.appComponent.inject(this)
    }

    fun getGifList(): ArrayList<GifItemEntity> {
        Log.d("tag22", "getGifList: ")
        return stateRepository.getGigList()
    }

    fun getGifs(keyWord: String?, offset: Int?) {
        if (keyWord != null) {
            stateRepository.setKeyWord(keyWord)
            stateRepository.getGigList().clear()
        }

        if (stateRepository.getIsInternetConnected()) {

            Log.d("tag22", "getGifs, REQUEST: ${keyWord ?: stateRepository.getKeyWord()} ${offset ?: stateRepository.getOffset()}")

            apiInterface
                .getGifs(keyWord ?: stateRepository.getKeyWord(), offset ?: stateRepository.getOffset())
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

    private fun addGifListToDB(gifList: ArrayList<GifItemEntity>) {
        if (gifList.isNotEmpty()) {
            Observable
                .just(gifList)
                .subscribeOn(Schedulers.io())
                .subscribe( { gifEntityList -> database.gifItemDao().insertGifs(gifEntityList) }, { error -> parseError(error) })
        }
    }

    private fun convertToDBEntity(gifList: ArrayList<GifItem>): ArrayList<GifItemEntity> {
        val gifListEntity: ArrayList<GifItemEntity> = arrayListOf()
        gifList.forEach {
            gifListEntity.add(GifItemEntity(it.id, it.title, it.images.original["url"]!!, false))
        }
        return gifListEntity
    }

    private fun saveToInternalStorage(gifList: ArrayList<GifItemEntity>) {

        Observable
            .just(gifList)
            .subscribeOn(Schedulers.io())
            .subscribe {
                gifList.forEach {
                    PRDownloader
                        .download(it.image_url, dirPath, "${it.id}.gif")
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

    private fun addGifList(gifList: ArrayList<GifItemEntity>) {
        if (gifList.isNotEmpty()) {
            gifList.forEach {
                database
                    .gifItemDao()
                    .isGifDeleted(it.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableSingleObserver<Boolean>() {
                        override fun onSuccess(isDeleted: Boolean) {
                            if (isDeleted) {
                                it.is_deleted = true
                            }

                            if (gifList.last().id == it.id) {
                                _gifsData.postValue(gifList)
                                stateRepository.addGifs(gifList)
                            }
                        }

                        override fun onError(e: Throwable) { }
                    })
            }
        }
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