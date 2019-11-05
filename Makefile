ACCOUNT_ID = 756629837203

include makefiles/functions.Makefile
include makefiles/formatting.Makefile

PROJECT_ID = stacks

STACK_ROOT 	= .

SBT_APPS =
SBT_NO_DOCKER_APPS = requests_api \
                     items_api

SBT_DOCKER_LIBRARIES    =
SBT_NO_DOCKER_LIBRARIES =

PYTHON_APPS =
LAMBDAS 	=

TF_NAME = stacks
TF_PATH = $(STACK_ROOT)/terraform

$(val $(call stack_setup))