package com.example.gifapp.models

import com.example.gifapp.db.AppDatabase
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.dataManager.IDataManager
import com.example.gifapp.utils.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject

class DataBaseHelper @Inject constructor(private val utils: Utils,
                                         private val database: AppDatabase) {

    private lateinit var iDataManager: IDataManager.Helper

    private var addGifListToDBDisposable: Disposable? = null
    private var saveToInternalStorageDisposable: Disposable? = null

    fun initInterface(iDataManager: IDataManager.Helper) {
        this.iDataManager = iDataManager
    }

    fun getGifList(keyWord: String) {
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
    }

    fun addGifList(gifList: ArrayList<GifItemEntity>) {
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
                                iDataManager.updateStateRepositoryList(gifList)
                            }

                        }

                        override fun onError(e: Throwable) {
                            if (gifList.lastIndex == i) {
                                iDataManager.updateStateRepositoryList(gifList)
                            }
                        }
                    })
            }
        }
    }

    fun getDeletedGifIdsList(gifList: ArrayList<GifItemEntity>, gifIdList: java.util.ArrayList<String>) {
        database
                .gifItemDao()
                .getDeletedGifIdsList(gifIdList, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableSingleObserver<List<String>>() {
                    override fun onSuccess(deletedGifIdList: List<String>) {
                        addGifListToDB(gifList)
                        iDataManager.increaseStateRepositoryOffset(gifList.size)

                        for (deletedGifId in deletedGifIdList) {
                            gifList.find { it.id == deletedGifId }?.is_deleted = true
                        }

                        val filteredGifList: ArrayList<GifItemEntity> = gifList.filter { !it.is_deleted } as ArrayList<GifItemEntity>

                        iDataManager.returnGifList(filteredGifList)
                        iDataManager.updateStateRepositoryList(filteredGifList)
                        saveToInternalStorage(filteredGifList)
                    }

                    override fun onError(error: Throwable) {
                        addGifListToDB(gifList)
                        iDataManager.increaseStateRepositoryOffset(gifList.size)

                        iDataManager.returnGifList(gifList)
                        iDataManager.updateStateRepositoryList(gifList)
                        saveToInternalStorage(gifList)
                    }
                })
    }

    private fun saveToInternalStorage(gifList: ArrayList<GifItemEntity>) {
        saveToInternalStorageDisposable = Observable
            .fromCallable {
                gifList.forEach {
                    database
                        .gifItemDao()
                        .isGifDeleted(it.id)
                        .subscribe(object : DisposableSingleObserver<Boolean>() {
                            override fun onSuccess(isDeleted: Boolean) {
                                if (!isDeleted) {
                                    iDataManager.downloadGifTOInternalStorage(it.image_url,"${it.id}.gif")
                                }
                            }

                            override fun onError(e: Throwable) {
                                iDataManager.downloadGifTOInternalStorage(it.image_url,"${it.id}.gif")
                            }
                        })
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    private fun addGifListToDB(gifList: ArrayList<GifItemEntity>) {
        if (gifList.isNotEmpty()) {
            addGifListToDBDisposable = Observable
                .just(gifList)
                .observeOn(Schedulers.io())
                .subscribe( { gifEntityList -> database.gifItemDao().insertGifs(gifEntityList) }, { error -> parseError(error) })
        }
    }

    private fun parseError(error: Throwable) {
        //TODO
    }

    fun setDeleted(dirPath: String, gifId: String) {
        Observable
            .fromCallable {
                database.gifItemDao().setGifDeleted(true, gifId)

                File("$dirPath$gifId.gif").also {
                    if (it.exists())
                        it.delete()
                }

                iDataManager.deleteGifFromStateRepositoryById(gifId)
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun stopAllRxRequests() {
        if (!utils.isDisposed(addGifListToDBDisposable)) {
            addGifListToDBDisposable?.dispose()
        }
        if (!utils.isDisposed(saveToInternalStorageDisposable)) {
            saveToInternalStorageDisposable?.dispose()
        }
    }
}