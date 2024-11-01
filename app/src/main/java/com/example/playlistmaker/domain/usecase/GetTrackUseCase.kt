package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class GetTrackUseCase(val trackRepository: TrackRepository) {
    //зависит от экземпляра какого-то класса реализовавшего интерфейс трекРепозитори
    operator fun invoke(trackId:Int):Track?{ //перегружаем опператор () - для вызова класса как ф-ии
        //вызываем ф-ию getSearchHistory из репозитория
        //ищем трек по совпадению трекИд
        return trackRepository.getSearchHistory().find {it.trackId == trackId}
    }
    //
}