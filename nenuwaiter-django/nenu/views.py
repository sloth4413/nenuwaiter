# -*- coding: utf-8 -*-  
from django.http import HttpResponse
from django.template.loader import get_template
from django.template import Context
from django.shortcuts import render_to_response
import datetime
import requests
import json
import simplejson
import sys
from users.models import user,restaurant,dish,comment,order,coupon
#登录
def login(request):
	dict= {}
	if request.method == 'POST':
		req = simplejson.loads(request.raw_post_data)
		rusername = req['username']
		rpassword = req['password']
		b = user.objects.filter(username=rusername)
		if(len(b) == 0):
			dict["status"] = '101'#用户名不存在返回101
		elif(b[0].password != rpassword):
			dict["status"] = '102'#密码错误返回102
		else:
			dict["status"] = 'yes'#验证成功返回yes
			dict['name']  = b[0].name
			dict['phonenumber'] = b[0].phonenumber
			dict['address'] = b[0].address
			dict['id'] = b[0].id

		x = simplejson.dumps(dict)
		return HttpResponse(x)

	else:
		dict['status'] = 'wa'
		x = simplejson.dumps(dict)
		return HttpResponse(x)
# 更新注册后台
def create(request):
	dict = {}
	if request.method == 'POST':
		req = simplejson.loads(request.raw_post_data)
		rusername = req['username']
		rname = req['name']
		rphonenumber = req['phonenumber']
		raddress = req['address']
		rpassword = req['password']
		b = user.objects.filter(username=rusername)
		if(len(b) != 0):
			dict['status'] = 'a'#如果username已经存在，返回状态a
		else:
			try:
				p1 = user(username = rusername,name = rname,phonenumber = rphonenumber,address = raddress,password = rpassword)
				p1.save()
				dict['status'] = 'y'
			except:	
				dict['status'] = 'n'
		x = simplejson.dumps(dict)
		return HttpResponse(x)
	dict['status'] = 'w'
	x = simplejson.dumps(dict)
	return HttpResponse(x)
#餐厅列表
def restaurantlist(request):
	r = restaurant.objects.all()
	name = []
	img = []
	phonenumber = []
	main_star = []
	dict = {}
	for i in r:
		name.append(i.name)
		img.append(i.img)
		phonenumber.append(i.phonenumber)
		main_star.append(i.mark)
	dict['name'] = name
	dict['img'] = img
	dict['phonenumber'] = phonenumber
	dict['main_star'] = main_star
	x = simplejson.dumps(dict)
	return HttpResponse(x)
#菜的列表
def dishlist(request):
	img = []
	price = []
	name = []
	dishlist_rating = []
	dict = {}
	if request.method == 'POST':
		req = simplejson.loads(request.raw_post_data)
		restaurantname = req['name']
		x = restaurant.objects.filter(name = restaurantname)
		id = x[0].id
		restaurant_phone = x[0].phonenumber
		restaurant_address = x[0].address
		restaurant_introduce = x[0].introduce
		restaurant_marknum = x[0].marknum
		restaurant_mark = x[0].mark
		dict['res_phone'] = restaurant_phone
		dict['res_address'] = restaurant_address
		dict['res_introduce'] = restaurant_introduce
		dict['res_id'] = id
		dict['res_mark'] = restaurant_mark
		dict['res_marknum'] = restaurant_marknum
		dishon = dish.objects.filter(fatherid = id)
		for i in dishon:
			img.append(i.img)
			name.append(i.name)
			price.append(i.price)
			dishlist_rating.append(i.mark)
		dict['img'] = img
		dict['name'] = name
		dict['price'] = price
		dict['dishlist_rating'] = dishlist_rating
		dict['status'] = 'y'
		x  = simplejson.dumps(dict)
		return HttpResponse(x)
	dict['status'] = 'n'
	x = simplejson.dumps(dict)
	return HttpResponse(x)
#单个菜的信息,订购也页面
def onedish(request):
	dict = {}
	commentitem = []
	if request.method == 'POST':
		req = simplejson.loads(request.raw_post_data)
		fatherid = req['fatherid']
		dishname = req['dishname']
		onedish = dish.objects.filter(fatherid = fatherid,name = dishname)
		res = restaurant.objects.filter(id = fatherid)
		onedish_introduce = onedish[0].introduce
		onedish_price = onedish[0].price
		onedish_father = res[0].name
		onedish_img = onedish[0].img
		onedish_id = onedish[0].id
		onedish_mark = onedish[0].mark
		onedish_marknum = onedish[0].marknum

		commentlist = comment.objects.filter(dishid = onedish_id)
		for i in commentlist:
			commentitem.append(i.content)
		dict['commentlist'] = commentitem
		dict['onedish_id'] = onedish_id
		dict['onedish_marknum'] = onedish_marknum
		dict['onedish_mark'] = onedish_mark
		dict['onedish_introduce'] = onedish_introduce
		dict['onedish_price'] = onedish_price
		dict['onedish_img'] = onedish_img
		dict['onedish_father'] = onedish_father
		dict['status'] = 'y'
		x = simplejson.dumps(dict)
		return HttpResponse(x)
	dict['status'] = 'n'
	x = simplejson.dumps(dict)
	return HttpResponse(x)
# 发送短信模块
def sendmessage(request):
	dict = {}
	if request.method == 'POST':
		req = simplejson.loads(request.raw_post_data)
		message = req['message']
		phonenumber = req['phonenumber']
		sendCount = req['sendCount']
		sendSum = req['sendSum']
		sendCoupon = req['sendCoupon']
		sendUserId = req['sendUserId']
		dict['message'] = message
		dict['phonenumber'] = phonenumber
		dict['sendCoupon'] = sendCoupon
		dict['sendCount'] = sendCount
		dict['sendSum'] = sendSum
		dict['sendUserId'] = sendUserId
		userObject = user.objects.get(id = sendUserId)
		p = order(count= sendCount,number = sendSum,fatherid = userObject)
		p.save()
		if sendCoupon > 0:
			coupon.objects.get(id = sendCoupon).delete()
		# resp = requests.post(("https://sms-api.luosimao.com/v1/send.json"),auth=("api", "6bac001348b3495d558a8edffe0312bb"),data={"mobile": phonenumber,"message": message},timeout=3 , verify=False);
		# result = json.loads(resp.content)
		# dict['status'] = result['error']
		# x = simplejson.dumps(dict)
#不发送
		dict['status'] = "0"
		x = simplejson.dumps(dict)
		return HttpResponse(x)
	dict['status'] = '104'
	x = simplejson.dumps(dict)
	return HttpResponse(x)
def search(request):
	img = []
	name = []
	price = []
	fatherid = []
	dict = {}
	if request.method == 'POST':
		req = simplejson.loads(request.raw_post_data)
		searchname = req['search']
		res = dish.objects.filter(name__contains = searchname)
		for i in res:
			img.append(i.img)
			name.append(i.name)
			price.append(i.price)
			fatherid.append(i.fatherid.id)
		dict['img'] = img
		dict['name'] = name
		dict['price'] = price
		dict['fatherid'] = fatherid
		dict['status'] = 'y'
		x  = simplejson.dumps(dict)
		return HttpResponse(x)
	dict['status'] = 'n'
	x = simplejson.dumps(dict)
	return HttpResponse(x)
def updata(request):
	if request.method == "POST":
		req = simplejson.loads(request.raw_post_data)
		mark = req['mark']
		marknum = req['marknum']
		fatherid = req['father_id']
		res = restaurant.objects.get(id = fatherid)
		res.mark = mark
		res.marknum = marknum
		res.save()
	return HttpResponse(res.id)
def updata2(request):
	if request.method == "POST":
		req = simplejson.loads(request.raw_post_data)
		mark = req['mark']
		marknum = req['marknum']
		getid = req['id']
		res = dish.objects.get(id = getid)
		res.mark = mark
		res.marknum = marknum
		res.save()
	return HttpResponse("hello")
def updata3(request):
	if request.method == "POST":
		req = simplejson.loads(request.raw_post_data)
		suserid = req['userid']
		sdishid = req['dishid']
		scontent = req['postcontent']
		dishObject = dish.objects.get(id = sdishid)
		userObject = user.objects.get(id = suserid)
		r = comment.objects.all()
		num = 1
		for i in r:
			num = num + 1
		p = comment.objects.create(
			id = num,
			content = scontent,
			userid = userObject,
			dishid = dishObject)
	return HttpResponse("hello updata3")

#获取优惠券信息
def getCoupon(request):
	dict = {}
	if request.method == "POST":
		couponItem = []
		couponId = []
		req = simplejson.loads(request.raw_post_data)
		userId = req['userId']
		couponAll = coupon.objects.filter(fatherid = userId)
		for i in couponAll:
			couponItem.append(i.money)
			couponId.append(i.id)
		dict['money'] = couponItem
		dict['couponId'] = couponId
		dict['status'] = 'y'
	else:
		# couponAll = coupon.objects.filter(fatherid = "1")
		# dict['money'] = couponAll[0].money
		dict['status'] = 'n'
	x = simplejson.dumps(dict)
	return HttpResponse(x)
#获取订单信息
def getOrder(request):
	dict = {}
	if request.method == "POST":
		orderNum = []
		orderCount = []
		req = simplejson.loads(request.raw_post_data)
		userId = req['userId']
		orderAll = order.objects.filter(fatherid = userId)
		for i in orderAll:
			orderNum.append(i.number)
			orderCount.append(i.count)
		dict['order_num'] = orderNum
		dict['order_count'] = orderCount
		dict['status'] = 'y'
	else:
		# orderAll = order.objects.filter(fatherid = "1")
		# dict['money'] = orderAll[0].money
		dict['status'] = 'n'
	x = simplejson.dumps(dict)
	return HttpResponse(x)