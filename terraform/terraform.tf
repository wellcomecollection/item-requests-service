data "aws_caller_identity" "current" {}

provider "aws" {
  region  = "eu-west-1"
  version = "1.60.0"

  assume_role {
    role_arn = "arn:aws:iam::756629837203:role/catalogue-developer"
  }
}

terraform {
  required_version = ">= 0.9"

  backend "s3" {
    role_arn = "arn:aws:iam::756629837203:role/catalogue-developer"

    bucket         = "wellcomecollection-catalogue-infra-delta"
    key            = "terraform/stacks.tfstate"
    dynamodb_table = "terraform-locktable"
    region         = "eu-west-1"
  }
}
