// IBinderInterface.aidl
package com.gochiusa.musicapp.plus.service;

import com.gochiusa.musicapp.plus.service.IPlayerStateListener;

// Declare any non-default types here with import statements

interface IBinderInterface {
    // 设置在播放时是否一直亮屏
    void setScreenOn(boolean screenOn);
    // 设置当前播放进度
    void setProgress(int progress);
    // 设置是否循环播放
    void setLooping(boolean looping);
    // 获取当前播放进度
    int getProgress();
    // 获取当前曲目的持续时长
    int getDuration();
    // 传入预备播放的歌曲的id，让服务准备播放资源，但并不播放。
    // 此方法承诺可以进行高频率的设置，但是只会使用最后提交的歌曲进行播放
    void prepareMusic(long musicId);
    // 开始/继续播放音乐
    void playMusic();
    // 暂停播放音乐
    void pauseMusic();
    // 注册监听
    void registerPlayerStateListener(IPlayerStateListener listener);
    // 取消监听
    void unregisterPlayerStateListener(IPlayerStateListener listener);
}
