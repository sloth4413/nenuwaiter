from django.db import models

# Create your models here.
class user(models.Model):
    username = models.CharField(max_length=30)
    password = models.CharField(max_length=16)
    phonenumber = models.CharField(max_length=15)
    name = models.CharField(max_length=20)
    address = models.CharField(max_length=100) 
    def __unicode__ (self):
	    return self.name
class restaurant(models.Model):
	id = models.IntegerField(primary_key = True)
	name = models.CharField(max_length = 100)
	phonenumber = models.CharField(max_length =12)
	address = models.CharField(max_length = 200)
	img = models.CharField(max_length = 500)
	introduce = models.CharField(max_length = 500)
	oredersum = models.IntegerField()
	servertime = models.CharField(max_length = 200)
	inorout  = models.CharField(max_length = 50)
	mark = models.FloatField()
	marknum = models.IntegerField()
	def __unicode__ (self):
		return self.name
class dish(models.Model):
	id  = models.IntegerField(primary_key = True)
	name = models.CharField(max_length = 100)
	img = models.CharField(max_length = 500)
	introduce = models.CharField(max_length = 500)
	oredersum = models.IntegerField()
	mark = models.FloatField()
	marknum = models.IntegerField()
	price = models.FloatField()
	fatherid = models.ForeignKey(restaurant,verbose_name = "fatherid")
	def __unicode__ (self):
		return self.name
class comment(models.Model):
	id = models.IntegerField(primary_key = True)
	content = models.CharField(max_length = 1000)
	dishid = models.ForeignKey(dish,verbose_name = "dishid")
	userid = models.ForeignKey(user,verbose_name = "userid")
	def __unicode__ (self):
		return self.dishid.name
class order(models.Model):
	id = models.AutoField(primary_key = True)
	count = models.FloatField()
	number = models.IntegerField()
	fatherid = models.ForeignKey(user)
	def __unicode__ (self):
		return self.fatherid.name
class coupon(models.Model):
	id = models.IntegerField(primary_key = True)
	money = models.IntegerField()
	fatherid = models.ForeignKey(user)
	def __unicode__(self):
		return self.fatherid.name
