package com.example.popularnews.api;

import com.example.popularnews.models.News;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("top-headlines") // sur le site news api , la suite de l'url
    Call<News> getNews(

            @Query("country") String country,
            @Query("apikey") String apikey    // HTTP --> API KEY , voir dans le mainActivity

    );
    @GET("everything") // site news api, la suite de l'url , pour la barre de recherche dans l'app
    Call<News> getNewsSearch(
        @Query("q") String keyword,
        @Query("language") String language,
        @Query("sortBy") String sortBy,
        @Query("apikey") String apikey
    );
}
