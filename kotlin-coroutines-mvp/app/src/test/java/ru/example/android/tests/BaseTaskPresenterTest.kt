package ru.example.android.tests

import com.example.android.kotlincoroutines.main.BaseTaskPresenter
import com.example.android.kotlincoroutines.main.HandlerCall
import com.example.android.kotlincoroutines.main.MainView
import com.example.android.kotlincoroutines.main.`MainView$$State`
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.setMain

import okhttp3.Request
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors

@RunWith(JUnit4::class)
class BaseTaskPresenterTest {

    private lateinit var mPresenter: TestPresenter

    private val mView: MainView = mock(MainView::class.java)

    private val mViewState: `MainView$$State` = mock(`MainView$$State`::class.java)
    private val threadExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()


    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(threadExecutor)
        mPresenter = TestPresenter()
        mPresenter.attachView(mView)
        mPresenter.setViewState(mViewState)
    }

    @Test
    fun testNoActionsWithView() {
        verifyNoMoreInteractions(mViewState)
    }

    @Test
    fun testStartTask() {
        mPresenter.startTaskClubAccountWithRetry(TestCall())
        sleep(800)
        verify(mView).showProgress()
        verify(mView).hideProgress()
        verify(mView).onDateReceived("success")
    }

    @Test
    fun testStartTaskFail() {
        val exception = Exception("Test Exception")
        val exceptionCall = object : TestCall() {
            override fun execute(): Response<String> {
                sleep(100)
                throw exception
            }
        }

        var returnedException: Throwable? = null
        mPresenter.startTaskClubAccount(exceptionCall) { returnedException = it }

        sleep(800)

        verify(mView).showProgress()
        verify(mView).hideProgress()
        assert(exception.message == returnedException!!.message)
    }

    @Test
    fun testStartTaskCancel() {
        mPresenter.startTaskClubAccountWithRetry(TestCall())
        sleep(100)
        mPresenter.onDestroy()
        sleep(700)
        verify(mView).showProgress()
        verifyNoMoreInteractions(mView)
    }

    fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    inner class TestPresenter : BaseTaskPresenter<MainView>() {
        fun startTaskClubAccountWithRetry(call: Call<String>) {
            call.startTaskWithRetry(mView, mView::onDateReceived)
        }

        fun startTaskClubAccount(call: Call<String>, errorReceived: ((Throwable) -> Unit)) {
            mPresenter.startTask(
                    HandlerCall(call),
                    mView::onDateReceived,
                    errorReceived,
                    mView::showProgress,
                    mView::hideProgress)
        }
    }

    open inner class TestCall : Call<String> {

        override fun cancel() {
            //To change body of created functions use File | Settings | File Templates.
        }

        override fun enqueue(p0: Callback<String>) {
        }

        override fun isExecuted(): Boolean {
            return false
        }

        override fun clone(): Call<String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isCanceled(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun execute(): Response<String> {
            sleep(500)
            return Response.success("success")
        }

        override fun request(): Request {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}