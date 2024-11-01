package com.example.playlistmaker.presentation.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.Track


class TrackAdapter(var trackList: MutableList<Track>, var clickListener: (Track) -> Unit = {}) :
//принимает в себя trackList изменяемых листов из треков (дата класс) и ф-ию  clickListener, которая тоже принимает в себя треки
    RecyclerView.Adapter<TrackViewHolder>() {
        var longClickListener: (Track) -> Unit = {}
    fun updateList(trackList: MutableList<Track>) { //ф-ия обновить плейлист
        this.trackList = trackList //этот треклист (который в аргументах как я понял) становится трекЛистом
        notifyDataSetChanged() //передаем ин-фу о обновлении
    }

    fun clearList() { //ф-ия очистить лист
        trackList.clear() //просто чистим
        notifyDataSetChanged() //передаем инфу
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_item, parent, false)

        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) { //ф-ия для каждого  экземпляр VH применяет ф-ию bind
        //для каждого экземпляра VH данные привязываются к элементам верстки этого VH
        holder.bind(trackList[position]) //привязываем к холдеру элемент массива по позиции
        holder.itemView.setOnClickListener {//при нажатии на весь трек происходит..
            clickListener(trackList[position]) //вызывает ф-ию по ссылке
        }
        holder.itemView.setOnLongClickListener{ // при долгом нажатии на трек
            longClickListener.invoke(trackList[position]) // происходит вызов второй функции
            true
        }
    }

    override fun getItemCount() = trackList.size

}