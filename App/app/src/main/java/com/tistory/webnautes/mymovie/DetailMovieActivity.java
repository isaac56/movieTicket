package com.tistory.webnautes.mymovie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tistory.webnautes.mymovie.movieInformation.MovieInformation;
import com.tistory.webnautes.mymovie.RecyclerViews.commentRecyclerView.CommentRecyclerViewAdapter;
import com.tistory.webnautes.mymovie.RecyclerViews.commentRecyclerView.CommentRecyclerViewItem;
import com.tistory.webnautes.mymovie.RecyclerViews.personRecyclerView.PersonRecyclerViewAdapter;
import com.tistory.webnautes.mymovie.RecyclerViews.photoRecyclerView.PhotoRecyclerViewAdapter;
import com.tistory.webnautes.mymovie.RecyclerViews.RecyclerViewClickListener;
import com.tistory.webnautes.mymovie.ratingTab.RatingTabGroup;
import com.tistory.webnautes.mymovie.recommendTab.RecommendTabGroup;
import com.tistory.webnautes.mymovie.wishListTab.WishListTabGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetailMovieActivity extends AppCompatActivity {
    private AppCompatDialog loadingDialog;
    private MovieInformation movieInfo;

    private ImageView poster_background_imageView;
    private RadiusImageView poster_imageView;
    private TextView movieTitle_textView;
    private TextView movieTitleSub_textView;
    private TextView movieSpec_textView;
    private TextView movieYear_textView;
    private TextView filmRate_textView;
    private TextView movieStory_textView;
    private RatingBar ratingBar;
    private TextView averageScore_textView;
    private TextView score_textView;

    //감독 및 출연진 recycler view
    private RecyclerView person_recyclerView;
    private PersonRecyclerViewAdapter personRecyclerViewAdapter;
    private LinearLayoutManager personLayoutManager;

    //스틸컷 recycler view
    private RecyclerView photo_recyclerView;
    private PhotoRecyclerViewAdapter photoRecyclerViewAdapter;
    private LinearLayoutManager photoLayoutManager;
    private AppCompatDialog photo_dialog;

    //코멘트 recycler view
    private RecyclerView comment_recyclerView;
    private CommentRecyclerViewAdapter commentRecyclerViewAdapter;
    private LinearLayoutManager commentLayoutManager;
    private EditText comment_editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_movie);

        //loading dialog 생성
        loadingDialog = new AppCompatDialog(getParent());
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingDialog.setContentView(R.layout.dialog_loading);
        ImageView loading_icon = (ImageView) loadingDialog.findViewById(R.id.loading_icon_imageView);
        Animation anim = AnimationUtils.loadAnimation(getParent(),R.anim.loading);
        loading_icon.setAnimation(anim);

        //layout 요소 바인딩
        movieTitle_textView = findViewById(R.id.movieTitle_textView);
        poster_background_imageView = findViewById(R.id.poster_background_imageView);
        poster_imageView = findViewById(R.id.poster_imageView);
        movieTitle_textView.setSelected(true);

        movieTitleSub_textView = findViewById(R.id.movieTitleSub_textView);
        movieSpec_textView = findViewById(R.id.movieSpac_textView);
        movieYear_textView = findViewById(R.id.movieYear_textView);
        filmRate_textView = findViewById(R.id.filmRate_textView);
        movieStory_textView = findViewById(R.id.movieStory_textView);

        averageScore_textView = findViewById(R.id.averageScore_textView);
        score_textView = findViewById(R.id.score_textView);
        ratingBar = findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(fromUser == true) {
                    score_textView.setText("나의평점: " + rating);
                    new SetRateToServer(rating).execute();
                }
            }
        });

        //출연진 recycler view 세팅
        person_recyclerView = (RecyclerView) findViewById(R.id.person_recyclerView);
        personLayoutManager = new LinearLayoutManager(this);
        personLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        personRecyclerViewAdapter = new PersonRecyclerViewAdapter();

        person_recyclerView.setLayoutManager(personLayoutManager);
        person_recyclerView.setAdapter(personRecyclerViewAdapter);

        comment_editText = (EditText) findViewById(R.id.comment_editText);
        //코멘트 recycler view 세팅
        comment_recyclerView = (RecyclerView) findViewById(R.id.comment_recyclerView);
        commentLayoutManager = new LinearLayoutManager(this);
        commentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentRecyclerViewAdapter = new CommentRecyclerViewAdapter();

        comment_recyclerView.setLayoutManager(commentLayoutManager);
        comment_recyclerView.setAdapter(commentRecyclerViewAdapter);

        //스틸컷 recycler view 세팅
        photo_recyclerView = (RecyclerView) findViewById(R.id.photo_recyclerView);
        photoLayoutManager = new LinearLayoutManager(this);
        photoLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        photoRecyclerViewAdapter = new PhotoRecyclerViewAdapter();

        photo_recyclerView.setLayoutManager(photoLayoutManager);
        photo_recyclerView.setAdapter(photoRecyclerViewAdapter);
        photo_recyclerView.addOnItemTouchListener(
                new RecyclerViewClickListener(getApplicationContext(), photo_recyclerView, new RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        ImageView photo_dialog_imageView = (ImageView) photo_dialog.findViewById(R.id.moviePhoto_imageView);
                        Bitmap photo_dialog_image =((PhotoRecyclerViewAdapter)photo_recyclerView.getAdapter()).getItem(position);
                        photo_dialog_imageView.setImageBitmap(photo_dialog_image);
                        photo_dialog.show();

                        //다이얼로그 사이즈의 변경
                        /////////////////////////////////////////////
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);

                        Window window = photo_dialog.getWindow();

                        int x = (int)(size.x * 1.0f);
                        int y = (int)(size.y * 0.5f);

                        window.setLayout(x,y);
                        /////////////////////////////////////////////
                    }
                }));

        //스틸컷 dialog 생성
        photo_dialog = new AppCompatDialog(getParent());
        photo_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        photo_dialog.setContentView(R.layout.dialog_photo);

        int tabNumber = getIntent().getIntExtra("tabNumber",0);
        switch(tabNumber) {
            case 1:
                movieInfo = Static.recommendTabMovieInfo;
                new CrawlingDetailFromNaverMovie().execute();
                break;
            case 2:
                movieInfo = Static.ratingTabMovieInfo;
                new CrawlingDetailFromNaverMovie().execute();
                break;
            case 3:
                movieInfo = Static.wishListTabMovieInfo;
                new CrawlingDetailFromNaverMovie().execute();
                break;
            default:
                movieInfo = new MovieInformation(-1,"곰돌이 푸 (2018)",(float)3.2,(float)4.5);
                movieInfo.setNaverMovieCode("168023");
                new CrawlingDetailFromNaverMovie().execute();
                break;
        }
        new GetCommentsFromServer().execute();
    }

    public void addWishList_button_clicked() {
        new AddWishListToServer(movieInfo.getM_id()).execute();
    }

    @Override
    public void onBackPressed() {
        int tabNumber = getIntent().getIntExtra("tabNumber",0);
        switch(tabNumber) {
            case 1:
                RecommendTabGroup parent1 = (RecommendTabGroup)getParent();
                parent1.onBackPressed();
                break;
            case 2:
                RatingTabGroup parent2 = (RatingTabGroup)getParent();
                parent2.onBackPressed();
                break;
            case 3:
                WishListTabGroup parent3 = (WishListTabGroup)getParent();
                parent3.onBackPressed();
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    public void comment_button_clicked(View v) {
        new AddCommentToServer(comment_editText.getText().toString()).execute();
        new GetCommentsFromServer().execute();
    }

    public class CrawlingDetailFromNaverMovie extends AsyncTask {
        private String baseUrl = "https://movie.naver.com/movie/bi/mi/basic.nhn?code=";
        private String castUrl = "https://movie.naver.com/movie/bi/mi//detail.nhn?code=";
        private String photoUrl = "https://movie.naver.com/movie/bi/mi//photoView.nhn?code=";
        private Bitmap posterBitmap;
        private String movieTitle;
        private String movieTitleSub;
        private String movieStory;
        private String movieGenre;
        private String movieNation;
        private String movieRunningTime;
        private String movieYear;
        private String filmRate;
        private Pair<Bitmap, String> movieDirector;

        public CrawlingDetailFromNaverMovie () {
            this.posterBitmap = null;
            this.movieTitle = this.onlyTitleFromTitle(movieInfo.getTitle());
            this.movieTitleSub = "";
            this.movieYear = this.yearFromTitle(movieInfo.getTitle());

            String no_information = "";
            this.movieGenre = no_information;
            this.movieNation = no_information;
            this.movieRunningTime = no_information;
            this.filmRate = no_information;
            this.movieStory = no_information;
        }

        //(1999)형식의 연도 뺀 제목만 반환
        private String onlyTitleFromTitle(String title) {
            if(title == null) return "";
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
            if(title == null) return "";
            int startIndex, endIndex;
            String year = null;
            if((startIndex = title.indexOf('(')) != -1 && (endIndex = title.indexOf(')')) != -1){
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

        private void getMovieInfoFromCode(String code) {
            try {
                //찾은 코드로 영화 정보 크롤링
                Document doc = Jsoup.connect(baseUrl + code).get();

                Element element = null;
                Elements elements = null;
                //영화 제목 가져오기
                try {
                    element = doc.select("div.mv_info_area div.mv_info").first();
                    String title = element.select("h3.h_movie a").first().text();
                    if (title.compareTo("") != 0)
                        movieTitle = title;
                    movieTitleSub = element.select("strong.h_movie2").text();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //장르, 국가, 러닝타임, 개봉일, 영화등급 크롤링
                try {
                    elements = doc.select("p.info_spec span");
                    movieGenre = elements.get(0).text();
                    movieNation = elements.get(1).text();
                    movieRunningTime = elements.get(2).text();
                    movieYear = elements.get(3).text();
                    filmRate = elements.get(4).text();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                //줄거리 가져오기
                try {
                    movieStory = doc.select("p.con_tx").first().text();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //포스터 이미지 url 가져온 뒤 bitmap 이미지로 변환
                try {
                    Elements image = doc.select("meta[property=og:image]");
                    Log.d("isaac: ", image.toString());
                    posterBitmap = getBitmapFromUrl(image.attr("content"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //감독, 출연진 정보 크롤링
                try {
                    doc = Jsoup.connect(castUrl + code).get();

                    Elements photoes_kNames = doc.select("p.thumb_dir a img");
                    Elements eNames_parts = doc.select("div.dir_product");
                    String imageUrl = photoes_kNames.get(0).attr("src");
                    Bitmap imageBitmap = getBitmapFromUrl(imageUrl);
                    String kName = photoes_kNames.get(0).attr("alt");
                    String eName = eNames_parts.get(0).select("em.e_name").text();
                    String part = "감독";
                    personRecyclerViewAdapter.addItem(new PersonInformation(imageBitmap, kName, eName, part));

                    photoes_kNames = doc.select("p.p_thumb a img");
                    eNames_parts = doc.select("div.p_info");
                    for (int index = 0; index < photoes_kNames.size(); index++) {
                        imageUrl = photoes_kNames.get(index).attr("src");
                        imageBitmap = getBitmapFromUrl(imageUrl);
                        kName = photoes_kNames.get(index).attr("alt");
                        eName = eNames_parts.get(index).select("em.e_name").text();
                        part = eNames_parts.get(index).select("p.pe_cmt span").text();
                        personRecyclerViewAdapter.addItem(new PersonInformation(imageBitmap, kName, eName, part));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //영화 스틸컷 크롤링
                try {
                    doc = Jsoup.connect(photoUrl + code).get();
                    elements = doc.select("li._list");
                    for (int index = 0; index < elements.size(); index++) {
                        JSONObject jsonObject = new JSONObject(elements.get(index).attr("data-json"));
                        photoRecyclerViewAdapter.addItem(getBitmapFromUrl(jsonObject.getString("fullImageUrl886px")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private MovieInformation getExactMovieInfoFromServer () {
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
                return new MovieInformation(jObject.getInt("m_id")
                        , jObject.getString("title")
                        , Float.parseFloat(jObject.getString("expectingScore"))
                        , Float.parseFloat(jObject.getString("averageScore"))
                        , Float.parseFloat(jObject.getString("score"))
                );
            } catch (JSONException e) {
                e.printStackTrace();
                return movieInfo;
            }
        }
        ///////////////////////////////////////////////////////////////////////////////
        //이미지 블러 코드 from googling
        /**
         * Stack Blur v1.0 from
         * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
         * Java Author: Mario Klingemann <mario at quasimondo.com>
         * http://incubator.quasimondo.com
         *
         * created Feburary 29, 2004
         * Android port : Yahel Bouaziz <yahel at kayenko.com>
         * http://www.kayenko.com
         * ported april 5th, 2012
         *
         * This is a compromise between Gaussian Blur and Box blur
         * It creates much better looking blurs than Box Blur, but is
         * 7x faster than my Gaussian Blur implementation.
         *
         * I called it Stack Blur because this describes best how this
         * filter works internally: it creates a kind of moving stack
         * of colors whilst scanning through the image. Thereby it
         * just has to add one new block of color to the right side
         * of the stack and remove the leftmost color. The remaining
         * colors on the topmost layer of the stack are either added on
         * or reduced by one, depending on if they are on the right or
         * on the left side of the stack.
         *
         * If you are using this algorithm in your code please add
         * the following line:
         * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
         */

        public Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

            int width = Math.round(sentBitmap.getWidth() * scale);
            int height = Math.round(sentBitmap.getHeight() * scale);
            sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

            if (radius < 1) {
                return (null);
            }

            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
            Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int dv[] = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }

            Log.e("pix", w + " " + h + " " + pix.length);
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);

            return (bitmap);
        }
        ///////////////////////////////////////////////////////////////////////////////

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            getMovieInfoFromCode(movieInfo.getNaverMovieCode());
            movieInfo = getExactMovieInfoFromServer();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            loadingDialog.dismiss();

            boolean setPosterBackground = false;
            for(Bitmap bitmap : photoRecyclerViewAdapter.getItems()) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                if(width > height) {
                    poster_background_imageView.setImageBitmap(fastblur(bitmap,1,10));
                    setPosterBackground = true;
                    break;
                }
            }
            if(setPosterBackground == false) {
                poster_background_imageView.setImageDrawable(getDrawable(R.drawable.poster_background));
            }
            poster_background_imageView.setAlpha(980);

            if(posterBitmap != null)
                poster_imageView.setImageBitmap(posterBitmap);
            else
                poster_imageView.setImageDrawable(getDrawable(R.drawable.no_poster_info));

            movieTitle_textView.setText(movieTitle);

            movieTitleSub_textView.setText(movieTitleSub);

            movieSpec_textView.setText(movieGenre + " | " + movieNation + " | " + movieRunningTime);

            movieYear_textView.setText(movieYear);

            filmRate_textView.setText(filmRate);


            personRecyclerViewAdapter.notifyDataSetChanged();

            photoRecyclerViewAdapter.notifyDataSetChanged();

            movieStory_textView.setText(movieStory);

            if(movieInfo.getAverageScore() != -1)
                averageScore_textView.setText("평균평점: " + movieInfo.getAverageScore());
            else
                averageScore_textView.setText("평균평점: 없음");

            if(movieInfo.getScore() != -1) {
                score_textView.setText("나의평점: " + movieInfo.getScore());
                ratingBar.setRating(movieInfo.getScore());
            }
            else {
                if(movieInfo.getExpectingScore() != -1) {
                    score_textView.setText("예상평점: " + movieInfo.getExpectingScore());
                    ratingBar.setRating(movieInfo.getExpectingScore());
                } else {
                    score_textView.setText("예상평점: 없음");
                }
            }
            ratingBar.setVisibility(View.VISIBLE);
        }
    }

    public class SetRateToServer extends AsyncTask {
        private float score;

        public SetRateToServer (float score) {
            this.score = score;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject();
                jsonObject.put("u_id", Static.u_id);
                jsonObject.put("m_id", movieInfo.getM_id());
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
            if (Static.ratingActivity != null) {
                Static.ratingActivity.getMovieRecyclerViewAdapter_rating().setItemScore(movieInfo.getM_id(), this.score);
                Static.ratingActivity.getMovieRecyclerViewAdapter_rating().notifyDataSetChanged();
            }
        }
    }

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

    public class AddCommentToServer extends AsyncTask {
        AppCompatDialog loadingDialog;
        private String comment;

        public AddCommentToServer (String comment) {
            this.comment = comment;
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
                jsonObject.put("m_id", movieInfo.getM_id());
                jsonObject.put("comment", this.comment);
                HttpConnection httpConnection = new HttpConnection();
                String result = httpConnection.request(Static.url + "add_comment/", jsonObject, "POST");

                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            loadingDialog.dismiss();
            if(((String)o).compareTo("comment.add.aleadyExist") == 0){
                Toast.makeText(DetailMovieActivity.this,"이미 코멘트가 존재합니다.",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(DetailMovieActivity.this,"코멘트가 등록되었습니다.",Toast.LENGTH_LONG).show();
            }
        }
    }

    public class GetCommentsFromServer extends AsyncTask {
        AppCompatDialog loadingDialog;
        private JSONArray jsonArray;

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
                jsonObject.put("m_id", movieInfo.getM_id());
                HttpConnection httpConnection = new HttpConnection();
                String result = httpConnection.request(Static.url + "get_comments/", jsonObject, "POST");
                jsonArray = new JSONArray(result);
                commentRecyclerViewAdapter.clearItem();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    commentRecyclerViewAdapter.addItem(new CommentRecyclerViewItem(jObject.getString("comment")
                    ,jObject.getString("login_id")
                    ,jObject.getString("date")));
                }
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            loadingDialog.dismiss();
            commentRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

}