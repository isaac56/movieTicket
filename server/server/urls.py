"""server URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/2.1/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from movie import views
from django.conf.urls import url

urlpatterns = [
    url(r'^admin/', admin.site.urls),
    url(r'^user/', views.user),
    url(r'^test/', views.test),
    url(r'^fileToDB/', views.fileToDB),
    url(r'^doSVDpp/', views.doSVDpp),
    url(r'^get_movie/', views.get_movie),
    url(r'^set_rate/', views.set_rate),
    url(r'^get_recommend_list/', views.get_recommend_list),
    url(r'^get_random_list/', views.get_random_list),
    url(r'^get_wishList/', views.get_wishList),
    url(r'^add_wishList/', views.add_wishList),
    url(r'^delete_wishList/', views.delete_wishList),
    url(r'^add_comment/', views.add_comment),
    url(r'^get_comments/', views.get_comments),
]
