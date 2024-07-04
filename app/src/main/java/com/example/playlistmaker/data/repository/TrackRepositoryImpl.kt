package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.remote.ItunesApiService
import com.example.playlistmaker.data.storage.SharedPreferencesStorage
import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import retrofit2.Callback
import retrofit2.Response

class TrackRepositoryImpl(val apiService: ItunesApiService, val storage: SharedPreferencesStorage):TrackRepository {
    //зависит от переменной apiService типа данных (интерфейс подключения к внешним данным)...
    //от переменной storage типа данных (класс, который хранит в себе класс SharedPreferencesStorage
    //имплементирует интерфейс TrackRepository и вызывает все его ф-ии

    override fun searchTracks(query: String, callback: (Result<List<Track>>) -> Unit) {
        //ф-ия поиска треков, которая принимает запрос(query) и действие при ответе на запрос(callback)
        //callback типа данных Unit, и уже сама ф-ия Unit принимает в себя результат листа треков
        //Result - сериализующий класс, в данном случае в лист треков.

        apiService.search(query).enqueue(object : Callback<ItunesApiService.SearchResponse> {
        //apiService производит поиск с указанным запросом
            //результат этого поиска ставится в очередь. Когда запрос завершится
            //(успешно или с ошибкой), тогда будет вызыван объект Callback
            //если обобщить, то эта часть кода отправляет запрос к API-сервису для запроса
            //и обрабатывает ответ с помощью Callback

            override fun onResponse( //метод используется в объекте Callback для обработки успешного ответа API-сервиса
                //когда запрос будет выполнен успешн и сервер вернет данные, тогда метод будет вызван
                // с параметрами, которые содержат ответ от сервера
                call: retrofit2.Call<ItunesApiService.SearchResponse>, //непосредственно сам запрос
                response: Response<ItunesApiService.SearchResponse>  //предоставляет ответ от сервера,
            // который либо содержит данные, либо инфу об ошибке
            ) {
                if (response.isSuccessful) {
                    //если ответ успешен, в таком случае..
                    //создается переменная треков, которая получает тело ответа и извлекает из
                    // него список результатов (если тело ответа не = null
                    //.map { it.toDomainModel() - применяет ф-ию toDomainModel() к каждому элементк
                    // списка результатов, чтобы преобразовать их в модель доменов (не шарю за это, гуглил конкретно про эту ф-ию)
                    //ну и Элвис - если тело ответа или результатов равны null, то возвращает пустой список
                    val tracks = response.body()?.results?.map { it.toDomainModel() } ?: emptyList()
                    callback(Result.success(tracks)) //создается объект Result, который указывает на успешный результат
                    //содержащий список треков
                        //callback передает этот объект в ф-ию обратного вызова
                } else {
                    callback(Result.failure(Exception("API error: ${response.code()}")))
                        // иначе выдает сообщение об ошибке
                }
            }

            override fun onFailure(call: retrofit2.Call<ItunesApiService.SearchResponse>, t: Throwable) {
                callback(Result.failure(t)) //при неудачном запросе
            }
        })
    }

    override fun getSearchHistory(): List<Track> = storage.readTracks() //ну я так понимаю вызывает
    // ф-ию readTracks из класса SharedPreferencesStorage

    override fun addToSearchHistory(track: Track) {
        val history = getSearchHistory().toMutableList() //переводит в изменяемый лист
        history.removeIf { it.trackId == track.trackId }  //удаляет элемент из массива, если он соответсвует условиям в теле
        history.add(0, track) //добавляет трек по индексу
        if (history.size > 10) { //если размер истории больше 10..
            history.subList(0, 10) //тогда добавляет его в нулевой индекс (то есть он самый топовый будет)
        }
        storage.saveTracks(history) //вызывает ф-ию saveTracks у класса SharedPreferencesStorage
    }

    override fun clearSearchHistory() {
        storage.clearTracks() //просто вызывает команду очистки истории
    }

    private fun ItunesApiService.TrackDto.toDomainModel() = Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )

}