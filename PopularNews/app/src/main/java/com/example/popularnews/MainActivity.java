package com.example.popularnews;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularnews.api.ApiClient;
import com.example.popularnews.api.ApiInterface;
import com.example.popularnews.models.Adapter;
import com.example.popularnews.models.Article;
import com.example.popularnews.models.News;
import com.example.popularnews.models.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    // Mon API personnel du site newsapi.org
    public static final String API_KEY = "a55e2f5e53ba42ffa2e8158f94bae817";

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        onLoadingSwipeRefresh(""); // méthode plus bas


    }

    public void LoadJson(final String keyword) {
        swipeRefreshLayout.setRefreshing(true);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String country = Utils.getCountry();
        String language = Utils.getLanguage();
        Call<News> call;

        if(keyword.length() > 0) { // pour specifier la recherche dans l'app
            call = apiInterface.getNewsSearch(keyword,language,"publishedAt",API_KEY);
        }
        else {
            call = apiInterface.getNews(country,API_KEY);
        }
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticle() != null) {
                        if(!articles.isEmpty()) {
                            articles.clear();
                        }

                        articles = response.body().getArticle();
                        adapter = new Adapter(articles,MainActivity.this);
                        recyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                        initListener();

                        swipeRefreshLayout.setRefreshing(false);

                } else {

                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this,"No Result !",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    private void initListener(){
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ImageView imageView = view.findViewById(R.id.img);
                Intent intent = new Intent(MainActivity.this,NewsDetailActivity.class);
                Article article = articles.get(position);
                intent.putExtra("url",article.getUrl());
                intent.putExtra("title",article.getTitle());
                intent.putExtra("img",article.getUrlToImage());
                intent.putExtra("date",article.getPublishedAt());
                intent.putExtra("source",article.getSource().getName());
                intent.putExtra("author",article.getAuthor());

                Pair<View,String> pair = Pair.create((View)imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this,pair

                );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(intent,optionsCompat.toBundle());
                }
                else {
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu); // dans les ressources on get menu_main dans le dossier main
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Rechercher... "); // en attente de la recherche

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length() > 2) {
                    onLoadingSwipeRefresh(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchMenuItem.getIcon().setVisible(false,false); // on cache l'icone

        return true;
    }

    @Override
    public void onRefresh() {
        LoadJson("");
    }
    private void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson(keyword);
                    }
                }
        );
    }
}
