// IPlayerStateListener.aidl
package com.gochiusa.musicapp.plus.service;


interface IPlayerStateListener {
    void onPrepare();
    void onError();
    void onCompletion();
}
