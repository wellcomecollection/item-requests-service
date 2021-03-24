locals {
  default_tags = {
    TerraformConfigurationURL = "https://github.com/wellcomecollection/stacks-service/tree/master/terraform"
  }
}

provider "aws" {
  assume_role {
    role_arn = "arn:aws:iam::756629837203:role/catalogue-developer"
  }

  region = var.aws_region
}

provider "aws" {
  alias = "platform"

  region = var.aws_region

  assume_role {
    role_arn = "arn:aws:iam::760097843905:role/platform-developer"
  }
}

provider "aws" {
  alias = "dns"

  region = "eu-west-1"

  assume_role {
    role_arn = "arn:aws:iam::267269328833:role/wellcomecollection-assume_role_hosted_zone_update"
  }
}
