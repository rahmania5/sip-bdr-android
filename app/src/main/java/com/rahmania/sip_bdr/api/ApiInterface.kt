package com.rahmania.sip_bdr.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {
    @FormUrlEncoded
    @POST("login")
    fun loginResponse(
        @Field("username") username: String?,
        @Field("password") password: String?
    ): Call<ResponseBody?>?

    @POST("logout")
    fun logout(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @GET("islogin")
    fun isLogin(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @GET("user")
    fun getDetails(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @FormUrlEncoded
    @PUT("changepassword")
    fun changePassword(
        @Header("Authorization") authToken: String?,
        @Field("old_password") oldPassword: String?,
        @Field("password") newPassword: String?,
        @Field("password_confirmation") passwordConfirmation: String?
    ): Call<ResponseBody?>?

    @GET("lecturerclassroom")
    fun getClassroomList(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @GET("schedules/{classroom_id}")
    fun getClassroomSchedule(
        @Header("Authorization") authToken: String?,
        @Path("classroom_id") classroomId: Int?
    ): Call<ResponseBody?>?

    @GET("meetingnumber/{lecturer_classroom_id}")
    fun getMeetingNumbers(
        @Header("Authorization") authToken: String?,
        @Path("lecturer_classroom_id") lecturerClassroomId: Int?
    ): Call<ResponseBody?>?

    @FormUrlEncoded
    @POST("meeting/{id}")
    fun createMeeting(
        @Header("Authorization") authToken: String?,
        @Path("id") id: Int?,
        @Field("number_of_meeting") numberOfMeeting: String,
        @Field("date") date: String?,
        @Field("start_time") startTime: String?,
        @Field("finish_time") finishTime: String?,
        @Field("lecturer_classroom_id") lecturerClassroomId: Int?,
        @Field("topic") topic: String?
    ): Call<ResponseBody?>?

    @FormUrlEncoded
    @PUT("meeting/{id}")
    fun editMeeting(
        @Header("Authorization") authToken: String?,
        @Path("id") id: Int?,
        @Field("id") meetingId: Int?,
        @Field("number_of_meeting") numberOfMeeting: String,
        @Field("date") date: String?,
        @Field("start_time") startTime: String?,
        @Field("finish_time") finishTime: String?,
        @Field("topic") topic: String?
    ): Call<ResponseBody?>?

    @DELETE("meeting/{id}")
    fun deleteMeeting(
        @Header("Authorization") authToken: String?,
        @Path("id") id: Int?
    ): Call<ResponseBody?>?

    @GET("lecturermeetings/{id}")
    fun getMeetings(
        @Header("Authorization") authToken: String?,
        @Path("id") classroomId: Int?
    ): Call<ResponseBody?>?

    @GET("studentsubmission")
    fun getStudentSubmission(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @FormUrlEncoded
    @PATCH("studentlocation/{id}")
    fun followUpSubmission(
        @Header("Authorization") authToken: String?,
        @Path("id") id: Int?,
        @Field("submission_status") submissionStatus: String?
    ): Call<ResponseBody?>?

    @GET("classattendance/{id}")
    fun getStudentAttendances(
        @Header("Authorization") authToken: String?,
        @Path("id") meetingId: Int?
    ): Call<ResponseBody?>?

    @FormUrlEncoded
    @PATCH("studentattendance/{id}")
    fun editAttendanceStatus(
        @Header("Authorization") authToken: String?,
        @Path("id") id: Int?,
        @Field("presence_status") presenceStatus: String?
    ): Call<ResponseBody?>?

}