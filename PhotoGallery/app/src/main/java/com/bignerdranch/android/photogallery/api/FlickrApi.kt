package com.bignerdranch.android.photogallery.api

import retrofit2.Call
import retrofit2.http.GET

interface FlickrApi {
    @GET(
        "services/rest/?method=flickr.interestingness.getlist" +
                "&api_key=489e3c4f227fcdbf879da15d13d1c8c5" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    fun fetchPhotos(): Call<FlickrResponse>


}