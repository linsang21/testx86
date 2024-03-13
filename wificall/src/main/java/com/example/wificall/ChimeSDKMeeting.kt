package com.example.wificall

import android.content.Context
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.DefaultEglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.internal.utils.DefaultBackOffRetry
import com.amazonaws.services.chime.sdk.meetings.internal.utils.HttpUtils
import com.amazonaws.services.chime.sdk.meetings.session.CreateAttendeeResponse
import com.amazonaws.services.chime.sdk.meetings.session.CreateMeetingResponse
import com.amazonaws.services.chime.sdk.meetings.session.DefaultMeetingSession
import com.amazonaws.services.chime.sdk.meetings.session.MediaPlacement
import com.amazonaws.services.chime.sdk.meetings.session.Meeting
import com.amazonaws.services.chime.sdk.meetings.session.MeetingFeatures
import com.amazonaws.services.chime.sdk.meetings.session.MeetingSessionConfiguration
import com.amazonaws.services.chime.sdk.meetings.utils.logger.ConsoleLogger
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL


class ChimeSDKMeeting {
    private val TAG = "ChimeSDKMeeting"
    private val logger = ConsoleLogger()
    private val gson = Gson()
    private val uiScope = CoroutineScope(Dispatchers.Main)


    fun startMeeting(applicationContext: Context) {
        uiScope.launch {
            val meetingServerUrl = ""//TODO: add
            val meetingId = "" //TODO: add
            val url = "${meetingServerUrl}join?title=${meetingId}&name=test&region=us-west-2"
            val response = HttpUtils.post(URL(url), "", DefaultBackOffRetry(), logger)
            if (response.httpException != null) {
                logger.error(TAG, "Unable to join meeting. ${response.httpException}")
            } else {
                val meetingResponseJson = response.data
                val sessionConfig =
                    createSessionConfigurationAndExtractPrimaryMeetingInformation(meetingResponseJson)
                val meetingSession = sessionConfig?.let {
                    DefaultMeetingSession(
                        it,
                        logger,
                        applicationContext,
                        DefaultEglCoreFactory()
                    )
                }
                meetingSession?.audioVideo?.start()
            }
        }
    }

    private fun createSessionConfigurationAndExtractPrimaryMeetingInformation(response: String?): MeetingSessionConfiguration? {
        if (response.isNullOrBlank()) return null

        return try {
            val joinMeetingResponse = gson.fromJson(response, JoinMeetingResponse::class.java)
            val meetingResp = joinMeetingResponse.joinInfo.meetingResponse.meeting
            val externalMeetingId: String = meetingResp.ExternalMeetingId ?: ""
            val mediaPlacement: MediaPlacement = meetingResp.MediaPlacement
            val mediaRegion: String = meetingResp.MediaRegion
            val meetingId: String = meetingResp.MeetingId
            val meetingFeatures = MeetingFeatures(meetingResp.MeetingFeatures?.Video?.MaxResolution, meetingResp.MeetingFeatures?.Content?.MaxResolution)
            val meeting =
                Meeting(
                    externalMeetingId,
                    mediaPlacement,
                    mediaRegion,
                    meetingId,
                    meetingFeatures
                )
            MeetingSessionConfiguration(
                CreateMeetingResponse(meeting),
                CreateAttendeeResponse(joinMeetingResponse.joinInfo.attendeeResponse.attendee)
            )
        } catch (exception: Exception) {
            logger.error(
                TAG,
                "Error creating session configuration: ${exception.localizedMessage}"
            )
            null
        }
    }
}
