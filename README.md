# Stacks

APIs for interacting  with the physical library.

## APIs

### /requests

Create or list holds for a library member.

### /items

View an items status.

## Developing

Use IntelliJ or ...

```
# Running test

make request_api-test 
make stacks_api-test 
```

## Releasing

```sh
# Ensure you have the correct AWS credentials in scope
# before running!

# build and publish apps
make requests_api-publish 
make items_api-publish 

# set up release
./docker_run.py --aws --root -- -it wellcome/release_tooling:119 prepare
./docker_run.py --aws --root -- -it wellcome/release_tooling:119 deploy

# cd terraform
terraform init
terraform apply
```

## Adding users

To create a Cognito user from a Sierra user:

```sh
./scripts/migrate_user.py "sierra_id" "sierra_secret" "eu-west-1_mypool" "email@sierra.com"
```