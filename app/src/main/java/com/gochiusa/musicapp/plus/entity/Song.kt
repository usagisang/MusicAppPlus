package com.gochiusa.musicapp.plus.entity

import android.os.Parcel
import android.os.Parcelable


data class Song(val id: Long, val name: String?, val albumId: Long, val albumName: String?,
                val albumPicUrl: String?, val artists: List<Artist>?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(Artist)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeLong(albumId)
        parcel.writeString(albumName)
        parcel.writeString(albumPicUrl)
        parcel.writeTypedList(artists)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}