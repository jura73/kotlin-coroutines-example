package com.example.android.kotlincoroutines.main

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

interface BaseMvpView : MvpView, ErrorMvpView {
    @StateStrategyType(SkipStrategy::class)
    fun showNetworkError(errorText: Throwable, networkRetry: NetworkRetry)

    fun showProgress()

    fun hideProgress()
}