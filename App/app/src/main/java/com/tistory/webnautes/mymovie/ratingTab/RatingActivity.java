package com.tistory.webnautes.mymovie.ratingTab;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.tistory.webnautes.mymovie.DetailMovieActivity;
import com.tistory.webnautes.mymovie.HttpConnection;
import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;
import com.tistory.webnautes.mymovie.R;
import com.tistory.webnautes.mymovie.RecyclerViews.RecyclerViewClickListener;
import com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView.MovieRecyclerViewAdapter_rating;
import com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView.MovieRecyclerViewItem;
import com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView.MovieRecyclerViewLayoutManager;
import com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView.OnRecordEventListener;
import com.tistory.webnautes.mymovie.Static;
import com.tistory.webnautes.mymovie.movieInformation.high_score_order_comparator;
import com.tistory.webnautes.mymovie.movieInformation.low_score_order_comparator;
import com.tistory.webnautes.mymovie.movieInformation.old_date_order_comparator;
import com.tistory.webnautes.mymovie.movieInformation.recent_date_order_comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;

public class RatingActivity extends AppCompatActivity implements OnRecordEventListener {
    private JSONArray jarrayFromServer = null;
    private ArrayList<MovieInformation> ratingMovieList;

    private TextView rating_textView;

    private RecyclerView movie_recyclerView;
    private MovieRecyclerViewLayoutManager movieLayoutManager;
    private MovieRecyclerViewAdapter_rating movieRecyclerViewAdapter_rating;
    int movieRecyclerViewCurrentSize;
    boolean movieRecyclerViewLoading;

    public MovieRecyclerViewAdapter_rating getMovieRecyclerViewAdapter_rating() {
        return movieRecyclerViewAdapter_rating;
    }

    //MovieRecyclerView item의 ratingBar 값이 바뀌었을때 이벤트
    @Override
    public void onRatingBarChange(MovieRecyclerViewItem item, float value) {
        item.getMovieInfo().setScore(value);

        new SetRateToServer(item.getMovieInfo().getM_id(),value).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        Static.ratingActivity = this;

        rating_textView = (TextView)findViewById(R.id.rating_textView);

        ratingMovieList = new ArrayList<MovieInformation>();

        //movieRecyclerView 세팅
        movie_recyclerView = (RecyclerView) findViewById(R.id.movie_recyclerView);
        movieLayoutManager = new MovieRecyclerViewLayoutManager(this);
        movieLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        movieRecyclerViewAdapter_rating = new MovieRecyclerViewAdapter_rating(this);
        movieRecyclerViewCurrentSize = 0;
        movieRecyclerViewLoading = false;

        movie_recyclerView.setLayoutManager(movieLayoutManager);
        movie_recyclerView.setAdapter(movieRecyclerViewAdapter_rating);
        movie_recyclerView.addOnItemTouchListener(
                new RecyclerViewClickListener(getApplicationContext(), movie_recyclerView, new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MovieRecyclerViewItem item =((MovieRecyclerViewAdapter_rating)movie_recyclerView.getAdapter()).getItem(position);

                        RatingTabGroup parent = (RatingTabGroup)getParent();
                        Static.ratingTabMovieInfo = item.getMovieInfo();

                        Intent intent = new Intent(getApplicationContext(), DetailMovieActivity.class);
                        intent.putExtra("tabNumber",2);
                        View detailMovieView = RatingTabGroup.tabGroup.getLocalActivityManager()
                                .startActivity("DetailMovieActivity", intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                .getDecorView();

                        RatingTabGroup.tabGroup.replaceView(detailMovieView);
                    }
                }));
        movie_recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (!movie_recyclerView.canScrollVertically(1) && movieRecyclerViewLoading == false) {
                    int index = movieRecyclerViewCurrentSize;
                    for(int i = 0; i < 3; i++, index++) {
                        if(index < ratingMovieList.size())
                            new AddMovieRecyclerViewItem(ratingMovieList.get(index)).execute();
                    }
                }
            }
        });

        new GetMovieListFromServer().execute();
    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = movieRecyclerViewAdapter_rating.getLayoutPosition();
        MovieInformation selecteMovieInfo = movieRecyclerViewAdapter_rating.getItem(position).getMovieInfo();

        Log.d("isaacRate","m_id:"+selecteMovieInfo.getM_id()+", title:"+selecteMovieInfo.getTitle());
        switch (item.getItemId()) {
            case 0:
                new AddWishListToServer(selecteMovieInfo.getM_id()).execute();
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    //추천목록 정렬하기
    public void sort_menu_button_clicked(View v) {
        PopupMenu p = new PopupMenu(getApplicationContext(), v);
        getMenuInflater().inflate(R.menu.menu_sort_rating, p.getMenu());

        p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                movieRecyclerViewAdapter_rating.clearItems();
                movieRecyclerViewCurrentSize = 0;

                switch(item.getItemId()) {
                    case R.id.recent_date_order:
                        Collections.sort(ratingMovieList,new recent_date_order_comparator());
                        break;
                    case R.id.old_date_order:
                        Collections.sort(ratingMovieList,new old_date_order_comparator());
                        break;
                    case R.id.random_order:
                        Collections.shuffle(ratingMovieList);
                        break;
                }

                if(ratingMovieList.size() >= 10) {
                    for(int i = 0; i < 10; i++) {
                        new AddMovieRecyclerViewItem(ratingMovieList.get(i)).execute();
                    }
                } else {
                    for(int i = 0; i < ratingMovieList.size(); i++) {
                        new AddMovieRecyclerViewItem(ratingMovieList.get(i)).execute();
                    }
                }
                return false;
            }
        });
        p.show();
    }

    //랜덤 영화 리스트 받아서 ratingMovieList에 저장
    public class GetMovieListFromServer extends AsyncTask<Void, Void, String> {
        private AppCompatDialog loadingDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(loadingDialog == null) {
                loadingDialog = new AppCompatDialog(getParent());
                loadingDialog.setCancelable(false);
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                loadingDialog.setContentView(R.layout.dialog_loading);
                ImageView loading_icon = loadingDialog.findViewById(R.id.loading_icon_imageView);
                Animation anim = AnimationUtils.loadAnimation(getParent(),R.anim.loading);
                loading_icon.setAnimation(anim);
            }
            loadingDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            JSONObject jsonObject = null;


            try {
                jsonObject = new JSONObject();
                jsonObject.put("u_id",Static.u_id);

                HttpConnection httpConnection = new HttpConnection();
                result = httpConnection.request(Static.url+"get_random_list/", jsonObject, "POST");
                if(result == null)
                    return null;

                jarrayFromServer = new JSONArray(result);
                for (int i = 0; i < jarrayFromServer.length(); i++) {
                    JSONObject jObject = jarrayFromServer.getJSONObject(i);

                    MovieInformation movieInfo = new MovieInformation(
                            jObject.getInt("m_id")
                            , jObject.getString("title")
                            , (float)-1
                            , (float)-1
                            , Float.parseFloat(jObject.getString("score"))
                    );

                    ratingMovieList.add(movieInfo);
                    Log.d("isaacRate","m_id:"+movieInfo.getM_id()+", title:"+movieInfo.getTitle());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadingDialog.dismiss();
            if(s == null) {
                Toast.makeText(RatingActivity.this, "서버에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Collections.shuffle(ratingMovieList);

            if(ratingMovieList.size() >= 10) {
                for(int i = 0; i < 10; i++) {
                    new AddMovieRecyclerViewItem(ratingMovieList.get(i)).execute();
                }
            } else {
                for(int i = 0; i < ratingMovieList.size(); i++) {
                    new AddMovieRecyclerViewItem(ratingMovieList.get(i)).execute();
                }
            }

            rating_textView.setVisibility(View.VISIBLE);
        }
    }
    
    //MovieInformation 하나에 해당하는 영화를 크롤링하여 MovieRecyclerView에 추가
    public class AddMovieRecyclerViewItem extends AsyncTask{
        private AppCompatDialog loadingDialog;
        private String baseUrl = "https://movie.naver.com/movie/search/result.nhn?section=movie&query=";
        private String titleQuery;
        private Bitmap bitmap;
        private String movieTitle;
        private String movieYear;
        private MovieInformation movieInfo;

        public AddMovieRecyclerViewItem (MovieInformation movieInfo) {
            this.titleQuery = this.titleToTitleQueary(movieInfo.getTitle());
            this.bitmap = null;
            this.movieTitle = this.onlyTitleFromTitle(movieInfo.getTitle());
            this.movieYear = this.yearFromTitle(movieInfo.getTitle());
            this.movieInfo = movieInfo;
            movieRecyclerViewCurrentSize++;
        }


        //제목검색 url를 반환
        private String titleToTitleQueary(String title) {
            int index;
            String title_only = title;
            if((index = title.indexOf('(')) != -1){
                title_only = title.substring(0, index);
            }
            String[] component = title_only.split(" ");

            String titleQueary = "";
            try {
                for(int i = 0; i < component.length - 1; i++) {
                    titleQueary += URLEncoder.encode(component[i], "euc-kr") + "+";
                }
                titleQueary += URLEncoder.encode(component[component.length - 1], "euc-kr");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }

            return titleQueary;
        }

        private String onlyTitleFromTitle(String title) {
            int index;
            String title_only = title;
            if((index = title.indexOf('(')) != -1){
                return(title.substring(0, index));
            } else {
                return title;
            }
        }

        //제목에서 연도 추출
        private String yearFromTitle(String title) {
            int startIndex, endIndex;
            String year = null;
            if((startIndex = title.lastIndexOf('(')) != -1 && (endIndex = title.lastIndexOf(')')) != -1){
                year = title.substring(startIndex + 1, endIndex);
                return year;
            } else {
                return "";
            }
        }

        @Nullable
        private Bitmap getBitmapFromUrl (String url) {
            try {
                URL imageUrl = new URL(url);
                if (imageUrl.toString().compareTo("https://ssl.pstatic.net/static/m/movie/icons/OG_270_270.png") != 0) {
                    HttpURLConnection connect = (HttpURLConnection) imageUrl.openConnection();
                    connect.setDoInput(true);//서버로부터 응답수신
                    connect.connect();

                    //ImageView에 지정할 Bitmap을 만든다.
                    InputStream is = connect.getInputStream(); //InputStream 값 가져오기
                    return BitmapFactory.decodeStream(is); // Bitmap으로 변환
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return null;
        }

        private void crawlingMovie_fromNaverMovie_byQueary(String queary) {
            try {
                //제목으로 검색하여
                Document doc = Jsoup.connect(baseUrl + queary).get();

                int index = -1;
                //검색 리스트 중 제작년도 일치하는 영화 찾기
                if(movieYear.compareTo("") != 0) {
                    Elements year = doc.select("ul.search_list_1 li dl dd.etc");

                    for (index = 0; index < year.size(); ) {
                        Element e = year.get(index);
                        String elementYear = e.select("a").last().text();
                        if (elementYear.compareTo(this.movieYear) == 0) {
                            index /= 2;
                            break;
                        }
                        index += 2;
                    }
                }

                //찾은 영화로부터 코드 추출
                Elements result = doc.select("p.result_thumb a");
                String href = result.get(index).attr("href");
                this.movieInfo.setNaverMovieCode(href.substring(href.indexOf('=') + 1));

                Log.d("isaac: ",movieTitle +"의 코드: " + movieInfo.getNaverMovieCode());

                //찾은 코드로 영화 정보 크롤링
                String movieUrl = "https://movie.naver.com/movie/bi/mi/basic.nhn?code=";
                doc = Jsoup.connect(movieUrl + movieInfo.getNaverMovieCode()).get();

                //영화 제목 가져오기
                String title = doc.select("h3.h_movie a").first().text();
                if(title.compareTo("") != 0)
                    movieTitle = title;
                Log.d("title: ",movieTitle);

                //포스터 이미지 url 가져오기
                Elements image = doc.select("meta[property=og:image]");
                Log.d("isaac: ",image.toString());

                //url에서 비트맵으로 반환
                bitmap = getBitmapFromUrl(image.attr("content"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(loadingDialog == null) {
                loadingDialog = new AppCompatDialog(getParent());
                loadingDialog.setCancelable(false);
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                loadingDialog.setContentView(R.layout.dialog_loading);
                ImageView loading_icon = loadingDialog.findViewById(R.id.loading_icon_imageView);
                Animation anim = AnimationUtils.loadAnimation(getParent(),R.anim.loading);
                loading_icon.setAnimation(anim);
            }

            movieRecyclerViewLoading = true;
            if(movieRecyclerViewAdapter_rating.getItemCount() <= 3)
                loadingDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            crawlingMovie_fromNaverMovie_byQueary(titleQuery);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            loadingDialog.dismiss();

            if(jarrayFromServer == null) {
                Toast.makeText(RatingActivity.this, "서버에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(bitmap != null) {
                movieRecyclerViewAdapter_rating.addItem(new MovieRecyclerViewItem(bitmap, movieTitle, movieYear, movieInfo));
            }
            else {
                movieRecyclerViewAdapter_rating.addItem( new MovieRecyclerViewItem(
                        getDrawable(R.drawable.no_poster_info), movieTitle, movieYear, movieInfo));
            }

            movieRecyclerViewAdapter_rating.notifyDataSetChanged();

            movieRecyclerViewLoading = false;
        }
    }

    public class SetRateToServer extends AsyncTask {
        private AppCompatDialog loadingDialog;
        private int m_id;
        private float score;

        public SetRateToServer (int m_id, float score) {
            this.m_id = m_id;
            this.score = score;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(loadingDialog == null) {
                loadingDialog = new AppCompatDialog(getParent());
                loadingDialog.setCancelable(false);
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                loadingDialog.setContentView(R.layout.dialog_loading);
                ImageView loading_icon = loadingDialog.findViewById(R.id.loading_icon_imageView);
                Animation anim = AnimationUtils.loadAnimation(getParent(),R.anim.loading);
                loading_icon.setAnimation(anim);
            }
            loadingDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject();
                jsonObject.put("u_id", Static.u_id);
                jsonObject.put("m_id", m_id);
                jsonObject.put("score", this.score);

                HttpConnection httpConnection = new HttpConnection();
                String result = httpConnection.request(Static.url + "set_rate/", jsonObject, "POST");

                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            loadingDialog.dismiss();
            movieRecyclerViewAdapter_rating.notifyDataSetChanged();
        }
    }

    //볼 영화에 등록
    public class AddWishListToServer extends AsyncTask {
        AppCompatDialog loadingDialog;
        private int m_id;

        public AddWishListToServer (int m_id) {
            this.m_id = m_id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(loadingDialog == null) {
                loadingDialog = new AppCompatDialog(getParent());
                loadingDialog.setCancelable(false);
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                loadingDialog.setContentView(R.layout.dialog_loading);
                ImageView loading_icon = loadingDialog.findViewById(R.id.loading_icon_imageView);
                Animation anim = AnimationUtils.loadAnimation(getParent(),R.anim.loading);
                loading_icon.setAnimation(anim);
            }
            loadingDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject();
                jsonObject.put("u_id", Static.u_id);
                jsonObject.put("m_id", this.m_id);

                HttpConnection httpConnection = new HttpConnection();
                String result = httpConnection.request(Static.url + "add_wishList/", jsonObject, "POST");

                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            loadingDialog.dismiss();

        }
    }
}
