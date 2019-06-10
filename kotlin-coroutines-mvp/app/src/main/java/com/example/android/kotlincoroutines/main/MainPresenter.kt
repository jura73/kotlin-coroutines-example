package com.example.android.kotlincoroutines.main

import com.arellomobile.mvp.InjectViewState

@InjectViewState
class MainPresenter : BaseTaskPresenter<MainView>() {

    fun getOkWithOutProgressBar() {
        RetrofitApi.service().getOk(3000)
                .startTask {
                    viewState.onDateReceived("Ok")
                }
    }

    fun getOk() {
        RetrofitApi.service().getOk(3000)
                .startTaskWithRetry(viewState) {
                    viewState.onDateReceived("Ok")
                }
    }
}