package com.example.testapp.ui.character.observe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testapp.db.entity.Character
import com.example.testapp.db.entity.CharacterSkills
import com.example.testapp.db.entity.Skill.Skill
import com.example.testapp.di.DBModelImpl
import com.example.testapp.ui.RxViewModel
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import toothpick.Toothpick
import toothpick.ktp.delegate.inject

class CharacterFragmentViewModel: RxViewModel() {

    private val dbm: DBModelImpl by inject()

    val characterById: LiveData<Character>
        get() = characterByIdEvent

    val error: LiveData<Throwable>
        get() = errorEvent

    val deleteComplete: LiveData<Boolean>
        get() = deleteCompleteEvent

     val characterSkillsByIdComplete: LiveData<List<CharacterSkills>>
        get() = characterSkillsByIdEvent

    val getSkillByNameComplete: LiveData<Skill>
        get() = getSkillByNameEvent

    private var errorEvent: MutableLiveData<Throwable> = MutableLiveData()

    private var deleteCompleteEvent: MutableLiveData<Boolean> = MutableLiveData()

    private var characterByIdEvent: MutableLiveData<Character> = MutableLiveData()

    private var characterSkillsByIdEvent: MutableLiveData<List<CharacterSkills>> = MutableLiveData()

    private var getSkillByNameEvent: MutableLiveData<Skill> = MutableLiveData()

    init {
        val appScope = Toothpick.openScope("APP")
        Toothpick.inject(this, appScope)
    }

    fun getCharacterById(id: Int)
    {
        dbm.getDB().characterDao().getById(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    characterByIdEvent.value = it
                },
                {
                    errorEvent.value = it
                }
            ).let(compositeDisposable::add)
    }

    fun getCharacterSkillsById(id: Int) {
        dbm.getDB().characterSkillsDao().getCharacterSkills(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    characterSkillsByIdEvent.value = it
                },
                {
                    errorEvent.value = it
                }
            ).let(compositeDisposable::add)
    }

    fun deleteCharacter(character: Character) {
        Observable.create { emitter: ObservableEmitter<Int> ->
            dbm.db.characterDao().delete(character)
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                errorEvent.value = it
            }
            .doOnComplete {
                deleteCompleteEvent.value = true
            }
            .subscribe()
            .let(compositeDisposable::add)
    }

    fun clearEvents()
    {
        errorEvent =  MutableLiveData()
        deleteCompleteEvent =  MutableLiveData()
        characterByIdEvent =  MutableLiveData()
        characterSkillsByIdEvent = MutableLiveData()
    }

    fun getSkillByName(name: String) {
        dbm.getDB().skillDao().getByName(name)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    getSkillByNameEvent.value = it
                },
                {
                    errorEvent.value = it
                }
            ).let(compositeDisposable::add)
    }
}