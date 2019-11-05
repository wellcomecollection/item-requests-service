provider "aws" {
  assume_role {
    role_arn = "arn:aws:iam::756629837203:role/catalogue-developer"
  }

  region  = "${var.aws_region}"
  version = "1.60.0"
}

data "terraform_remote_state" "infra_shared" {
  backend = "s3"

  config {
    role_arn = "arn:aws:iam::760097843905:role/platform-read_only"

    bucket = "wellcomecollection-platform-infra"
    key    = "terraform/shared_infra.tfstate"
    region = "eu-west-1"
  }
}

data "terraform_remote_state" "infra_critical" {
  backend = "s3"

  config {
    role_arn = "arn:aws:iam::760097843905:role/platform-read_only"

    bucket = "wellcomecollection-platform-infra"
    key    = "terraform/catalogue_pipeline_data.tfstate"
    region = "eu-west-1"
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