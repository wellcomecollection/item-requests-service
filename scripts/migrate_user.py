#!/usr/bin/env python3

import sys
import requests
import boto3
import uuid
import base64
import json

def create_session(role_arn):
    client = boto3.client('sts')

    response = client.assume_role(
        RoleArn=role_arn,
        RoleSessionName=str(uuid.uuid1())
    )

    return boto3.session.Session(
        aws_access_key_id=response['Credentials']['AccessKeyId'],
        aws_secret_access_key=response['Credentials']['SecretAccessKey'],
        aws_session_token=response['Credentials']['SessionToken']
    )

def get_token(username, password):

    encoded = base64.b64encode(f'{username}:{password}'.encode())
    token = f'Basic {encoded.decode("utf-8")}'

    response = requests.post(
        'https://libsys.wellcomelibrary.org/iii/sierra-api/v5/token',
        headers={
            'Authorization': f'Basic {encoded.decode("utf-8")}'
        },
    )

    json_content = json.loads(response.content.decode("utf-8"))
    return json_content['access_token']

def _get_field(varFields, fieldId):
    filteredVarFields = [d for d in varFields if d['fieldTag'] == fieldId]

    field = None
    if filteredVarFields:
        field = filteredVarFields[0].get('content', None)

    return field


def find_user(token, email):
    find_url = 'https://libsys.wellcomelibrary.org/iii/sierra-api/v5/patrons/find'
    response = requests.get(
        f"{find_url}?fields=varFields&varFieldTag=z&varFieldContent={email}",
        headers={
            'Authorization': f'Bearer {token}'
        },
    )

    loaded_response = json.loads(response.content)

    if not 'varFields' in loaded_response:
        return None

    varFields = loaded_response['varFields']

    username = _get_field(varFields, 's')
    email = _get_field(varFields, 'z')

    return {
        'patronId': loaded_response['id'],
        'username': username
    }

def create_user(role_arn, user_pool_id, user):
    session = create_session(role_arn)
    client = session.client('cognito-idp')

    temporary_password = f"CHANGEIT!_{str(uuid.uuid1())}"

    client.admin_create_user(
        UserPoolId=user_pool_id,
        Username=user['username'],
        UserAttributes=[
            { 'Name': 'custom:patronId', 'Value': str(user['patronId'])}
        ],
        TemporaryPassword=f"CHANGEIT!_{temporary_password}"
    )

    return {
        'username': user['username'],
        'password': temporary_password
    }

def migrate_user(role_arn, user_pool_id, sierra_id, sierra_secret, email):
    token = get_token(sierra_id, sierra_secret)

    user = find_user(token, email)
    return create_user(role_arn, user_pool_id, user)


if __name__ == "__main__":
    try:
        sierra_id = sys.argv[1]
    except IndexError:
        sys.exit(f"Usage: {__file__} <SIERRA_ID> <SIERRA_SECRET> <USER_POOL_ID> <EMAIL>")

    try:
        sierra_secret = sys.argv[2]
    except IndexError:
        sys.exit(f"Usage: {__file__} <SIERRA_ID> <SIERRA_SECRET> <USER_POOL_ID> <EMAIL>")

    try:
        user_pool_id = sys.argv[3]
    except IndexError:
        sys.exit(f"Usage: {__file__} <SIERRA_ID> <SIERRA_SECRET> <USER_POOL_ID> <EMAIL>")

    try:
        email = sys.argv[4]
    except IndexError:
        sys.exit(f"Usage: {__file__} <SIERRA_ID> <SIERRA_SECRET> <USER_POOL_ID> <EMAIL>")

    user = migrate_user(
        "arn:aws:iam::756629837203:role/catalogue-developer",
        user_pool_id, sierra_id, sierra_secret, email
    )

    print(json.dumps(user, indent=2, sort_keys=True))