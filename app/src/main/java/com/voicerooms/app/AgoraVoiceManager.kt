package com.voicerooms.app

import android.content.Context
import android.util.Log
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

/**
 * مدير الصوت باستخدام Agora
 * ملاحظة: الـ App Certificate موجود هنا للتجربة فقط.
 * في الإنتاج لازم يتولد الـ Token من سيرفر وما يتحطش في التطبيق.
 */
object AgoraVoiceManager {

    private const val TAG = "AgoraVoiceManager"

    // بيانات المشروع من Agora Console
    const val APP_ID = "7a85f74147c349749501edefab2f059f"

    // للتجربة فقط - لا تستخدم في الإنتاج
    private const val APP_CERTIFICATE = "26e52fb187e045a4b9ee4f8e001d65bf"

    private var rtcEngine: RtcEngine? = null
    private var isJoined = false
    private var currentChannel: String? = null

    var onUserJoined: ((uid: Int) -> Unit)? = null
    var onUserOffline: ((uid: Int) -> Unit)? = null
    var onJoinSuccess: ((channel: String, uid: Int) -> Unit)? = null
    var onError: ((code: Int, msg: String) -> Unit)? = null

    private val eventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "Joined channel: $channel, uid: $uid")
            isJoined = true
            currentChannel = channel
            onJoinSuccess?.invoke(channel ?: "", uid)
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "Remote user joined: $uid")
            onUserJoined?.invoke(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "Remote user offline: $uid")
            onUserOffline?.invoke(uid)
        }

        override fun onError(err: Int) {
            Log.e(TAG, "Agora error: $err")
            onError?.invoke(err, "Error code: $err")
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            Log.d(TAG, "Connection state: $state, reason: $reason")
        }
    }

    fun initialize(context: Context): Boolean {
        if (rtcEngine != null) return true
        return try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = APP_ID
                mEventHandler = eventHandler
            }
            rtcEngine = RtcEngine.create(config)
            // تفعيل الصوت فقط
            rtcEngine?.enableAudio()
            rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            Log.d(TAG, "Agora engine initialized")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init Agora", e)
            false
        }
    }

    /**
     * الانضمام لغرفة صوتية
     * @param channelName اسم الغرفة (نفس الاسم = نفس الغرفة)
     * @param token توكن مؤقت (فارغ لو المشروع يسمح، أو من Console)
     * @param asSpeaker true = متحدث (يقدر يبعت صوت)، false = مستمع فقط
     */
    fun joinChannel(
        channelName: String,
        token: String? = null,
        asSpeaker: Boolean = false
    ): Boolean {
        val engine = rtcEngine ?: return false
        if (isJoined && currentChannel == channelName) return true

        // لو كنا في قناة تانية نطلع الأول
        if (isJoined) {
            leaveChannel()
        }

        val options = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            clientRoleType = if (asSpeaker) {
                Constants.CLIENT_ROLE_BROADCASTER
            } else {
                Constants.CLIENT_ROLE_AUDIENCE
            }
            publishMicrophoneTrack = asSpeaker
            autoSubscribeAudio = true
        }

        val result = engine.joinChannel(token, channelName, 0, options)
        Log.d(TAG, "joinChannel result: $result for channel: $channelName")
        return result == 0
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
        isJoined = false
        currentChannel = null
        Log.d(TAG, "Left channel")
    }

    fun muteLocalAudio(mute: Boolean) {
        rtcEngine?.muteLocalAudioStream(mute)
        // لو فتحنا المايك نتحول لمتحدث
        if (!mute) {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            rtcEngine?.enableLocalAudio(true)
        } else {
            rtcEngine?.setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
        Log.d(TAG, "Mute local audio: $mute")
    }

    fun isJoined(): Boolean = isJoined

    fun destroy() {
        leaveChannel()
        RtcEngine.destroy()
        rtcEngine = null
        Log.d(TAG, "Agora engine destroyed")
    }
}
