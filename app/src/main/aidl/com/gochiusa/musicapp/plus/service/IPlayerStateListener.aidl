// IPlayerStateListener.aidl
package com.gochiusa.musicapp.plus.service;


interface IPlayerStateListener {
    void onPrepared();
    void onError(int errorCode);
    void onCompletion();

    void skipNextSong();
    void returnPreviousSong();
    void stopSelf();
}
