from django.contrib import admin
from users.models import user,restaurant,dish,comment,order,coupon

admin.site.register(user)
admin.site.register(restaurant)
admin.site.register(dish)
admin.site.register(comment)
admin.site.register(order)
admin.site.register(coupon)
# admin.site.register(password)
# admin.site.register(phonenumber)