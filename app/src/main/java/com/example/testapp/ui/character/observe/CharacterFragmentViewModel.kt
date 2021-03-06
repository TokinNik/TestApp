package com.example.testapp.ui.character.observe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.testapp.db.entity.Character
import com.example.testapp.db.entity.CharacterSkills
import com.example.testapp.db.entity.Skill.Skill
import com.example.testapp.di.DBModelImpl
import com.example.testapp.ui.RxViewModel
import com.example.testapp.ui.settings.ColorScheme
import com.example.testapp.util.DataManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import toothpick.Toothpick
import toothpick.ktp.delegate.inject

class CharacterFragmentViewModel : RxViewModel() {

    private val dbm: DBModelImpl by inject()
    private val dataManager: DataManager by inject()

    val characterById: LiveData<Character>
        get() = characterByIdEvent

    val error: LiveData<Throwable>
        get() = errorEvent

    val deleteComplete: LiveData<Boolean>
        get() = deleteCompleteEvent

     val characterSkillsByIdComplete: LiveData<List<CharacterSkills>>
        get() = characterSkillsByIdEvent

    val getSkillByNamesComplete: LiveData<List<Skill>>
        get() = getSkillByNamesEvent

    val colorScheme: LiveData<ColorScheme>
        get() = colorSchemeEvent

    private var errorEvent: MutableLiveData<Throwable> = MutableLiveData()

    private var deleteCompleteEvent: MutableLiveData<Boolean> = MutableLiveData()

    private var characterByIdEvent: MutableLiveData<Character> = MutableLiveData()

    private var characterSkillsByIdEvent: MutableLiveData<List<CharacterSkills>> = MutableLiveData()


    private var getSkillByNamesEvent: MutableLiveData<List<Skill>> = MutableLiveData()

    private var colorSchemeEvent: MutableLiveData<ColorScheme> = MutableLiveData()

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

    fun getSkillByNames(characterSkills: List<CharacterSkills>)
    {
        dbm.getDB().skillDao().getByNames(characterSkills.map { it.skillName })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    getSkillByNamesEvent.value = list.filter { skill ->
                        characterSkills.any {
                            it.skillName == skill.name && it.specialization == skill.specialization
                        }
                    }
                    getSkillByNamesEvent.value
                },
                {
                    errorEvent.value = it
                }
            ).let(compositeDisposable::add)
    }

    fun clearEvents()
    {
        errorEvent =  MutableLiveData()
        deleteCompleteEvent =  MutableLiveData()
        characterByIdEvent =  MutableLiveData()
        characterSkillsByIdEvent = MutableLiveData()
        getSkillByNamesEvent = MutableLiveData()
        colorSchemeEvent = MutableLiveData()
    }

    fun getColorScheme() {
        colorSchemeEvent.value = ColorScheme.valueOf(dataManager.appSettingsVault.colorScheme)
    }
}