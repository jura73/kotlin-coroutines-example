package com.example.android.kotlincoroutines.main

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import java.util.ArrayList

open class BaseTaskPresenter<View : MvpView> : MvpPresenter<View>(), NetworkRetry {
    private var failedBlock: (() -> Unit)? = null

    private var startedJobs: ArrayList<Job> = ArrayList()

    fun <T> Call<T>.builderJob(): BuilderJob<T> {
        return BuilderJob(this)
    }

    inner class BuilderJob<T>(private val call: Call<T>) {
        private var errorHandleFun: ((Throwable) -> Unit)? = null
        private var beforeFun: (() -> Unit)? = null
        private var afterFun: (() -> Unit)? = null

        fun setErrorHandleFun(errorHandle: (Throwable) -> Unit): BuilderJob<T> {
            errorHandleFun = errorHandle
            return this
        }

        fun setBeforeFun(beforeFun: () -> Unit): BuilderJob<T> {
            this.beforeFun = beforeFun
            return this
        }

        fun setAfterFun(afterFun: () -> Unit): BuilderJob<T> {
            this.afterFun = afterFun
            return this
        }

        fun startTaskWithRetry(resultHandleFun: (T?) -> Unit) {
            startTaskWithRetry(HandlerCall(call), resultHandleFun, errorHandleFun, beforeFun, afterFun)
        }

        fun onResponseWithRetry(resultHandleFun: (Response<T?>?) -> Unit) {
            startTask(HandlerCallResponse(call), resultHandleFun, errorHandleFun, beforeFun, afterFun)
        }
    }

    protected fun <T> Call<T>.startTaskWithRetry(baseMvpView: BaseMvpView, resultHandleFun: (T?) -> Unit) {
        startTaskWithRetry(
                HandlerCall(this),
                resultHandleFun,
                {
                    baseMvpView.showNetworkError(it, this@BaseTaskPresenter)
                },
                baseMvpView::showProgress,
                baseMvpView::hideProgress)
    }

    private fun <T> startTaskWithRetry(runnableFuture: RunnableFuture<T>, resultHandleFun: ((T?) -> Unit)? = null, errorHandleFun: ((Throwable) -> Unit)? = null, beforeFun: (() -> Unit)? = null, afterFun: (() -> Unit)? = null) {
        val errorHandleWithRetryFun: (Throwable) -> Unit = {
            errorHandleFun?.invoke(it)
            failedBlock = { startTaskWithRetry(runnableFuture, resultHandleFun, errorHandleFun, beforeFun, afterFun) }
        }
        startTask(runnableFuture, resultHandleFun, errorHandleWithRetryFun, beforeFun, afterFun)
    }

    protected fun <T> Call<T>.startTask(resultHandleFun: (T?) -> Unit) {
        startTask(HandlerCall(this), resultHandleFun)
    }

    @UseExperimental(InternalCoroutinesApi::class)
    protected fun <T> startTask(runnableFuture: RunnableFuture<T>, resultReceived: ((T?) -> Unit)? = null, errorReceived: ((Throwable) -> Unit)? = null, beforeFun: (() -> Unit)? = null, afterFun: (() -> Unit)? = null) {
        val newJob = GlobalScope.launch(Dispatchers.Main) {
            beforeFun?.invoke()
            try {
                val result = withContext(Dispatchers.IO) { runnableFuture.run() }
                afterFun?.invoke()
                resultReceived?.invoke(result)
            } catch (e: CancellationException) {
                runnableFuture.cancel()
            } catch (e: Exception) {
                afterFun?.invoke()
                errorReceived?.invoke(e)
            }
        }
        startedJobs.add(newJob)
        newJob.invokeOnCompletion(true) {
            if (it != null) {
                runnableFuture.cancel()
            }
            startedJobs.remove(newJob)
        }
    }

    override fun onRetry() {
        failedBlock?.invoke()
        failedBlock = null
    }

    fun cancelAllJobs(){
        val iterator = startedJobs.iterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            iterator.remove()
            value.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAllJobs()
    }
}