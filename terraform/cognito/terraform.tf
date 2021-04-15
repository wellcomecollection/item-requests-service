terraform {
  required_version = ">= 0.12"

  backend "s3" {
    role_arn = "arn:aws:iam::756629837203:role/catalogue-developer"

    bucket         = "wellcomecollection-catalogue-infra-delta"
    key            = "terraform/stacks-service/cognito.tfstate"
    dynamodb_table = "terraform-locktable"
    region         = "eu-west-1"
  }
}
