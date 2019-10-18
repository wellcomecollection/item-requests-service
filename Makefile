ACCOUNT_ID = 760097843905

include makefiles/functions.Makefile
include makefiles/formatting.Makefile

PROJECT_ID = requests

STACK_ROOT 	= .

SBT_APPS =
SBT_NO_DOCKER_APPS = requests_api \
                     status_api

SBT_DOCKER_LIBRARIES    =
SBT_NO_DOCKER_LIBRARIES =

PYTHON_APPS =
LAMBDAS 	=

TF_NAME = requests
TF_PATH = $(STACK_ROOT)/terraform

$(val $(call stack_setup))