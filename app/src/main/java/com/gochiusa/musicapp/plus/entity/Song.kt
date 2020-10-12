package com.gochiusa.musicapp.plus.entity

import android.os.Parcel
import android.os.Parcelable
import com.gochiusa.musicapp.plus.util.StringContract


data class Song(val id: Long, val name: String?, val albumId: Long, val albumName: String?,
                val albumPicUrl: String?, val artists: List<Artist>?) : Parcelable {
    var localUriString: String? = null

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(Artist)
    ) {
        this@Song.localUriString = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeLong(albumId)
        parcel.writeString(albumName)
        parcel.writeString(albumPicUrl)
        parcel.writeTypedList(artists)
        parcel.writeString(localUriString)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        private const val DEFAULT_ARTIST = "未知"

        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }

    fun getAllArtistString(): String {
        return this.artists?.joinToString(separator = StringContract.COMMA_SEPARATOR) {
            it.name ?: ""
        } ?: DEFAULT_ARTIST
    }
}