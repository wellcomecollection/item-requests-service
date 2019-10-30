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
    role_arn = "arn:aws:iam::756629837203:role/platform-developer"

    bucket         = "wellcomecollection-catalogue-infra"
    key            = "terraform/requests-service.tfstate"
    dynamodb_table = "terraform-locktable"
    region         = "eu-west-1"
  }
}

data "terraform_remote_state" "shared_infra" {
  backend = "s3"

  config {
    role_arn = "arn:aws:iam::760097843905:role/platform-developer"

    bucket = "wellcomecollection-platform-infra"
    key    = "terraform/shared_infra.tfstate"
    region = "eu-west-1"
  }
}

data "terraform_remote_state" "infra_critical" {
  backend = "s3"

  config {
    role_arn = "arn:aws:iam::760097843905:role/platform-developer"

    bucket = "wellcomecollection-platform-infra"
    key    = "terraform/catalogue_pipeline_data.tfstate"
    region = "eu-west-1"
  }
}

data "terraform_remote_state" "catalogue_api" {
  backend = "s3"

  config {
    role_arn = "arn:aws:iam::760097843905:role/platform-developer"

    bucket = "wellcomecollection-platform-infra"
    key    = "terraform/catalogue_api.tfstate"
    region = "eu-west-1"
  }
}
