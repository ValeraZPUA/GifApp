package com.example.gifapp.models.dataManager

import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.example.gifapp.utils.Utils
import com.example.gifapp.api.ApiInterface
import com.example.gifapp.api.models.gifs.gifItem.GifItem
import com.example.gifapp.db.AppDatabase
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.stateRepository.IStateRepository
import com.example.gifapp.models.stateRepository.StateRepository
import com.example.gifapp.utils.NetworkChecker
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject constructor(private val apiInterface: ApiInterface,
                                      private val database: AppDatabase,
                                      private val utils: Utils,
                                      private val networkChecker: NetworkChecker) {

    private lateinit var dirPath: String
    private val stateRepository: IStateRepository = StateRepository()
    private lateinit var iDataManager: IDataManager

    private var getGifsDisposable: Disposable? = null
    private var addGifListToDBDisposable: Disposable? = null
    private var saveToInternalStorageDisposable: Disposable? = null

    fun initRequiredData(dirPathToDownload: String, iDataManager: IDataManager) {
        dirPath = dirPathToDownload
        this.iDataManager = iDataManager
    }

    fun getGifList(): ArrayList<GifItemEntity> {
        return stateRepository.getGifList()
    }

    fun getIsInternetConnected(): Boolean {
        return networkChecker.getIsInternetConnected()
    }

    fun getGifs(keyWord: String?, offset: Int?, isInternetConnected: Boolean) {
        if (keyWord != null) {
            stateRepository.setKeyWord(keyWord)
            stateRepository.clearGifList()
        }

        if (isInternetConnected) {
            getGifsDisposable = apiInterface
                .getGifs(keyWord ?: stateRepository.getKeyWord(), offset ?: stateRepository.getOffset())
                .map { convertToDBEntity(it.data)}
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ gifList -> checkDeletedGifs(gifList) },
                           { error -> parseError(error) } )
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

    private fun checkDeletedGifs(gifList: ArrayList<GifItemEntity>) {
            val gifIdList: ArrayList<String> = arrayListOf()
                gifList.forEach { gifIdList.add(it.id) }

            database
                .gifItemDao()
                .getDeletedGifIdsList(gifIdList, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : DisposableSingleObserver<List<String>>() {
                    override fun onSuccess(deletedGifIdList: List<String>) {
                        addGifListToDB(gifList)
                        stateRepository.increaseOffset(gifList.size)

                        for (deletedGifId in deletedGifIdList) {
                            gifList.find { it.id == deletedGifId }?.is_deleted = true
                        }

                        val filteredGifList: ArrayList<GifItemEntity> = gifList.filter { !it.is_deleted } as ArrayList<GifItemEntity>

                        iDataManager.returnGifList(filteredGifList)
                        stateRepository.addGifs(filteredGifList)
                        saveToInternalStorage(filteredGifList)
                    }

                    override fun onError(error: Throwable) {
                        addGifListToDB(gifList)
                        stateRepository.increaseOffset(gifList.size)

                        iDataManager.returnGifList(gifList)
                        stateRepository.addGifs(gifList)
                        saveToInternalStorage(gifList)
                    }
                })
    }

    private fun addGifListToDB(gifList: ArrayList<GifItemEntity>) {
        if (gifList.isNotEmpty()) {
            addGifListToDBDisposable = Observable
                .just(gifList)
                .observeOn(Schedulers.io())
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
        saveToInternalStorageDisposable = Observable
            .fromCallable {
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
            .subscribeOn(Schedulers.io())
            .subscribe()
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
                                iDataManager.returnGifList(gifList)
                                stateRepository.addGifs(gifList)
                            }

                        }

                        override fun onError(e: Throwable) {
                            if (gifList.lastIndex == i) {
                                iDataManager.returnGifList(gifList)
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
            .fromCallable {
                database.gifItemDao().setGifDeleted(true, gifId)

                File("$dirPath$gifId.gif").also {
                    if (it.exists())
                        it.delete()
                }

                stateRepository.deleteGifById(gifId)
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun stopAllRxRequests() {
        if (!utils.isDisposed(addGifListToDBDisposable)) {
            addGifListToDBDisposable?.dispose()
        }
        if (!utils.isDisposed(getGifsDisposable)) {
            getGifsDisposable?.dispose()
        }
        if (!utils.isDisposed(saveToInternalStorageDisposable)) {
            saveToInternalStorageDisposable?.dispose()
        }
        if (!utils.isDisposed(networkChecker.getNetworkDisposable())) {
            networkChecker.getNetworkDisposable()
        }
    }
}
