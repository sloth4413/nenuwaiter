import os
import sys
import sae
from nenu import wsgi
application = sae.create_wsgi_app(wsgi.application)
root = os.path.dirname(__file__)
sys.path.insert(0, os.path.join(root, 'site-packages'))