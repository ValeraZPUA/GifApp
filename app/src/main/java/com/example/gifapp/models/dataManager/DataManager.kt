package com.example.gifapp.models.dataManager

import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.example.gifapp.db.entities.GifItemEntity
import com.example.gifapp.models.DataBaseHelper
import com.example.gifapp.models.NetworkHelper
import com.example.gifapp.models.stateRepository.IStateRepository
import com.example.gifapp.models.stateRepository.StateRepository
import com.example.gifapp.utils.NetworkChecker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataManager @Inject constructor(private val networkChecker: NetworkChecker,
                                      private val networkHelper: NetworkHelper,
                                      private val dataBaseHelper: DataBaseHelper
): IDataManager.Helper {

    private lateinit var dirPath: String
    private val stateRepository: IStateRepository = StateRepository()
    private lateinit var iDataManager: IDataManager.ViewModel

    fun initRequiredData(dirPathToDownload: String, iDataManager: IDataManager.ViewModel) {
        dirPath = dirPathToDownload
        this.iDataManager = iDataManager
        dataBaseHelper.initInterface(this)
        networkHelper.initInterface(this)
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
            networkHelper.getGifs(keyWord ?: stateRepository.getKeyWord(), offset ?: stateRepository.getOffset())
        } else {
            if (keyWord != null && keyWord.isNotBlank()) {
                dataBaseHelper.getGifList(keyWord)
            } else {
                dataBaseHelper.addGifList(ArrayList())
            }
        }
    }

    override fun checkDeletedGifs(gifList: ArrayList<GifItemEntity>) {
            val gifIdList: ArrayList<String> = arrayListOf()
                gifList.forEach { gifIdList.add(it.id) }

        dataBaseHelper.getDeletedGifIdsList(gifList, gifIdList)
    }

    fun setDeleted(gifId: String) {
        dataBaseHelper.setDeleted(dirPath, gifId)
    }

    fun stopAllRxRequests() {
        dataBaseHelper.stopAllRxRequests()
        networkChecker.stopNetworkChecking()
        networkHelper.stopAllRxRequests()
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
