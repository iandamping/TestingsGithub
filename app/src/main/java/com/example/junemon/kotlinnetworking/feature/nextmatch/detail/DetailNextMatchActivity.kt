package com.example.junemon.kotlinnetworking.feature.nextmatch.detail

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.junemon.kotlinnetworking.MainApplication
import com.example.junemon.kotlinnetworking.R
import com.example.junemon.kotlinnetworking.databases.DatabaseModel
import com.example.junemon.kotlinnetworking.databases.database
import com.example.junemon.kotlinnetworking.model.DetailNextMatchModel
import com.example.junemon.kotlinnetworking.model.MainModelNextMatch
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail_nextmatch.*
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select

class DetailNextMatchActivity : AppCompatActivity(), DetailNextMatchView {


    private var menuItem: Menu? = null
    private var isFavorite: Boolean = false
    lateinit var presenter: DetailNextMatchPresenter
    lateinit var data: MainModelNextMatch.Event
    lateinit var compositeDisposable: CompositeDisposable
    private var homeBadge: String? = null
    private var awayBadge: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_nextmatch)
        compositeDisposable = CompositeDisposable()
        presenter = DetailNextMatchPresenter(this)
        presenter.onCreate(this)

        val intent: Intent = getIntent()
        data = intent.getParcelableExtra(Integer.toString(R.string.parcel_key))
        getPhotosData(data.idHomeTeam, data.idAwayTeam)
        presenter.acceptData(data)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    private fun setFavorite() {
        favoriteState()
        Observable.just(isFavorite).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe { results ->
                    if (results) {
                        menuItem?.getItem(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_added_to_favorites)
                    } else {
                        menuItem?.getItem(0)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_add_to_favorites)
                    }
                }
    }

    fun getPhotosData(home: String?, away: String?) {

        compositeDisposable.addAll(MainApplication.getFootballEvent.getNextHomeTeamInfo(home)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ results -> initPhotos(results.teams) }))


        compositeDisposable.add(MainApplication.getFootballEvent.getNextAwayTeamInfo(away)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ results -> initAwayPhotos(results.teams) }))
    }

    fun initHomePhotos(data: String) {
        homeBadge = data
    }

    fun initAwayPhotos(data: String) {
        awayBadge = data
    }

    fun initPhotos(data: List<DetailNextMatchModel.Teams>) {
        Observable.just(data).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe { results ->
            initHomePhotos(results.get(0).strTeamBadge)
        }
        Picasso.get().load(data.get(0).strTeamBadge).into(imgHomeNextMatch)
    }

    fun initAwayPhotos(data: List<DetailNextMatchModel.Teams>) {
        Observable.just(data).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe { results ->
            initAwayPhotos(results.get(0).strTeamBadge)
        }
        Picasso.get().load(data.get(0).strTeamBadge).into(imgAwayNextMatch)
    }


    override fun onSuccess(data: MainModelNextMatch.Event) {
        tvDetailDateNextMatch.text = data.dateEvent
        tvHomeTeamDetailNextMatch.text = data.strHomeTeam
        tvAwayTeamDetailNextMatch.text = data.strAwayTeam
        tvAwayMidFieldNextMatch.text = data.strAwayLineupMidfield
        tvHomeMidFieldNextMatch.text = data.strHomeLineupMidfield
        tvAwayDefenseNextMatch.text = data.strAwayLineupDefense
        tvHomeDefenseNextMatch.text = data.strHomeLineupDefense
        tvAwayGoalKeeperNextMatch.text = data.strAwayLineupGoalkeeper
        tvHomeGoalKeeperNextMatch.text = data.strHomeLineupGoalkeeper
        tvAwayForwardNextMatch.text = data.strAwayLineupForward
        tvHomeForwardNextMatch.text = data.strHomeLineupForward
        tvAwayGoalsNextMatch.text = data.strAwayGoalDetails
        tvHomeGoalsNextMatch.text = data.strHomeGoalDetails
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        menuItem = menu
        setFavorite()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.add_to_favorite -> {
                if (isFavorite) removeFromFavorite() else addToFav(data, awayBadge, homeBadge)

                isFavorite = !isFavorite
                setFavorite()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun favoriteState() {
        database.use {
            val result = select(DatabaseModel.TABLE_FAVORITE)
                    .whereArgs("(TEAM_HOME_ID = {data})",
                            "data" to data.idHomeTeam!!)
            val favorite = result.parseList(classParser<DatabaseModel>())
            if (!favorite.isEmpty()) isFavorite = true
        }
    }

    private fun removeFromFavorite() {
        try {
            database.use {
                delete(DatabaseModel.TABLE_FAVORITE, "(TEAM_HOME_ID = {data})",
                        "data" to data.idHomeTeam!!)
            }
        } catch (e: SQLiteConstraintException) {
        }
    }

    private fun addToFav(data: MainModelNextMatch.Event, awayTeam: String?, homeTeam: String?) {
        try {
            database.use {
                insert(DatabaseModel.TABLE_FAVORITE,
                        DatabaseModel.TEAM_HOME_ID to data.idHomeTeam,
                        DatabaseModel.TEAM_AWAY_ID to data.idAwayTeam,
                        DatabaseModel.TEAM_HOME_NAME to data.strHomeTeam,
                        DatabaseModel.TEAM_AWAY_NAME to data.strAwayTeam,
                        DatabaseModel.TEAM_HOME_SCORE to data.intHomeScore,
                        DatabaseModel.TEAM_AWAY_SCORE to data.intAwayScore,
                        DatabaseModel.HOME_GOAL_DETAILS to data.strHomeGoalDetails,
                        DatabaseModel.AWAY_GOAL_DETAILS to data.strAwayGoalDetails,
                        DatabaseModel.HOME_GOALKEPEER to data.strHomeLineupGoalkeeper,
                        DatabaseModel.AWAY_GOALKEPEER to data.strAwayLineupGoalkeeper,
                        DatabaseModel.HOME_DEFENSE to data.strHomeLineupDefense,
                        DatabaseModel.AWAY_DEFENSE to data.strAwayLineupDefense,
                        DatabaseModel.HOME_MIDFIELD to data.strHomeLineupMidfield,
                        DatabaseModel.AWAY_MIDFIELD to data.strAwayLineupMidfield,
                        DatabaseModel.HOME_FORWARD to data.strHomeLineupForward,
                        DatabaseModel.AWAY_FORWARD to data.strAwayLineupForward,
                        DatabaseModel.DATE_EVENT to data.dateEvent,
                        DatabaseModel.TEAM_AWAY_BADGE to awayTeam,
                        DatabaseModel.TEAM_HOME_BADGE to homeTeam
                )


            }
        } catch (e: SQLiteConstraintException) {
        }

    }

    override fun onFailed(message: String) {
    }

    override fun initView() {
    }

}