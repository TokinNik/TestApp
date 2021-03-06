package com.example.testapp.util

import com.example.testapp.db.entity.Skill.Skill
import com.example.testapp.db.entity.advantage.Advantage
import com.example.testapp.di.DBModelImpl
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.xmlpull.v1.XmlPullParserFactory
import toothpick.Toothpick
import toothpick.ktp.delegate.inject
import java.io.InputStream
import javax.inject.Inject

class StandartLibLoader @Inject constructor(){

    private val dbm: DBModelImpl by inject()
    private val gcsParser: GCSParser by inject()

    private val compositeDisposable = CompositeDisposable()

    init {
        val scope = Toothpick.openScope("APP")
        scope.inject(this)
    }

     fun convertSkillListToDBFormat(skillsList: MutableList<Skill>) {
        skillsList.forEach {
            getSkillByName(it)
        }
    }

    fun convertAdvantageListToDBFormat(advList: MutableList<Advantage>) {
        advList.forEach {
            getAdvanyageByName(it)
        }
    }

    fun loadSkillLibrary(open: InputStream) {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(open, "windows-1251")

        convertSkillListToDBFormat(gcsParser.parseSkillList(parser))
    }

    fun loadAdvantageLibrary(open: InputStream) {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(open, "windows-1251")

        convertAdvantageListToDBFormat(gcsParser.parseAdvantageList(parser))
    }

    private fun getSkillByName(skill: Skill) {
        dbm.getDB().skillDao().getByName(skill.name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    println("This Skill is already there  - ${it.name}")
                },
                {
                    println("Insert skill (${skill.name}) error - $it")
                    insertSkill(skill)
                }
            ).let(compositeDisposable::add)
    }

    private fun getAdvanyageByName(advantage: Advantage) {
        dbm.getDB().advantageDao().getByName(advantage.name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    println("This Advantage is already there  - ${it.name}")
                },
                {
                    println("Insert advantage (${advantage.name}) error - $it")
                    insertAdvantage(advantage)
                }
            ).let(compositeDisposable::add)
    }

    private fun insertSkill(skill: Skill){
        Observable.create { emitter: ObservableEmitter<Int> ->
            dbm.db.skillDao().insert(skill)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                println("In insert skill (${skill.name}) error - $it")
            }
            .doOnComplete {
                println("Insert skill ${skill.name} Complete")
            }
            .subscribe()
    }

    private fun insertAdvantage(advantage: Advantage){
        Observable.create { emitter: ObservableEmitter<Int> ->
            dbm.db.advantageDao().insert(advantage)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                println("In insert advantage (${advantage.name}) error - $it")
            }
            .doOnComplete {
                println("Insert advantage ${advantage.name} Complete")
            }
            .subscribe()
    }
}