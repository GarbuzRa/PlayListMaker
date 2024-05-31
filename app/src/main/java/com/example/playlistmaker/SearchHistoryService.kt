package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson

class SearchHistoryService(sharedPreferences: SharedPreferences) {

    private val sharedPreferences = sharedPreferences
    fun read(): Array<Track> { //фу-ия чтения, отдает массив из треков (дата класс)
        //переменная, которая получает строку у SP через ключ со стандартным значением null(пустота) ->
        // -> ЭлвисО проверяет является ли все, что слева, NULL, если является, то делает действие справа ->
        //в остальном случае возвращает массив треков созданый из Json файла
        val json = sharedPreferences.getString(TRACK_HISTORY_KEY, null) ?: return emptyArray()
        return Gson().fromJson(json, Array<Track>::class.java)
    }

    fun clear() { //ф-ия очистки
        sharedPreferences //SP вызыван, допущен к комментам, очищен и сохранен
            .edit()
            .clear()
            .apply()
    }

    fun add(newTrack: Track) { //ф-ия добавить
        var tempList = read().toMutableList() //помещаем в темпЛист список треков, который взяли из ф-ии read и перевели в изменяемый список

        tempList.removeIf { it.trackId == newTrack.trackId } //удалить если id треков совпадают (такой трек уже есть)
        tempList.add(0, newTrack) //добавть трек по индексу

        if (tempList.size > 10) { //если треков больше 10 то ...
            tempList = tempList.subList(0, 10) //обрезает список треков (как я понял удаляет самый "верхний")
        }

        val json = Gson().toJson(tempList) //конвертируем в Json файл tempList

        sharedPreferences //вызываем SP, открываем для записи, добавляем по ключу в формате json, сохраняем
            .edit()
            .putString(TRACK_HISTORY_KEY, json)
            .apply()
    }
}
