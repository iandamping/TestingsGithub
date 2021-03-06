package com.example.junemon.kotlinnetworking.feature.team.detail.player.detail

import com.example.junemon.kotlinnetworking.base.BaseView
import com.example.junemon.kotlinnetworking.model.AllPlayer

interface PlayerDetailView : BaseView {
    fun onGetData(allData: AllPlayer.Player)
    fun failGetData(message: String)
}