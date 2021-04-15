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

  # Ignore deployment tags on services
  ignore_tags {
    keys = ["deployment:label"]
  }
}

provider "aws" {
  alias = "dns"

  region = "eu-west-1"

  assume_role {
    role_arn = "arn:aws:iam::267269328833:role/wellcomecollection-assume_role_hosted_zone_update"
  }
}

provider "aws" {
  alias = "experience"

  region = "eu-west-1"

  assume_role {
    role_arn = "arn:aws:iam::130871440101:role/experience-developer"
  }
}
