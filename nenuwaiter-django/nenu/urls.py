from django.conf.urls import patterns, include, url
from django.contrib import admin
from nenu.views import login,create,restaurantlist,dishlist,onedish,sendmessage,search,updata,updata2,updata3,getCoupon,getOrder
admin.autodiscover()

urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'nenu.views.home', name='home'),
    # url(r'^nenu/', include('nenu.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    url(r'^admin/', include(admin.site.urls)),
    url(r'^login/',login),
    url(r'^create/',create),
    url(r'^restaurantlist/',restaurantlist),
    url(r'^dishlist/',dishlist),
    url(r'^onedish/',onedish),
    url(r'^sendmessage/',sendmessage),
    url(r'^search/',search),
    url(r'^updata/',updata),
    url(r'^updata2/',updata2),
    url(r'^updata3/',updata3),
    url(r'^coupon/',getCoupon),
    url(r'^order/',getOrder),    
)
