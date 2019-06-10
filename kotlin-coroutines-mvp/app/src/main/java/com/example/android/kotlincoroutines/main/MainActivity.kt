package com.example.android.kotlincoroutines.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.example.android.kotlincoroutines.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : MvpAppCompatActivity(), MainView {

    @InjectPresenter
    lateinit var mainPresenter: MainPresenter

    override fun onDateReceived(text: String?) {
        showText("onDateReceived: $text")
    }

    override fun showNetworkError(errorText: Throwable, networkRetry: NetworkRetry) {
        showText("showNetworkError: $errorText")
    }

    override fun showProgress() {
            progressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
           progressBar.visibility = View.GONE
    }

    override fun showErrorDialog(errorText: String) {
        showText("showErrorDialog: $errorText")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       buttonEmptyTask.setOnClickListener { mainPresenter.getOkWithOutProgressBar() }
        buttonTaskWithProgress.setOnClickListener { mainPresenter.getOk() }
        buttonCancelTask.setOnClickListener { mainPresenter.cancelAllJobs()}
    }

    private fun showText(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}