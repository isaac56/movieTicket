import math
import os
import random
import numpy as np
import json
import datetime
from movie.models import User, Movie, Rating
from collections import OrderedDict

# genre
_unknown = 0
_action = 1
_adventure = 2
_animation = 3
_children = 4
_comedy = 5
_crime = 6
_documentary = 7
_drama = 8
_fantasy = 9
_film_noir = 10
_horror = 11
_musical = 12
_mystery = 13
_romance = 14
_sci_fi = 15
_thriller = 16
_war = 17
_western = 18

# 옵션
_gamma = 0.01
_lambda = 0.02
# 추천작품 선정 기준
_threshold = 3.8
# _gamma 값 감소 횟수
# error 값이 줄어들다가 커지면
# _gamma 값을 감소시키고 다시 시도한다
_reduce_gamma_limit = 1

# occupation
_administrator = 0
_artist = 1
_doctor = 2
_educator = 3
_engineer = 4
_entertainment = 5
_executive = 6
_healthcare = 7
_homemaker = 8
_lawyer = 9
_librarian = 10
_marketing = 11
_none = 12
_other = 13
_programmer = 14
_retired = 15
_salesman = 16
_scientist = 17
_student = 18
_technician = 19
_writer = 20

# gender
_male = 0
_female = 1


class simple_movie:
    # 하나의 추천 영화를 저장하기 위한 클래스
    # 간단하게 id와 예측된 레이팅을 저장
    def __init__(self, m_id, m_rating):
        self.movie_id = m_id
        self.rating = m_rating


class top_n:
    # simple_movie 클래스의 리스트를 사용해
    # 추천 영화들을 저장하기 위한 클래스
    def __init__(self):
        self.movies = []

    def add(self, m_id, m_rating):
        # 일정 이상의 예측 레이팅을 지닌 영화 만을 추가
        # 매번 내림차순으로 정렬
        if m_rating >= _threshold:
            s_m = simple_movie(m_id, m_rating)

            self.movies.append(s_m)

            idx = len(self.movies) - 1

            while (1):
                if idx == 0:
                    break

                if self.movies[idx].rating > self.movies[idx - 1].rating:
                    tmp = self.movies[idx - 1]
                    self.movies[idx - 1] = self.movies[idx]
                    self.movies[idx] = tmp
                    idx -= 1
                else:
                    break

    def clear(self):
        self.movies.clear()


class user_info:
    # 사용자 한 명의 정보를 저장
    def __init__(self, _id):
        self.id = _id
        self.is_being_written = False  # 수정중인가
        self.recommended_movies = top_n()


def convert_gender(_gender):
    if _gender == "M":
        return _male
    if _gender == "F":
        return _female
    return -1


def convert_occupation(_occupation):
    if _occupation == "administrator":
        return _administrator
    if _occupation == "artist":
        return _artist
    if _occupation == "doctor":
        return _doctor
    if _occupation == "educator":
        return _educator
    if _occupation == "engineer":
        return _engineer
    if _occupation == "entertainment":
        return _entertainment
    if _occupation == "executive":
        return _executive
    if _occupation == "healthcare":
        return _healthcare
    if _occupation == "homemaker":
        return _homemaker
    if _occupation == "lawyer":
        return _lawyer
    if _occupation == "librarian":
        return _librarian
    if _occupation == "marketing":
        return _marketing
    if _occupation == "none":
        return _none
    if _occupation == "other":
        return _other
    if _occupation == "programmer":
        return _programmer
    if _occupation == "retired":
        return _retired
    if _occupation == "salesman":
        return _salesman
    if _occupation == "scientist":
        return _scientist
    if _occupation == "student":
        return _student
    if _occupation == "technician":
        return _technician
    if _occupation == "writer":
        return _writer
    return -1


class total_user:
    # 전체 사용자의 정보를 저장
    # id를 바로 인덱스로 넣을 수 있게 _user_num + 1개로 리스트 구성
    # content-based profile은 별도로 저장
    def __init__(self, _user_num):
        self.user_list = [-1] * (_user_num + 1)
        self.user_list[0] = user_info(-1)

    def read_user_data(self, _user_info_file):
        print("=======================================")
        print("-- total_user.read_user_data() start--")
        print(datetime.datetime.now())

        for line in _user_info_file:
            if line == "":
                break
            token_list = line.split("|")
            user_id = int(token_list[0])

            self.user_list[user_id] = user_info(user_id)

        missing_users = []
        for i in range(1, len(self.user_list)):
            if self.user_list[i] == -1:
                missing_users.append(i)

        print("missing users list")
        for i in missing_users:
            print(i)
        print("missing users end")

        print("-- total_user.read_user_data() end --")
        print(datetime.datetime.now())
        print("=======================================")

    def read_user_data_from_db(self, user_num):
        # 사실 db에서 안 읽음 ☆
        print("=======================================")
        print("-- total_user.read_user_data_from_db() start--")
        print(datetime.datetime.now())

        for i in range(1, user_num + 1):
            self.user_list[i] = user_info(i)

        print("user num : " + str(user_num))

        print("-- total_user.read_user_data_from_db() end --")
        print(datetime.datetime.now())
        print("=======================================")


class movie_info:
    # 영화 하나의 정보를 저장
    def __init__(self, _id, _title):
        self.id = _id
        self.title = _title
        self.total_rate = 0.0
        self.total_rate_num = 0.0

    def add(self, rate):
        self.total_rate = self.total_rate + rate
        self.total_rate_num = self.total_rate_num + 1.0


class total_movie:
    # 전체 영화의 정보를 저장
    # id를 바로 인덱스로 사용할 수 있게 _movie_num + 1개로 리스트 구성
    # content-based profile은 별도로 저장
    def __init__(self, _movie_num):
        self.movie_list = [-1] * (_movie_num + 1)
        self.movie_list[0] = movie_info(-1, "none")

    def read_movie_data(self, _movie_info_file):
        print("=======================================")
        print("-- total_movie.read_movie_data() start--")
        print(datetime.datetime.now())

        for line in _movie_info_file:
            if line == "":
                break
            elements_list = line.split("|")

            movie_id = int(elements_list[0])

            self.movie_list[movie_id] = movie_info(movie_id, elements_list[1])

        missing_movies = []
        for i in range(1, len(self.movie_list)):
            if self.movie_list[i] == -1:
                missing_movies.append(i)

        print("missing movies list")
        for i in missing_movies:
            print(i)
        print("missing movies end")

        print("-- total_movie.read_movie_data() end --")
        print(datetime.datetime.now())
        print("=======================================")

    def read_movie_data_from_db(self, movie_num):
        print("=======================================")
        print("-- total_movie.read_movie_data_from_db --")
        print(datetime.datetime.now())

        count = 0

        for i in range(1, movie_num + 1):
            movie_from_db = Movie.objects.get(m_id=i)
            self.movie_list[i] = movie_info(i, movie_from_db.title)
            count += 1

        print("movie num : " + str(movie_num))
        print("count : " + str(count))

        print("-- total_movie.read_movie_data_from_db --")
        print(datetime.datetime.now())
        print("=======================================")


class total_data:
    # 사용자, 영화 정보와 content-based profile
    # svd++를 위한 행렬들을 저장.
    def __init__(self):
        print("==================================")
        print("-- total_data initializiing start --")
        print(datetime.datetime.now())

        # 사용자 수
        self.user_num = User.objects.filter().count()

        # 영화 수
        self.movie_num = Movie.objects.filter().count()

        # 레이팅 수
        self.rating_num = Rating.objects.filter().count()

        # 아이디
        self.users = total_user(self.user_num)
        self.users.read_user_data_from_db(self.user_num)

        # 아이디, 타이틀
        self.movies = total_movie(self.movie_num)
        self.movies.read_movie_data_from_db(self.movie_num)

        # 사용자와 영화의 content-based profile
        # 장르별 평균 평점 19개
        # user_num + 1 을 사용한 이유는 사용자 id를 바로 대입해 참조하기 위함이다.
        self.user_profile = np.zeros((self.user_num + 1, 19))
        # 장르 정보 19개
        self.movie_profile = np.zeros((19, self.movie_num + 1))

        # svd++를 위한 행렬
        # 레이팅 행렬 = u * s * m

        # self.user_profile, self.movie_profile은 원본
        # self.u, self.s, self.m은 svd++를 위한 행렬으로
        # svd++ 진행과 함꼐 수정이 가해진다
        # 사용자 content-based profile
        self.u = np.zeros((self.user_num + 1, 19))
        # 장르별 가중치
        self.s = np.zeros(19)
        # 영화 content-based profile
        self.m = np.zeros((19, self.movie_num + 1))
        # 전채 (실제)레이팅의 평균에 대한
        # 개개의 사용자, 영화에 대한 레이팅의 평균의 편차
        self.u_dev = np.zeros(self.user_num + 1)
        self.m_dev = np.zeros(self.movie_num + 1)

        # 실제 레이팅을 저장하는 매트릭스
        self.rating_info = np.full((self.user_num + 1, self.movie_num + 1), -1.0)

        # 예측 값을 저장하는 매트릭스
        self.prediction_result = np.full((self.user_num + 1, self.movie_num + 1), -1.0)

        # 영화의 content-based profile(장르 정보)을 저장
        self.read_movie_genre_data()
        # 레이팅과 사용자의 content-based profile(장르 정보) 저장
        # 사용자 프로필에 장르별로 평점과 레이팅의 수를 누적시킨다
        self.read_rating()
        # 누적시킨 장르별 평점에 대해 장르별 레이팅 수로
        # 나누기를 수행 content-based profile을 완성
        self.read_user_genre_data()
        # user_profile과 movie_profile을 u와 m으로 복사
        self.copy_profiles_to_u_and_m()
        # s를 임의의 값으로 초기화
        for i in range(19):
            self.s[i] = random.random()
        # 전체 레이팅의 평균과 사용자 분산값, 영화 분산값을 계산
        self.avg = self.calculate_avg()
        self.calculate_u_dev(self.avg)
        self.calculate_m_dev(self.avg)

        print("-- total_data initializing end --")
        print("==================================")

    def read_movie_genre_data(self):
        print("===============================================")
        print("-- total_data.read_movie_genre_data() start --")
        print(datetime.datetime.now())
        count = 0

        for i in range(1, self.movie_num + 1):
            movie_from_db = Movie.objects.get(m_id=i)
            self.movie_profile[_unknown][i] = movie_from_db.is_unknown
            self.movie_profile[_action][i] = movie_from_db.is_action
            self.movie_profile[_adventure][i] = movie_from_db.is_adventure
            self.movie_profile[_animation][i] = movie_from_db.is_animation
            self.movie_profile[_children][i] = movie_from_db.is_children
            self.movie_profile[_comedy][i] = movie_from_db.is_comedy
            self.movie_profile[_crime][i] = movie_from_db.is_crime
            self.movie_profile[_documentary][i] = movie_from_db.is_documentary
            self.movie_profile[_drama][i] = movie_from_db.is_drama
            self.movie_profile[_fantasy][i] = movie_from_db.is_fantasy
            self.movie_profile[_film_noir][i] = movie_from_db.is_film_noir
            self.movie_profile[_horror][i] = movie_from_db.is_horror
            self.movie_profile[_musical][i] = movie_from_db.is_musical
            self.movie_profile[_mystery][i] = movie_from_db.is_mystery
            self.movie_profile[_romance][i] = movie_from_db.is_romance
            self.movie_profile[_sci_fi][i] = movie_from_db.is_sci_fi
            self.movie_profile[_thriller][i] = movie_from_db.is_thriller
            self.movie_profile[_war][i] = movie_from_db.is_war
            self.movie_profile[_western][i] = movie_from_db.is_western
            count += 1

        print("movie_num : " + str(self.movie_num))
        print("count : " + str(count))

        print("-- total_data.read_movie_genre_data() end --")
        print(datetime.datetime.now())
        print("===============================================")

    def read_rating(self):
        print("===============================================")
        print("-- total_data.read_rating() start --")
        print(datetime.datetime.now())

        count = 0

        for i in range(1, self.rating_num + 1):
            if i % 2000 == 0:
                print("rating : " + str(i))
            rating_from_db = Rating.objects.get(r_id=i)
            self.movies.movie_list[rating_from_db.movie.m_id].add(rating_from_db.rate)
            self.rating_info[rating_from_db.user.u_id][rating_from_db.movie.m_id] = rating_from_db.rate
            count += 1

        print("rating_num : " + str(self.rating_num))
        print("count : " + str(count))

        print("-- total_data.read_rating() end --")
        print(datetime.datetime.now())
        print("===============================================")

    def read_user_genre_data(self):
        print("===============================================")
        print("-- total_data.read_user_genre_data() start --")
        print(datetime.datetime.now())

        count = 0

        for i in range(1, self.user_num + 1):
            user_from_db = User.objects.get(u_id=i)
            self.user_profile[i][_unknown] = user_from_db.unknown_rate
            self.user_profile[i][_action] = user_from_db.action_rate
            self.user_profile[i][_adventure] = user_from_db.adventure_rate
            self.user_profile[i][_animation] = user_from_db.animation_rate
            self.user_profile[i][_children] = user_from_db.children_rate
            self.user_profile[i][_comedy] = user_from_db.comedy_rate
            self.user_profile[i][_crime] = user_from_db.crime_rate
            self.user_profile[i][_documentary] = user_from_db.documentary_rate
            self.user_profile[i][_drama] = user_from_db.drama_rate
            self.user_profile[i][_fantasy] = user_from_db.fantasy_rate
            self.user_profile[i][_film_noir] = user_from_db.film_noir_rate
            self.user_profile[i][_horror] = user_from_db.horror_rate
            self.user_profile[i][_musical] = user_from_db.musical_rate
            self.user_profile[i][_mystery] = user_from_db.mystery_rate
            self.user_profile[i][_romance] = user_from_db.romance_rate
            self.user_profile[i][_sci_fi] = user_from_db.sci_fi_rate
            self.user_profile[i][_thriller] = user_from_db.thriller_rate
            self.user_profile[i][_war] = user_from_db.war_rate
            self.user_profile[i][_western] = user_from_db.western_rate
            count += 1

        print("user_num : " + str(self.user_num))
        print("count : " + str(count))

        print("-- total_data.read_user_genre_data() end --")
        print(datetime.datetime.now())
        print("===============================================")

    def copy_profiles_to_u_and_m(self):
        print("===================================================")
        print("-- total_data.copy_profiles_to_u_and_m() start --")
        print(datetime.datetime.now())

        for i in range(1, self.user_num + 1):
            for j in range(19):
                self.u[i][j] = self.user_profile[i][j]

        for i in range(1, self.movie_num + 1):
            for j in range(19):
                self.m[j][i] = self.movie_profile[j][i]

        print("-- total_data.copy_profiles_to_u_and_m() end --")
        print(datetime.datetime.now())
        print("===================================================")

    def calculate_avg(self):
        print("===================================================")
        print("-- total_data.calculate_avg() start --")
        print(datetime.datetime.now())

        count = 0
        avg = 0

        for i in range(1, self.user_num + 1):
            for j in range(1, self.movie_num + 1):
                if self.rating_info[i][j] == -1.0:
                    continue

                avg += self.rating_info[i][j]
                count += 1

        avg = avg / count

        print("avg : " + str(avg))

        print("-- total_data.calculate_avg() end --")
        print(datetime.datetime.now())
        print("===================================================")

        return avg

    def calculate_u_dev(self, avg):
        print("===========================================")
        print("-- total_data.calculate_u_dev() start --")
        print(datetime.datetime.now())

        for i in range(1, self.user_num + 1):
            count = 0
            user_avg = 0

            for j in range(1, self.movie_num + 1):
                if self.rating_info[i][j] == -1.0:
                    continue

                user_avg += self.rating_info[i][j]
                count += 1

            if count == 0:
                print("user : " + str(i) + " : has no rating. - total_data.calculate_u_dev()")
                continue

            user_avg = user_avg / count

            self.u_dev[i] = user_avg - avg

        print("-- total_data.calculate_u_dev() end --")
        print(datetime.datetime.now())
        print("===========================================")

    def calculate_m_dev(self, avg):
        print("===========================================")
        print("-- total_data.calculate_m_dev() start --")
        print(datetime.datetime.now())

        for i in range(1, self.movie_num + 1):
            count = 0
            movie_avg = 0

            for j in range(1, self.user_num + 1):
                if self.rating_info[j][i] == -1.0:
                    continue

                movie_avg += self.rating_info[j][i]
                count += 1

            if count == 0:
                print("movie : " + str(i) + " : has no rating. - total_data.calculate_m_dev()")
                continue

            movie_avg = movie_avg / count

            self.m_dev[i] = movie_avg - avg

        print("-- total_data.calculate_m_dev() end --")
        print(datetime.datetime.now())
        print("===========================================")

    def predict(self, u_id, m_id):
        tmp = self.dot_pro(u_id, m_id)

        ##
        if tmp == "nan_error":
            return "nan_error"

        prediction = self.avg + self.u_dev[u_id] + self.m_dev[m_id] + tmp

        return prediction

    def dot_pro(self, u_id, m_id):
        value = 0.0

        for i in range(19):
            value += self.u[u_id][i] * self.s[i] * self.m[i][m_id]

        if math.isnan(value):
            print("dot_pro - user : " + str(u_id) + " : movie : " + str(m_id) + " : nan error. total_data.dot_pro()")
            return "nan_error"

        return value

    def print_s(self):
        print("-- s value --")
        for i in range(19):
            print("s[" + str(i) + "] : " + str(self.s[i]))
        print("-------------")

    def grad_descent(self):
        for u in range(1, self.user_num + 1):
            for m in range(1, self.movie_num + 1):
                if self.rating_info[u][m] == -1.0:
                    continue
                tmp = self.predict(u, m)

                if tmp == "nan_error":
                    return "nan_error"

                self.prediction_result[u][m] = tmp

                dev = self.rating_info[u][m] - tmp

                for i in range(19):
                    self.s[i] += _gamma * dev * self.m[i][m]
                    tmp = self.u[u][i]
                    self.u[u][i] += _gamma * (dev * self.m[i][m] - _lambda * self.u[u][i])
                    self.m[i][m] += _gamma * (dev * tmp - _lambda * self.m[i][m])

        error = 0
        count = 0

        for u in range(1, self.user_num + 1):
            for m in range(1, self.movie_num + 1):
                if self.rating_info[u][m] == -1.0:
                    continue

                dev = self.rating_info[u][m] - self.prediction_result[u][m]
                count += 1
                error += abs(dev) * abs(dev)

        return math.sqrt(error / count)

    def start_svd(self):
        print("=====================================")
        print("-- total_data.start_svd() start --")
        print(datetime.datetime.now())

        ##back up for u, s, m
        # 이전 u, s, m을 저장해둠.
        # nan 오류가 발생하거나 error 값이 커지면 불러온다.
        self.u_save = np.zeros((self.user_num + 1, 19))
        for i in range(self.user_num + 1):
            for j in range(19):
                self.u_save[i][j] = self.u[i][j] = self.user_profile[i][j]
        self.s_save = np.zeros(19)
        for i in range(19):
            self.s_save[i] = self.s[i]
        self.m_save = np.zeros((19, self.movie_num + 1))
        for i in range(19):
            for j in range(self.movie_num + 1):
                self.m_save[i][j] = self.m[i][j] = self.movie_profile[i][j]
        ##

        min = 0
        count = 0

        # initial error
        for u in range(1, self.user_num + 1):
            for m in range(1, self.movie_num + 1):
                if self.rating_info[u][m] == -1.0:
                    continue

                dev = self.rating_info[u][m] - self.predict(u, m)
                count += 1
                min += abs(dev) * abs(dev)

        min = math.sqrt(min / count)

        step = 0
        print("initial error : " + str(min))

        count_reduce_gamma = 0

        while (True):
            err = self.grad_descent()
            step += 1

            if err == "nan_error":  # nan 에러 발생
                print("***NAN***")
                print("step : " + str(step))
                print(datetime.datetime.now())

                if count_reduce_gamma < _reduce_gamma_limit:
                    # _gamma 값 감소 횟수가 아직 남았다면 _gamma 값을 감소시키고
                    # grad_descent를 다시 시도
                    count_reduce_gamma += 1
                    global _gamma
                    _gamma = _gamma / 2
                    print("****************************************")
                    print("count_reduce_gamma : " + str(count_reduce_gamma))
                    print("reduce_gamma_limit : " + str(_reduce_gamma_limit))
                    print("reduced _gamma : " + str(_gamma))
                    self.print_s()
                    print("****************************************")

                    # u, s, m 복원
                    for i in range(self.user_num + 1):
                        for j in range(19):
                            self.u[i][j] = self.u_save[i][j]
                    for i in range(19):
                        self.s[i] = self.s_save[i]
                    for i in range(19):
                        for j in range(self.movie_num + 1):
                            self.m[i][j] = self.m_save[i][j]
                    continue
                else:
                    print("end - step : " + str(step))
                    print("end - error : " + str(min))
                    break
            else:
                err = abs(err)

                print("step : " + str(step) + " : error : " + str(err))
                print(datetime.datetime.now())
                ##
                self.print_s()

            if err < min:
                min = err

                for i in range(self.user_num + 1):
                    for j in range(19):
                        self.u_save[i][j] = self.u[i][j]
                for i in range(19):
                    self.s_save[i] = self.s[i]
                for i in range(19):
                    for j in range(self.movie_num + 1):
                        self.m_save[i][j] = self.m[i][j]
            else:  # error 값이 증가한 경우
                if count_reduce_gamma < _reduce_gamma_limit:
                    # _gamma 값 감소 횟수가 아직 남았다면 _gamma 값을 감소시키고
                    # grad_descent를 다시 시도
                    count_reduce_gamma += 1
                    _gamma = _gamma / 2
                    print("****************************************")
                    print("count_reduce_gamma : " + str(count_reduce_gamma))
                    print("reduce_gamma_limit : " + str(_reduce_gamma_limit))
                    print("reduced _gamma : " + str(_gamma))
                    print("****************************************")

                    # u, s, m 복구
                    for i in range(self.user_num + 1):
                        for j in range(19):
                            self.u[i][j] = self.u_save[i][j]
                    for i in range(19):
                        self.s[i] = self.s_save[i]
                    for i in range(19):
                        for j in range(self.movie_num + 1):
                            self.m[i][j] = self.m_save[i][j]
                else:
                    print("end - step : " + str(step))
                    print("end - error : " + str(min))
                    break
        ###while

        # u, s, m 복구
        for i in range(self.user_num + 1):
            for j in range(19):
                self.u[i][j] = self.u_save[i][j]
        for i in range(19):
            self.s[i] = self.s_save[i]
        for i in range(19):
            for j in range(self.movie_num + 1):
                self.m[i][j] = self.m_save[i][j]
        ###

        print("-- total_data.start_svd() end --")
        print(datetime.datetime.now())
        print("=====================================")

    def start_prediction(self):
        # 레이팅 매트릭스를 채움(예측)
        print("==========================================")
        print("-- total_data.start_prediction() start --")
        print(datetime.datetime.now())

        for u in range(1, self.user_num + 1):
            for m in range(1, self.movie_num + 1):
                if self.rating_info[u][m] != -1.0:
                    continue

                tmp = self.predict(u, m)

                if tmp > 5.0:
                    tmp = 5.0

                if tmp < 0.0:
                    tmp = 0.0

                self.prediction_result[u][m] = tmp

        print("-- total_data.start_prediction() end --")
        print(datetime.datetime.now())
        print("==========================================")

    def start_recommendation(self):
        # 각 사용자별 영화 추천 및 저장
        print("==============================================")
        print("-- total_data.start_recommendation() start --")
        print(datetime.datetime.now())

        for u in range(1, self.user_num + 1):
            ###
            while (True):
                if (self.users.user_list[u].is_being_written == False):
                    break
            self.users.user_list[u].is_being_written = True
            ###

            if u % 50 == 0:
                print("user : " + str(u))

            self.users.user_list[u].recommended_movies.clear()

            for m in range(1, self.movie_num + 1):
                if self.rating_info[u][m] != -1.0:
                    continue

                self.users.user_list[u].recommended_movies.add(m, self.prediction_result[u][m])
            ###
            self.users.user_list[u].is_being_written = False
            ###

        print("-- total_data.start_recommendation() end --")
        print(datetime.datetime.now())
        print("==============================================")

    def get_recommended_movies_list_about_user(self, u):
        # 주어진 사용자(아이디) u에 대한 추천 영화 json 메시지 작성 및 반환
        ###
        while (True):
            if (self.users.user_list[u].is_being_written == False):
                break
        self.users.user_list[u].is_being_written = True
        ###
        recommended_num = len(self.users.user_list[u].recommended_movies.movies)
        recommended_movies = self.users.user_list[u].recommended_movies.movies
        '''json_u_id = u
        json_num = 0

        json_m_ids = []
        json_m_titles = []
        json_m_ratings = []
        json_m_genres = []

        json_num = len(self.users.user_list[json_u_id].recommended_movies.movies)
        recommended_movies = self.users.user_list[json_u_id].recommended_movies.movies

        for i in range(json_num):
            m_id = recommended_movies[i].movie_id
            json_m_ids.append(m_id)

            json_m_titles.append(self.movies.movie_list[m_id].title)

            json_m_ratings.append(recommended_movies[i].rating)

            genre = [0] * 19
            for i in range(19):
                genre[i] = self.movie_profile[i][m_id]
            json_m_genres.append(genre)'''

        ###
        self.users.user_list[u].is_being_written = False
        ###
        recommend_list = []

        for i in range(recommended_num):
            json_data = OrderedDict()
            m_id = recommended_movies[i].movie_id
            expecting_score = recommended_movies[i].rating
            m_title = self.movies.movie_list[m_id].title
            if self.movies.movie_list[m_id].total_rate_num != 0:
                average_score = self.movies.movie_list[m_id].total_rate / self.movies.movie_list[m_id].total_rate_num
            else:
                average_score = 0
            json_data["m_id"] = m_id
            json_data["title"] = m_title
            json_data["expectingScore"] = expecting_score
            json_data["averageScore"] = average_score
            try:
                json_data["score"] = Rating.objects.get(user=u, movie=m_id).rate
            except Rating.DoesNotExist:
                json_data["score"] = -1.0
            recommend_list.append(json_data)

        json_data = json.dumps(recommend_list, ensure_ascii=False, indent="\t")

        return json_data


class data_for_db:
    def __init__(self, _directory):
        # 데이터 파일들의 이름과 그 디렉토리
        self.movie_info_file_name = "u.item"
        self.user_info_file_name = "u.user"
        self.metadata_file_name = "u.info"
        self.rating_data_file_name = "u.data"
        self.directory_name = _directory

        os.chdir(self.directory_name)

        # open files
        movie_info_file = open(self.movie_info_file_name, "r", encoding="utf-8-sig")
        user_info_file = open(self.user_info_file_name, "r", encoding="utf-8-sig")
        metadata_file = open(self.metadata_file_name, "r", encoding="utf-8-sig")

        # read metadata file
        # 사용자 수
        user_line = metadata_file.readline()
        elements_user_line = user_line.split(" ")
        self.user_num = int(elements_user_line[0])

        # 영화 수
        movie_line = metadata_file.readline()
        elements_movie_line = movie_line.split(" ")
        self.movie_num = int(elements_movie_line[0])

        # 레이팅 수
        rating_line = metadata_file.readline()
        elements_rating_line = rating_line.split(" ")
        self.rating_num = int(elements_rating_line[0])

        metadata_file.close()

        # 사용자 정보 파일
        # 아이디
        self.users = total_user(self.user_num)
        self.users.read_user_data(user_info_file)
        user_info_file.close()

        # 영화 정보 파일
        # 아이디, 타이틀
        self.movies = total_movie(self.movie_num)
        self.movies.read_movie_data(movie_info_file)
        movie_info_file.close()

        # 사용자와 영화의 content-based profile
        # 장르별 누적 평점 19개 + 장르별 누적 레이팅 수 19개(나누기 용)
        # user_num + 1 이므로 사용자 id를 바로 대입해 참조
        self.user_profile = np.zeros((self.user_num + 1, 38))
        # 장르 정보 19개
        self.movie_profile = np.zeros((19, self.movie_num + 1))

        # 실제 레이팅을 저장하는 매트릭스
        self.rating_info = np.full((self.user_num + 1, self.movie_num + 1), -1.0)

        # 영화의 content-based profile(장르 정보)을 저장
        self.read_movie_genre_data()
        # 레이팅과 사용자의 content-based profile(장르 정보) 저장
        # 사용자 프로필에 장르별로 평점과 레이팅의 수를 누적시킨다
        self.read_rating()
        # 누적시킨 장르별 평점에 대해 장르별 레이팅 수로
        # 나누기를 수행 content-based profile을 완성
        self.calculate_user_profile()

    def read_movie_genre_data(self):
        print("================================================")
        print("-- data_to_db.read_movie_genre_data() start --")
        print(datetime.datetime.now())

        movie_info_file = open(self.movie_info_file_name, "r", encoding="utf-8-sig")

        for line in movie_info_file:  # 한 줄씩 읽음
            if line == "":  # -끝-
                break
            elements_list = line.split("|")  # "|"로 문자열을 나눠서 리스트에 저장

            movie_id = int(elements_list[0])

            for i in range(18):
                self.movie_profile[i][movie_id] = int(elements_list[i + 5])
            # 마지막 장르는 개행문자가 붙어있으므로 [0]만 취급(ex. "0\n")
            self.movie_profile[_western][movie_id] = int(elements_list[23][0])

        movie_info_file.close()

        print("-- data_to_db.read_movie_genre_data() end --")
        print(datetime.datetime.now())
        print("================================================")

    def read_rating(self):
        print("================================================")
        print("-- data_to_db.read_rating() start --")
        print(datetime.datetime.now())

        rating_data_file = open(self.rating_data_file_name, "r", encoding="utf-8-sig")

        count = 0

        for line in rating_data_file:  # 한 줄씩 읽음
            if line == "":  # -끝-
                break
            elements_list = line.split("\t")  # "\t"으로 나누어 리스트에 저장
            user_id = int(elements_list[0])
            movie_id = int(elements_list[1])
            rating = int(elements_list[2])

            if user_id < 1 or user_id > self.user_num:
                print("user : " + str(user_id) + " : user id range error. - total_data.read_rating()")
                continue
            if movie_id < 1 or movie_id > self.movie_num:
                print("movie : " + str(movie_id) + " : movie id range error. - total_data.read_rating()")
                continue

            self.rating_info[user_id][movie_id] = rating

            # 장르별 점수, 레이팅 수 누적
            for i in range(19):
                self.user_profile[user_id][i] += (self.movie_profile[i][movie_id] * rating)
                self.user_profile[user_id][i + 19] += self.movie_profile[i][movie_id]

            count += 1

        print("rating count : " + str(count))
        print("rating count by metadata : " + str(self.rating_num))

        print("-- data_to_db.read_rating() end --")
        print(datetime.datetime.now())
        print("================================================")

    def calculate_user_profile(self):
        print("================================================")
        print("-- data_to_db.calculate_user_profile() start --")
        print(datetime.datetime.now())

        for i in range(1, self.user_num + 1):
            for j in range(19):
                count = self.user_profile[i][j + 19]
                if count == 0:
                    self.user_profile[i][j] = 0
                    continue
                self.user_profile[i][j] = self.user_profile[i][j] / count

        print("-- data_to_db.calculate_user_profile() end --")
        print(datetime.datetime.now())
        print("================================================")

