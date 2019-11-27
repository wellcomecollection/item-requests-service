#!/usr/bin/env bash

set -o nounset

USERNAME=$1
PATRON_ID=$2

aws cognito-idp --profile catalogue-developer admin-update-user-attributes \
    --user-pool-id eu-west-1_oToO0mWFj \
    --username $USERNAME \
    --user-attributes Name="custom:patronId",Value="$PATRON_ID"
