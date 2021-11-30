package com.example.gifapp.models.dataManager

import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.example.gifapp.utils.Utils
import com.example.gifapp.api.ApiInterface
import com.example.gifapp.api.models.gifs.gifItem.GifItem
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.dataBaseHelper.DataBaseHelper
import com.example.gifapp.models.stateRepository.IStateRepository
import com.example.gifapp.models.stateRepository.StateRepository
import com.example.gifapp.utils.NetworkChecker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject constructor(private val apiInterface: ApiInterface,
                                      private val utils: Utils,
                                      private val networkChecker: NetworkChecker,
                                      private val dataBaseHelper: DataBaseHelper): IDataManager.DataBaseHelper {

    private lateinit var dirPath: String
    private val stateRepository: IStateRepository = StateRepository()
    private lateinit var iDataManager: IDataManager.ViewModel

    private var getGifsDisposable: Disposable? = null

    fun initRequiredData(dirPathToDownload: String, iDataManager: IDataManager.ViewModel) {
        dirPath = dirPathToDownload
        this.iDataManager = iDataManager
        dataBaseHelper.initInterface(this)
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
                dataBaseHelper.getGifList(keyWord)
            } else {
                dataBaseHelper.addGifList(ArrayList())
            }
        }
    }

    private fun checkDeletedGifs(gifList: ArrayList<GifItemEntity>) {
            val gifIdList: ArrayList<String> = arrayListOf()
                gifList.forEach { gifIdList.add(it.id) }

        dataBaseHelper.getDeletedGifIdsList(gifList, gifIdList)
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

    fun setDeleted(gifId: String) {
        dataBaseHelper.setDeleted(dirPath, gifId)
    }

    fun stopAllRxRequests() {
        dataBaseHelper.stopAllRxRequests()
        networkChecker.stopNetworkChecking()
        if (!utils.isDisposed(getGifsDisposable)) {
            getGifsDisposable?.dispose()
        }
    }

    override fun updateStateRepositoryList(gifList: ArrayList<GifItemEntity>) {
        stateRepository.addGifs(gifList)
    }

    override fun returnGifList(gifList: ArrayList<GifItemEntity>) {
        iDataManager.returnGifList(gifList)
    }

    override fun increaseStateRepositoryOffset(offsetSize: Int) {
        stateRepository.increaseOffset(offsetSize)
    }

    override fun downloadGifTOInternalStorage(imageUrl: String, imageTitle: String) {
        PRDownloader
            .download(imageUrl, dirPath, imageTitle)
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() { }

                override fun onError(error: Error?) { }
            })
    }

    override fun deleteGifFromStateRepositoryById(gifId: String) {
        stateRepository.deleteGifById(gifId)
    }
}
