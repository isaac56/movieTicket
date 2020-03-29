views.py의 def fileToDB(~~~~에서
data = data_for_db('디렉토리')의 디렉토리는 '~~~/gp'

데이터는 db에 저장되어 있는 상태.(fileToDB 필요없음.)

호출 순서
fileToDB(http://localhost:8000/fileToDB/)
:파일에 있는 정보를 db로 전송.
↓
prepareSVDpp(http://localhost:8000/prepareSVDpp/)
:svd++를 진행하기 전에 total_data 클래스 객체 td 생성.
↓
doSVDpp(http://localhost:8000/doSVDpp/)
:svd 수행.
↓
getRecommendedMoviesListOfUser(http://localhost:8000/getRecommendationOfUser/'id 3자리'/)
:전달된 u_id에 대해 json 메시지를 반환함.

고려할 부분
:getRecommendedMoviesListOfUser의 반환값.
:새로운 레이팅의 처리방법.
db로 전송하는 방법.(어플리케이션에서 전송? 서버를 경유해 전송?)
db에 새로운 레이팅이 저장되고 사용자의 정보 변경은 어떻게 할 것인가.
db에서 갱신된 사용자 정보, 레이팅 정보를 서버에서 받아들이는 방법.
:추천리스트 갱신중 접근 제한.



