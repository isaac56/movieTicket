from movie.models import User, Movie, Rating, WishList, Comment
from django.http import HttpResponse
from movie import recommender
from django.views.decorators.csrf import csrf_exempt
from django.core import serializers
from collections import OrderedDict
import datetime
import json
import random

@csrf_exempt
def test(request):
    return HttpResponse("")
    list1 = []
    temp = Rating.objects.filter(user="1").order_by("rate")
    for i in range(temp.count()):
        jsonData = OrderedDict()
        jsonData["movie"]=temp[i].movie.m_id
        jsonData["user"]=temp[i].user.u_id
        jsonData["rate"]=temp[i].rate
        list1.append(jsonData)

    return HttpResponse(json.dumps(list1, ensure_ascii=False, indent="\t"))


@csrf_exempt
def get_recommend_list(request):
    request = json.loads(request.body)
    u_id = request["u_id"]
    global td
    if u_id <= td.user_num:
        message = td.get_recommended_movies_list_about_user(int(request["u_id"]))
        return HttpResponse(message, content_type="application/json")
    else:
        return HttpResponse("recommend_list.calculating")

@csrf_exempt
def set_rate(request):
    request = json.loads(request.body)
    try:
        rating = Rating.objects.get(user=request["u_id"], movie=request["m_id"])
        rating.rate=request["score"]
        rating.save()
        print("----rating set success----" + str(rating.rate))
        return HttpResponse("rate.set.success")
    except Rating.DoesNotExist:
        rating = Rating(user=User.objects.get(u_id=request["u_id"]), movie=Movie.objects.get(m_id=request["m_id"]),
                        rate=request["score"])
        rating.save()
        print("----rating create success----" + str(rating.rate))
        return HttpResponse("rate.create.success")

@csrf_exempt
def get_wishList(request):
    request = json.loads(request.body)

    list = []
    wishList = WishList.objects.filter(user=request["u_id"]).order_by("date")
    for i in range(wishList.count()):
        jsonData = OrderedDict()
        jsonData["m_id"] = wishList[i].movie.m_id
        jsonData["title"] = wishList[i].movie.title
        #jsonData["date"] = wishList[i].date
        list.append(jsonData)
    return HttpResponse(json.dumps(list, ensure_ascii=False, indent="\t"), content_type="application/json")

@csrf_exempt
def add_wishList(request):
    request = json.loads(request.body)
    try:
        wishList = WishList.objects.get(user=request["u_id"], movie=request["m_id"])
        return HttpResponse("wishList.add.aleadyExist")
    except WishList.DoesNotExist:
        wishList = WishList(user=User.objects.get(u_id=request["u_id"]),
                            movie=Movie.objects.get(m_id=request["m_id"]))
        print(request["m_id"])
        print(Movie.objects.get(m_id=request["m_id"]).title)
        wishList.save()
        return HttpResponse("wishList.add.success")

@csrf_exempt
def delete_wishList(request):
    request = json.loads(request.body)
    try:
        wishList = WishList.objects.get(user=request["u_id"], movie=request["m_id"])
        wishList.delete()
        return HttpResponse("wishList.delete.success")
    except WishList.DoesNotExist:
        return HttpResponse("wishList.delete.doesNotExist")

@csrf_exempt
def get_comments(request):
    request = json.loads(request.body)
    m_id = request["m_id"]
    comments = Comment.objects.filter(movie=m_id).order_by("-comment_date")
    list = []
    for i in range(comments.count()):
        jsonData = OrderedDict()
        jsonData["comment"] = comments[i].comment
        jsonData["login_id"] = comments[i].user.login_id
        jsonData["date"] = comments[i].comment_date.strftime('%Y-%m-%d %H:%M')
        list.append(jsonData)

    return HttpResponse(json.dumps(list, ensure_ascii=False, indent="\t"), content_type="application/json")


@csrf_exempt
def add_comment(request):
    request = json.loads(request.body)
    try:
        comment = Comment.objects.get(user=request["u_id"], movie=request["m_id"])
        return HttpResponse("comment.add.aleadyExist")
    except Comment.DoesNotExist:
        comment = Comment(user=User.objects.get(u_id=request["u_id"]),
                          movie=Movie.objects.get(m_id=request["m_id"]),
                          comment=request["comment"])
        comment.save()
        return HttpResponse("comment.add.success")


@csrf_exempt
def get_random_list(request):
    '''
    random_request = json.loads(request.body)
    random_movie = Movie.objects.filter().order_by("?")

    if random_movie.count() > 100:
        list = []
        for i in range(100):
            jsonData = OrderedDict()
            u_id = random_request["u_id"]
            m_id = random_movie[i].m_id
            title = random_movie[i].title
            try:
                score = Rating.objects.get(user=u_id, movie=m_id).rate
            except Rating.DoesNotExist:
                score = -1

            jsonData["m_id"] = m_id
            jsonData["title"] = title
            jsonData["score"] = score
            list.append(jsonData)
    '''
    random_request = json.loads(request.body)
    movie_num = Movie.objects.all().count()
    if movie_num > 100:
        random_m_id_list = random.sample(range(movie_num), 100)
        random_movie = Movie.objects.filter(m_id__in=random_m_id_list)

        list = []
        for i in range(random_movie.count()):
            jsonData = OrderedDict()
            u_id = random_request["u_id"]
            m_id = random_movie[i].m_id
            title = random_movie[i].title
            try:
                score = Rating.objects.get(user=u_id,movie=m_id).rate
            except Rating.DoesNotExist:
                score = -1

            jsonData["m_id"] = m_id
            jsonData["title"] = title
            jsonData["score"] = score
            list.append(jsonData)
    return HttpResponse(json.dumps(list, ensure_ascii=False, indent="\t"))

@csrf_exempt
def get_movie(request):
    request = json.loads(request.body)
    global td
    u_id = request["u_id"]
    m_id = request["m_id"]
    title = request["title"]
    print(title)
    average_score = -1
    expecting_score = -1

    if m_id == -1:
        try:
            movie = Movie.objects.get(title=title)
            m_id = movie.m_id
            print("-----movie exist-----")
        except Movie.DoesNotExist:
            movie = Movie(title=title)
            movie.save()
            m_id = Movie.objects.get(title=title).m_id
            print("-----movie does not exist-----")
    try:
        rating = Rating.objects.get(movie=m_id, user=u_id)
        score = rating.rate
    except Rating.DoesNotExist:
        score = -1

    if m_id <= td.movie_num:
        if u_id <= td.user_num:
            expecting_score = td.prediction_result[u_id][m_id]
            if td.movies.movie_list[m_id].total_rate_num != 0:
                average_score = td.movies.movie_list[m_id].total_rate / td.movies.movie_list[m_id].total_rate_num

    jsonData = OrderedDict()
    jsonData["m_id"] = m_id
    jsonData["title"] = title
    jsonData["averageScore"] = average_score
    jsonData["expectingScore"] = expecting_score
    jsonData["score"] = score
    return HttpResponse(json.dumps(jsonData, ensure_ascii=False, indent="\t"), content_type="application/json")

@csrf_exempt
def user(request):
    if request.method == "GET":
        return HttpResponse("nothing")

    if request.method == "POST":
        login_request = json.loads(request.body)
        try:
            login_user = User.objects.get(login_id=login_request["id"])
            if login_user.password == login_request["password"]:
                jsonData = OrderedDict()
                jsonData["u_id"] = login_user.u_id
                return HttpResponse(json.dumps(jsonData, ensure_ascii=False, indent="\t"), content_type="application/json")
            else:
                return HttpResponse("user.post.fail:wrongPassword")
        except User.DoesNotExist:
            return HttpResponse("user.post.fail:notExists")

    if request.method == "PUT":
        signUp_request = json.loads(request.body)
        if User.objects.filter(login_id=signUp_request["id"]).exists():
            return HttpResponse("user.put.fail:idExists")

        new_user = User(name=signUp_request["name"], birth_date=signUp_request["birthDate"],
                        login_id=signUp_request["id"], password=signUp_request["password"])
        new_user.save()
        return HttpResponse("user.put.success")

    if request.method == "DELETE":
        return HttpResponse("nothing")

@csrf_exempt
def doSVDpp(request):
    print("===============================")
    print("----- prepareSVDpp start -----")
    print(datetime.datetime.now())

    global td

    temp = recommender.total_data()

    print("----- prepareSVDpp end -----")
    print(datetime.datetime.now())
    print("===============================")

    print("==========================")
    print("----- doSVDpp start -----")
    print(datetime.datetime.now())

    temp.start_svd()
    temp.start_prediction()
    temp.start_recommendation()

    td = temp
    print("----- doSVDpp end -----")
    print(datetime.datetime.now())
    print("==========================")
    return HttpResponse("doSVDpp가 완료되었습니다.")

@csrf_exempt
def fileToDB(request):
    print("===========================")
    print("----- fileToDB start -----")
    print(datetime.datetime.now())

    count = 1

    data = recommender.data_for_db("C:\\Users\\isaac\\Desktop\\project\\server")

    # 디비에 저장
    print("transfer user data")
    print(datetime.datetime.now())
    for i in range(1, data.user_num + 1):
        if i % 100 == 0:
            print("doing : " + str(i))
        newUser = User(u_id=i, login_id="test"+str(i), password="test"+str(i))
        newUser.unknown_rate = data.user_profile[i][recommender._unknown]
        newUser.action_rate = data.user_profile[i][recommender._action]
        newUser.adventure_rate = data.user_profile[i][recommender._adventure]
        newUser.animation_rate = data.user_profile[i][recommender._animation]
        newUser.children_rate = data.user_profile[i][recommender._children]
        newUser.comedy_rate = data.user_profile[i][recommender._comedy]
        newUser.crime_rate = data.user_profile[i][recommender._crime]
        newUser.documentary_rate = data.user_profile[i][recommender._documentary]
        newUser.drama_rate = data.user_profile[i][recommender._drama]
        newUser.fantasy_rate = data.user_profile[i][recommender._fantasy]
        newUser.film_noir_rate = data.user_profile[i][recommender._film_noir]
        newUser.horror_rate = data.user_profile[i][recommender._horror]
        newUser.musical_rate = data.user_profile[i][recommender._musical]
        newUser.mystery_rate = data.user_profile[i][recommender._mystery]
        newUser.romance_rate = data.user_profile[i][recommender._romance]
        newUser.sci_fi_rate = data.user_profile[i][recommender._sci_fi]
        newUser.thriller_rate = data.user_profile[i][recommender._thriller]
        newUser.war_rate = data.user_profile[i][recommender._war]
        newUser.western_rate = data.user_profile[i][recommender._western]

        newUser.unknown_num = data.user_profile[i][19 + recommender._unknown]
        newUser.action_num = data.user_profile[i][19 + recommender._action]
        newUser.adventure_num = data.user_profile[i][19 + recommender._adventure]
        newUser.animation_num = data.user_profile[i][19 + recommender._animation]
        newUser.children_num = data.user_profile[i][19 + recommender._children]
        newUser.comedy_num = data.user_profile[i][19 + recommender._comedy]
        newUser.crime_num = data.user_profile[i][19 + recommender._crime]
        newUser.documentary = data.user_profile[i][19 + recommender._documentary]
        newUser.drama_num = data.user_profile[i][19 + recommender._drama]
        newUser.fantasy_num = data.user_profile[i][19 + recommender._fantasy]
        newUser.film_noir_num = data.user_profile[i][19 + recommender._film_noir]
        newUser.horror_num = data.user_profile[i][19 + recommender._horror]
        newUser.musical_num = data.user_profile[i][19 + recommender._musical]
        newUser.mystery_num = data.user_profile[i][19 + recommender._mystery]
        newUser.romance_num = data.user_profile[i][19 + recommender._romance]
        newUser.sci_fi_num = data.user_profile[i][19 + recommender._sci_fi]
        newUser.thriller_num = data.user_profile[i][19 + recommender._thriller]
        newUser.war_num = data.user_profile[i][19 + recommender._war]
        newUser.western_num = data.user_profile[i][19 + recommender._western]

        newUser.save()

    print("transfer movie data")
    print(datetime.datetime.now())
    for i in range(1, data.movie_num + 1):
        if i % 100 == 0:
            print("doing : " + str(i))
        newMovie = Movie(m_id=i, title=data.movies.movie_list[i].title)
        newMovie.is_unknown = True if data.movie_profile[recommender._unknown][i] == 1 else False
        newMovie.is_action = True if data.movie_profile[recommender._action][i] == 1 else False
        newMovie.is_adventure = True if data.movie_profile[recommender._adventure][i] == 1 else False
        newMovie.is_animation = True if data.movie_profile[recommender._animation][i] == 1 else False
        newMovie.is_children = True if data.movie_profile[recommender._children][i] == 1 else False
        newMovie.is_comedy = True if data.movie_profile[recommender._comedy][i] == 1 else False
        newMovie.is_crime = True if data.movie_profile[recommender._crime][i] == 1 else False
        newMovie.is_documentary = True if data.movie_profile[recommender._documentary][i] == 1 else False
        newMovie.is_drama = True if data.movie_profile[recommender._drama][i] == 1 else False
        newMovie.is_fantasy = True if data.movie_profile[recommender._fantasy][i] == 1 else False
        newMovie.is_film_noir = True if data.movie_profile[recommender._film_noir][i] == 1 else False
        newMovie.is_horror = True if data.movie_profile[recommender._horror][i] == 1 else False
        newMovie.is_musical = True if data.movie_profile[recommender._musical][i] == 1 else False
        newMovie.is_mystery = True if data.movie_profile[recommender._mystery][i] == 1 else False
        newMovie.is_romance = True if data.movie_profile[recommender._romance][i] == 1 else False
        newMovie.is_sci_fi = True if data.movie_profile[recommender._sci_fi][i] == 1 else False
        newMovie.is_thriller = True if data.movie_profile[recommender._thriller][i] == 1 else False
        newMovie.is_war = True if data.movie_profile[recommender._war][i] == 1 else False
        newMovie.is_western = True if data.movie_profile[recommender._western][i] == 1 else False

        newMovie.save()

    print("transfer rating data")
    print(datetime.datetime.now())
    for i in range(1, data.user_num + 1):
        if i % 20 == 0:
            print("doing user : " + str(i))
        for j in range(1, data.movie_num + 1):
            if (data.rating_info[i][j] != -1.0):
                newRating = Rating(r_id=count)
                count += 1
                newRating.user = User.objects.get(u_id=i)
                newRating.movie = Movie.objects.get(m_id=j)
                newRating.rate = data.rating_info[i][j]
                newRating.isUpdated = True

                newRating.save()

    # newUser = User.objects.get(u_id=1)

    print("----- fileToDB end -----")
    print(datetime.datetime.now())
    print("===========================")

    print("===============================")
    print("----- prepareSVDpp start -----")
    print(datetime.datetime.now())

    global td

    temp = recommender.total_data()

    print("----- prepareSVDpp end -----")
    print(datetime.datetime.now())
    print("===============================")

    print("==========================")
    print("----- doSVDpp start -----")
    print(datetime.datetime.now())

    temp.start_svd()
    temp.start_prediction()
    temp.start_recommendation()

    td = temp
    print("----- doSVDpp end -----")
    print(datetime.datetime.now())
    print("==========================")
    return HttpResponse("fileToDB, doSVDpp가 완료되었습니다.")