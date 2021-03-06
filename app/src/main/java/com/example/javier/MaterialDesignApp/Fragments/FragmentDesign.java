package com.example.javier.MaterialDesignApp.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.javier.MaterialDesignApp.DetailActivity;
import com.example.javier.MaterialDesignApp.R;
import com.example.javier.MaterialDesignApp.RecyclerView.RecyclerViewAdapters.DesignAdapter;
import com.example.javier.MaterialDesignApp.RecyclerView.RecyclerViewClasses.Design;
import com.example.javier.MaterialDesignApp.RecyclerView.RecyclerViewDecorations.DividerItemDecoration;
import com.example.javier.MaterialDesignApp.RecyclerView.RecyclerViewUtils.ItemClickSupport;
import com.example.javier.MaterialDesignApp.Utilitis.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class FragmentDesign extends Fragment {

    String urlPost;
    JSONObject jsonObjectDesignPosts;
    JSONArray jsonArrayDesignContent;
    ArrayList<Design> designs;
    SwipeRefreshLayout swipeRefreshLayout;
    String[] designTitle, designExcerpt, designImage, designImageFull, designContent;
    int postNumber = 99;
    SharedPreferences sharedPreferences;
    Boolean error = false;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("VALUES", Context.MODE_PRIVATE);

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_design, container, false);

        // Setup RecyclerView News
        recyclerViewDesign(view);

        // Setup swipe to refresh
        swipeToRefresh(view);

        return view;
    }

    private void recyclerViewDesign(View view) {

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewDesign);

        // Divider
        recyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(android.R.drawable.divider_horizontal_bright)));

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        urlPost = "http://wordpressdesarrolladorandroid.hol.es/category/diseno?json=1";
        new AsyncTaskNewsParseJson().execute(urlPost);

        ItemClickSupport itemClickSupport = ItemClickSupport.addTo(recyclerView);
        itemClickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                sharedPreferences.edit().putString("TITLE", designTitle[position]).apply();
                sharedPreferences.edit().putString("CONTENT", designContent[position]).apply();
                sharedPreferences.edit().putString("EXCERPT", designExcerpt[position]).apply();
                sharedPreferences.edit().putString("IMAGE", designImageFull[position]).apply();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                startActivity(intent);
            }
        });

    }

    public class AsyncTaskNewsParseJson extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
        }

        // get JSON Object
        @Override
        protected String doInBackground(String... url) {

            urlPost = url[0];
            try {
                jsonObjectDesignPosts = JsonParser.readJsonFromUrl(urlPost);
                postNumber = jsonObjectDesignPosts.getJSONArray("posts").length();
                jsonArrayDesignContent = jsonObjectDesignPosts.getJSONArray("posts");
                sharedPreferences.edit().putString("DESIGN", jsonArrayDesignContent.toString()).apply();
                designTitle = new String[postNumber];
                designExcerpt = new String[postNumber];
                designContent = new String[postNumber];
                designImage = new String[postNumber];
                designImageFull = new String[postNumber];
                for (int i = 0; i < postNumber; i++) {
                    designTitle[i] = Html.fromHtml(jsonObjectDesignPosts.getJSONArray("posts").getJSONObject(i).getString("title")).toString();
                    designExcerpt[i] = Html.fromHtml(jsonObjectDesignPosts.getJSONArray("posts").getJSONObject(i).getString("excerpt")).toString();
                    designContent[i] = jsonObjectDesignPosts.getJSONArray("posts").getJSONObject(i).getString("content");
                    designImage[i] = Html.fromHtml(jsonObjectDesignPosts.getJSONArray("posts").getJSONObject(i).getJSONObject("thumbnail_images").getJSONObject("thumbnail").getString("url")).toString();
                    designImageFull[i] = Html.fromHtml(jsonObjectDesignPosts.getJSONArray("posts").getJSONObject(i).getJSONObject("thumbnail_images").getJSONObject("full").getString("url")).toString();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                designTitle = new String[0];
                error = true;
            }
            return null;
        }

        // Set facebook items to the textviews and imageviews
        @Override
        protected void onPostExecute(String result) {

            designs = new ArrayList<>();

            //Data set used by the adapter. This data will be displayed.
            if (designTitle.length != 0) {
                for (int i = 0; i < postNumber; i++) {
                    designs.add(new Design(designTitle[i], designExcerpt[i], designImage[i]));
                }
            }
            if (error) {
                Toast.makeText(getActivity(), "Error de conexión", Toast.LENGTH_LONG).show();
            }
            // Create the adapter
            adapter = new DesignAdapter(getActivity(), designs);
            recyclerView.setAdapter(adapter);

            swipeRefreshLayout = (android.support.v4.widget.SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
            swipeRefreshLayout.setRefreshing(false);

            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
        }
    }
    private void swipeToRefresh(View view) {
        swipeRefreshLayout = (android.support.v4.widget.SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        TypedValue typedValueColorPrimary = new TypedValue();
        TypedValue typedValueColorAccent = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValueColorPrimary, true);
        getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValueColorAccent, true);
        final int colorPrimary = typedValueColorPrimary.data, colorAccent = typedValueColorAccent.data;
        swipeRefreshLayout.setColorSchemeColors(colorPrimary,colorAccent);

        swipeRefreshLayout.setOnRefreshListener(new android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTaskNewsParseJson().execute(urlPost);
            }
        });
    }
}

