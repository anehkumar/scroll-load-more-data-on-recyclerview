package com.scrollmorerecyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.scrollmorerecyclerview.adapter.UserListAdapter;
import com.scrollmorerecyclerview.model.UserModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView list;

    ArrayList<UserModel> userModelArrayList;
    UserListAdapter userListAdapter;
    LinearLayoutManager linearLayoutManager;
    int page = 1;
    OkHttpClient client = new OkHttpClient();
    int pastVisibleItems, visibleItemCount, totalItemCount;
    Boolean loading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        list = (RecyclerView)findViewById(R.id.list);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        list.setLayoutManager(linearLayoutManager);
        userModelArrayList = new ArrayList<>();

        userListAdapter = new UserListAdapter(this, userModelArrayList, this);
        list.setAdapter(userListAdapter);

        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                {
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (!loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = true;
                            //page = page+1;
                            getUserList();
                        }
                    }
                }
            }
        });

        getUserList();
    }

    public void getUserList(){
        Request request = new Request.Builder()
                .url("https://reqres.in/api/users?per_page=15&page=1")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String mMessage = response.body().string();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject response = new JSONObject(mMessage);
                            JSONArray users = response.getJSONArray("data");

                            for (int i = 0; i < users.length(); i++){
                                JSONObject user = users.getJSONObject(i);

                                UserModel userModel = new UserModel();
                                userModel.setName(user.getString("first_name"));
                                userModel.setImage(user.getString("avatar"));

                                userModelArrayList.add(userModel);
                            }

                            userListAdapter.notifyDataSetChanged();
                            loading = false;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
