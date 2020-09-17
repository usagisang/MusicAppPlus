package com.gochiusa.musicapp.plus.entity

import android.view.View
import android.widget.Button
import com.gochiusa.musicapp.plus.R
import com.gochiusa.musicapp.plus.widget.MusicControlView

class MusicControlHolder(view: View): MusicControlView.ViewHolder(view) {
    var button: Button = itemView.findViewById(R.id.btn_entity)
        private set
}