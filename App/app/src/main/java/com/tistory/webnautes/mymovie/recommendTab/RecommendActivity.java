package com.tistory.webnautes.mymovie.recommendTab;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.os.AsyncTask;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tistory.webnautes.mymovie.DetailMovieActivity;
import com.tistory.webnautes.mymovie.HttpConnection;
import com.tistory.webnautes.mymovie.R;
import com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView.MovieRecyclerViewLayoutManager;
import com.tistory.webnautes.mymovie.RecyclerViews.newMovieRecyclerView.NewMovieRecyclerViewAdapter;
import com.tistory.webnautes.mymovie.Static;
import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;
import com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView.MovieRecyclerViewAdapter_recommend;
import com.tistory.webnautes.mymovie.RecyclerViews.movieRecyclerView.MovieRecyclerViewItem;
import com.tistory.webnautes.mymovie.RecyclerViews.RecyclerViewClickListener;
import com.tistory.webnautes.mymovie.movieInformation.recent_date_order_comparator;
import com.tistory.webnautes.mymovie.movieInformation.old_date_order_comparator;
import com.tistory.webnautes.mymovie.movieInformation.high_score_order_comparator;
import com.tistory.webnautes.mymovie.movieInformation.low_score_order_comparator;

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
import java.util.Collection;
import java.util.Collections;

public class RecommendActivity extends AppCompatActivity {

    private JSONArray jarrayFromServer = null;
    private ArrayList<MovieInformation> recommendMovieList;
    private ArrayList<String> newMovieCodeList;

    private ScrollView recommend_scrollView;
    private TextView current_textView;
    private TextView recommend_textView;
    private ImageButton sort_menu_button;

    private RecyclerView movie_recyclerView;
    private MovieRecyclerViewLayoutManager movieLayoutManager;
    private MovieRecyclerViewAdapter_recommend movieRecyclerViewAdapter_recommend;
    int movieRecyclerViewCurrentSize;
    boolean movieRecyclerViewLoading;

    private RecyclerView newMovie_recyclerView;
    private LinearLayoutManager newMovieLayoutManager;
    private NewMovieRecyclerViewAdapter newMovieRecyclerViewAdapter;
    int newMovieRecyclerViewCurrentSize;
    boolean newMovieRecyclerViewLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        current_textView = (TextView)findViewById(R.id.current_textView);
        recommend_textView = (TextView)findViewById(R.id.recommend_textView);
        recommend_textView.setSelected(true);
        sort_menu_button = (ImageButton)findViewById(R.id.sort_menu_button);

        recommendMovieList = new ArrayList<MovieInformation>();
        newMovieCodeList = new ArrayList<String>();

        recommend_scrollView = (ScrollView)findViewById(R.id.recommend_scrollView);
        recommend_scrollView.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (recommend_scrollView.getChildAt(0).getBottom()
                                <= (recommend_scrollView.getHeight() + recommend_scrollView.getScrollY())) {
                            movieLayoutManager.setScrollEnabled(true);
                        } else {
                            movieLayoutManager.setScrollEnabled(false);
                        }
                    }
                });

        //movieRecyclerView 세팅
        movie_recyclerView = (RecyclerView) findViewById(R.id.movie_recyclerView);
        movieLayoutManager = new MovieRecyclerViewLayoutManager(this);
        movieLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        movieLayoutManager.setScrollEnabled(false);
        movieRecyclerViewAdapter_recommend = new MovieRecyclerViewAdapter_recommend();
        movieRecyclerViewCurrentSize = 0;
        movieRecyclerViewLoading = false;

        movie_recyclerView.setLayoutManager(movieLayoutManager);
        movie_recyclerView.setAdapter(movieRecyclerViewAdapter_recommend);
        movie_recyclerView.addOnItemTouchListener(
                new RecyclerViewClickListener(getApplicationContext(), movie_recyclerView, new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MovieRecyclerViewItem item =((MovieRecyclerViewAdapter_recommend)movie_recyclerView.getAdapter()).getItem(position);

                        Static.recommendTabMovieInfo = item.getMovieInfo();
                        Intent intent = new Intent(getApplicationContext(), DetailMovieActivity.class);

                        intent.putExtra("tabNumber", 1);
                        View detailMovieView = RecommendTabGroup.tabGroup.getLocalActivityManager()
                                .startActivity("DetailMovieActivity", intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                .getDecorView();

                        RecommendTabGroup.tabGroup.replaceView(detailMovieView);
                    }
                }));
        movie_recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (!movie_recyclerView.canScrollVertically(1) && movieRecyclerViewLoading == false) {
                    int index = movieRecyclerViewCurrentSize;
                    for(int i = 0; i < 3; i++, index++) {
                        if(index < recommendMovieList.size())
                            new AddMovieRecyclerViewItem(recommendMovieList.get(index)).execute();
                    }
                }
            }
        });

        //newMovierecyclerView 세팅
        newMovie_recyclerView = (RecyclerView) findViewById(R.id.newMovie_recyclerView);
        newMovieLayoutManager = new LinearLayoutManager(this);
        newMovieLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        newMovieRecyclerViewAdapter = new NewMovieRecyclerViewAdapter();
        newMovieRecyclerViewCurrentSize = 0;
        newMovieRecyclerViewLoading = false;

        newMovie_recyclerView.setLayoutManager(newMovieLayoutManager);
        newMovie_recyclerView.setAdapter(newMovieRecyclerViewAdapter);
        newMovie_recyclerView.addOnItemTouchListener(
                new RecyclerViewClickListener(getApplicationContext(), newMovie_recyclerView, new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MovieRecyclerViewItem item =((NewMovieRecyclerViewAdapter)newMovie_recyclerView.getAdapter()).getItem(position);

                        RecommendTabGroup parent = (RecommendTabGroup)getParent();
                        Static.recommendTabMovieInfo = item.getMovieInfo();

                        Intent intent = new Intent(getApplicationContext(), DetailMovieActivity.class);
                        intent.putExtra("tabNumber",1);
                        View detailMovieView = RecommendTabGroup.tabGroup.getLocalActivityManager()
                                .startActivity("DetailMovieActivity", intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                .getDecorView();

                        Log.d("isaac","newMovieList clicked");
                        RecommendTabGroup.tabGroup.replaceView(detailMovieView);
                    }
                }));
        newMovie_recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (!newMovie_recyclerView.canScrollHorizontally(1) && newMovieRecyclerViewLoading == false) {
                    int index = newMovieRecyclerViewCurrentSize;
                    for(int i = 0; i < 5; i++, index++) {
                        if(index < newMovieCodeList.size())
                            new AddNewMovieRecyclerViewItem(newMovieCodeList.get(index)).execute();
                    }
                }
            }
        });


        //json 테스트
        JSONObject user_info = null;
        try {
            user_info = new JSONObject();
            user_info.put("u_id","1");
            user_info.put("login_id", "isaac56");
            user_info.put("password", "tktlf55");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //개봉 영화 리스트 크롤링하여 코드로 저장
        GetNewListFromNaver getNewListFromNaver = new GetNewListFromNaver();
        getNewListFromNaver.execute();

        //추천 영화 리스트 받아서 recommendMovieList로 저장
        GetMovieListFromServer getMovieListFromServer = new GetMovieListFromServer();
        getMovieListFromServer.execute();
    }



    @Override
    public void onBackPressed() {
        RecommendTabGroup parent = (RecommendTabGroup)getParent();
        parent.onBackPressed();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        MovieInformation selecteMovieInfo;
        int position;

        switch(Static.recommendTab_which_recyclerView) {
            case 0:
                position = newMovieRecyclerViewAdapter.getLayoutPosition();
                selecteMovieInfo = newMovieRecyclerViewAdapter.getItem(position).getMovieInfo();
                switch (item.getItemId()) {
                    case 0:
                        new AddWishListToServer(selecteMovieInfo.getM_id()).execute();
                        break;
                    default:
                        break;
                }
                break;
            case 1:
                position = movieRecyclerViewAdapter_recommend.getLayoutPosition();
                selecteMovieInfo = movieRecyclerViewAdapter_recommend.getItem(position).getMovieInfo();
                switch (item.getItemId()) {
                    case 0:
                        new AddWishListToServer(selecteMovieInfo.getM_id()).execute();
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    //추천목록 정렬하기
    public void sort_menu_button_clicked(View v) {
        PopupMenu p = new PopupMenu(getApplicationContext(), v);
        getMenuInflater().inflate(R.menu.menu_sort, p.getMenu());

        p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                movieRecyclerViewAdapter_recommend.clearItems();
                movieRecyclerViewCurrentSize = 0;

                switch(item.getItemId()) {
                    case R.id.recent_date_order:
                        Collections.sort(recommendMovieList,new recent_date_order_comparator());
                        break;
                    case R.id.old_date_order:
                        Collections.sort(recommendMovieList,new old_date_order_comparator());
                        break;
                    case R.id.high_score_order:
                        Collections.sort(recommendMovieList,new high_score_order_comparator());
                        break;
                    case R.id.low_score_order:
                        Collections.sort(recommendMovieList,new low_score_order_comparator());
                        break;
                }

                if(recommendMovieList.size() >= 10) {
                    for(int i = 0; i < 10; i++) {
                        new AddMovieRecyclerViewItem(recommendMovieList.get(i)).execute();
                    }
                } else {
                    for(int i = 0; i < recommendMovieList.size(); i++) {
                        new AddMovieRecyclerViewItem(recommendMovieList.get(i)).execute();
                    }
                }
                return false;
            }
        });
        p.show();
    }

    //추천 영화 리스트 받아서 recommendMovieList에 저장
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
                result = httpConnection.request(Static.url+"get_recommend_list/", jsonObject, "POST");

                if(result == null)
                    return result;
                if(result.compareTo("recommend_list.calculating") == 0)
                    return result;

                jarrayFromServer = new JSONArray(result);
                for (int i = 0; i < jarrayFromServer.length(); i++) {
                    JSONObject jObject = jarrayFromServer.getJSONObject(i);

                    MovieInformation movieInfo = new MovieInformation(
                            jObject.getInt("m_id")
                            , jObject.getString("title")
                            , Float.parseFloat(jObject.getString("expectingScore"))
                            , Float.parseFloat(jObject.getString("averageScore"))
                            , Float.parseFloat(jObject.getString("score"))
                    );

                    recommendMovieList.add(movieInfo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            loadingDialog.dismiss();
            super.onPostExecute(s);
            if(s == null) {
                Toast.makeText(RecommendActivity.this, "서버에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(s.compareTo("recommend_list.calculating") == 0) {
                recommend_textView.setText("추천 영화 계산중..\n(15개 이상의 평가를 부탁드립니다.)");
                recommend_textView.setVisibility(View.VISIBLE);
                return;
            }

            Collections.sort(recommendMovieList,new recent_date_order_comparator());

            if(recommendMovieList.size() >= 10) {
                for(int i = 0; i < 10; i++) {
                    new AddMovieRecyclerViewItem(recommendMovieList.get(i)).execute();
                }
            } else {
                for(int i = 0; i < recommendMovieList.size(); i++) {
                    new AddMovieRecyclerViewItem(recommendMovieList.get(i)).execute();
                }
            }
            recommend_textView.setVisibility(View.VISIBLE);
            sort_menu_button.setVisibility(View.VISIBLE);
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
            if(movieRecyclerViewCurrentSize == 1)
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
                Toast.makeText(RecommendActivity.this, "서버에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(bitmap != null) {
                movieRecyclerViewAdapter_recommend.addItem(new MovieRecyclerViewItem(bitmap, movieTitle, movieYear, movieInfo));
            }
            else {
                movieRecyclerViewAdapter_recommend.addItem( new MovieRecyclerViewItem(
                        getDrawable(R.drawable.no_poster_info), movieTitle, movieYear, movieInfo));
            }

            movieRecyclerViewAdapter_recommend.notifyDataSetChanged();

            movieRecyclerViewLoading = false;
        }
    }

    //신작 리스트 크롤링하여 네이버영화코드를 newMovieCodeList에 저장
    public class GetNewListFromNaver extends AsyncTask{
        private AppCompatDialog loadingDialog;
        private String baseUrl = "https://movie.naver.com/movie/running/current.nhn";

        private void crawlingNewList_fromNaverMovie() {
            try {
                Document doc = Jsoup.connect(baseUrl).get();

                Elements elements = doc.select("div.thumb a");
                for(Element e : elements) {
                    String href = e.attr("href");
                    newMovieCodeList.add(href.substring(href.indexOf('=') + 1));
                    Log.d("newList",href.substring(href.indexOf('=') + 1));
                }
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
            loadingDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            crawlingNewList_fromNaverMovie();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            loadingDialog.dismiss();

            if(newMovieCodeList.size() >= 5) {
                for(int i = 0; i < 5; i++) {
                    new AddNewMovieRecyclerViewItem(newMovieCodeList.get(i)).execute();
                }
            } else {
                for(int i = 0; i < newMovieCodeList.size(); i++) {
                    new AddNewMovieRecyclerViewItem(newMovieCodeList.get(i)).execute();
                }
            }
            current_textView.setVisibility(View.VISIBLE);
        }
    }

    //네이버 영화코드에 해당하는 영화를 크롤링하여 NewMovieRecyclerView에 추가
    public class AddNewMovieRecyclerViewItem extends AsyncTask {
        AppCompatDialog loadingDialog;
        private String baseUrl = "https://movie.naver.com/movie/bi/mi/basic.nhn?code=";
        private String photoUrl = "https://movie.naver.com/movie/bi/mi//photoView.nhn?code=";

        String code;
        private Bitmap bitmap;
        private String movieTitle;
        private String movieYear;
        private MovieInformation movieInfo;

        public AddNewMovieRecyclerViewItem(String code) {
            this.code = code;
            newMovieRecyclerViewCurrentSize++;
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

        private void crawlingMovie_fromNaverMovie_byCode() {
            try {
                //찾은 코드로 영화 정보 크롤링
                Document doc = Jsoup.connect(baseUrl + code).get();

                //영화 제목 가져오기
                Element element = doc.select("div.mv_info_area div.mv_info").first();
                String title = element.select("h3.h_movie a").first().text();
                if(title.compareTo("") != 0)
                    movieTitle = title;

                //개봉일 크롤링
                Elements elements = doc.select("p.info_spec span");
                try {
                    movieYear = elements.get(3).text();
                } catch(Exception e) {
                    e.printStackTrace();
                }

                //영화 스틸컷 크롤링
                doc = Jsoup.connect(photoUrl + code).get();
                elements = doc.select("li._list");
                for(int index = 0; index < elements.size(); index++) {
                    JSONObject jsonObject = new JSONObject(elements.get(index).attr("data-json"));
                    bitmap = getBitmapFromUrl(jsonObject.getString("fullImageUrl886px"));
                    if(bitmap.getWidth() > bitmap.getHeight())
                        break;
                }

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

            newMovieRecyclerViewLoading = true;
            if(newMovieRecyclerViewCurrentSize == 1)
                loadingDialog.show();
        }

        private MovieInformation getExactMovieInfoFromServer (MovieInformation movieInfo) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject();
                jsonObject.put("u_id",Static.u_id);
                jsonObject.put("m_id", movieInfo.getM_id());
                if(movieInfo.getTitle() == null)
                    jsonObject.put("title", this.movieTitle + " "
                            + "(" + this.movieYear.substring(0,4) + ")");
                else
                    jsonObject.put("title", movieInfo.getTitle());

                HttpConnection httpConnection = new HttpConnection();
                String result = httpConnection.request(Static.url+"get_movie/", jsonObject, "POST");
                if(result == null)
                    return movieInfo;

                JSONObject jObject = new JSONObject(result);
                MovieInformation exactMovieInfo = new MovieInformation(jObject.getInt("m_id")
                        , jObject.getString("title")
                        , Float.parseFloat(jObject.getString("expectingScore"))
                        , Float.parseFloat(jObject.getString("averageScore"))
                        , Float.parseFloat(jObject.getString("score"))
                );
                exactMovieInfo.setNaverMovieCode(movieInfo.getNaverMovieCode());
                return exactMovieInfo;
            } catch (JSONException e) {
                e.printStackTrace();
                return movieInfo;
            }
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            crawlingMovie_fromNaverMovie_byCode();
            this.movieInfo = getExactMovieInfoFromServer(new MovieInformation(this.code));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            loadingDialog.dismiss();

            newMovieRecyclerViewAdapter.addItem(new MovieRecyclerViewItem(bitmap, movieTitle, movieYear, movieInfo));

            newMovieRecyclerViewAdapter.notifyDataSetChanged();

            newMovieRecyclerViewLoading = false;
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