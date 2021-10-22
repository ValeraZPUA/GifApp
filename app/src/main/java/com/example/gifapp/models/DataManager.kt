package com.example.gifapp.models

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.example.gifapp.api.ApiInterface
import com.example.gifapp.api.models.gifs.gifItem.GifItem
import com.example.gifapp.db.AppDatabase
import com.example.gifapp.db.entities.GifItemEntity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("CheckResult")
class DataManager @Inject constructor(private val apiInterface: ApiInterface,
                                      private val database: AppDatabase) {

    private lateinit var _gifsData: MutableLiveData<ArrayList<GifItemEntity>>
    private lateinit var dirPath: String
    private var stateRepository = StateRepository()

    fun initRequiredData(gifsLivaData: MutableLiveData<ArrayList<GifItemEntity>>, dirPathToDownload: String) {
        _gifsData = gifsLivaData
        dirPath = dirPathToDownload
    }

    fun getGifList(): ArrayList<GifItemEntity> {
        return stateRepository.getGigList()
    }

    fun getIsInternetConnected(): Boolean {
        return stateRepository.getIsInternetConnected()
    }

    fun getGifs(keyWord: String?, offset: Int?, isInternetConnected: Boolean) {
        if (keyWord != null) {
            stateRepository.setKeyWord(keyWord)
            stateRepository.getGigList().clear()
        }

        if (isInternetConnected) {
            apiInterface
                .getGifs(keyWord ?: stateRepository.getKeyWord(), offset ?: stateRepository.getOffset())
                .map { convertToDBEntity(it.data)}
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
                            addGifList(ArrayList())
                        }

                    })
            } else {
                addGifList(ArrayList())
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
                    database
                        .gifItemDao()
                        .isGifDeleted(it.id)
                        .subscribe(object : DisposableSingleObserver<Boolean>() {
                            override fun onSuccess(isDeleted: Boolean) {
                                if (!isDeleted) {
                                    downloadGifTOInternalStorage(it.image_url, dirPath, "${it.id}.gif")
                                }
                            }

                            override fun onError(e: Throwable) {
                                downloadGifTOInternalStorage(it.image_url, dirPath, "${it.id}.gif")
                            }
                        })
                }
            }
    }

    private fun downloadGifTOInternalStorage(imageUrl: String, dirPath: String, fileName: String) {
        PRDownloader
            .download(imageUrl, dirPath, fileName)
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() { }

                override fun onError(error: Error?) { }
            })
    }

    private fun addGifList(gifList: ArrayList<GifItemEntity>) {
        if (gifList.isNotEmpty()) {
            for (i in 0 until gifList.size) {
                database
                    .gifItemDao()
                    .isGifDeleted(gifList[i].id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : DisposableSingleObserver<Boolean>() {
                        override fun onSuccess(isDeleted: Boolean) {
                            if (isDeleted) {
                                gifList[i].is_deleted = true
                            }

                            if (gifList.lastIndex == i) {
                                _gifsData.postValue(gifList)
                                stateRepository.addGifs(gifList)
                            }

                        }

                        override fun onError(e: Throwable) {
                            if (gifList.lastIndex == i) {
                                _gifsData.postValue(gifList)
                                stateRepository.addGifs(gifList)
                            }
                        }
                    })
            }
        }
    }

    private fun parseError(error: Throwable) {
        //TODO
    }

    fun setDeleted(gifId: String) {
        Observable
            .just(gifId)
            .subscribeOn(Schedulers.io())
            .subscribe {
                database.gifItemDao().setGifDeleted(true, it)
            }
        Observable
            .just(gifId)
            .subscribeOn(Schedulers.io())
            .subscribe {
                val file = File("$dirPath$gifId.gif")
                if (file.exists()) {
                    file.delete()
                }
            }
    }
}