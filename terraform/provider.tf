locals {
  default_tags = {
    TerraformConfigurationURL = "https://github.com/wellcomecollection/stacks-service/tree/master/terraform"
  }
}

provider "aws" {
  assume_role {
    role_arn = "arn:aws:iam::756629837203:role/catalogue-developer"
  }

  region  = var.aws_region
  version = "~> 2.7"
}

provider "aws" {
  alias = "platform"

  region  = var.aws_region
  version = "~> 2.47.0"

  assume_role {
    role_arn = "arn:aws:iam::760097843905:role/platform-developer"
  }
}

provider "aws" {
  region  = "eu-west-1"
  version = "~> 2.47.0"
  alias   = "dns"

  assume_role {
    role_arn = "arn:aws:iam::267269328833:role/wellcomecollection-assume_role_hosted_zone_update"
  }
}
