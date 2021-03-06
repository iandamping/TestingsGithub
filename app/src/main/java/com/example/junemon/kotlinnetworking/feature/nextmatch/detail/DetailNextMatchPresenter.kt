package com.example.junemon.kotlinnetworking.feature.nextmatch.detail

import android.content.Context
import com.example.junemon.kotlinnetworking.base.BasePresenter
import com.example.junemon.kotlinnetworking.model.MainModelNextMatch
import io.reactivex.disposables.CompositeDisposable

class DetailNextMatchPresenter(val mView: DetailNextMatchView) : BasePresenter {

    lateinit var ctx: Context
    lateinit var compositeDisposable: CompositeDisposable

    override fun getContext(): Context {
        return ctx
    }

    override fun onCreate(context: Context) {
        this.ctx = context
        mView.initView()
        compositeDisposable = CompositeDisposable()
    }

    fun acceptData(homeTeam: MainModelNextMatch.Event) {
        mView.onSuccess(homeTeam)

    }

    override fun onPause() {
        if (compositeDisposable != null && compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }


    override fun onDestroy() {
        if (compositeDisposable != null && compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }
}