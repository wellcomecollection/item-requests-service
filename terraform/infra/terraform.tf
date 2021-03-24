data "terraform_remote_state" "infra_catalogue" {
  backend = "s3"

  config = {
    role_arn = "arn:aws:iam::760097843905:role/platform-read_only"
    bucket   = "wellcomecollection-platform-infra"
    key      = "terraform/platform-infrastructure/accounts/catalogue.tfstate"
    region   = "eu-west-1"
  }
}

locals {
  catalogue_vpcs = data.terraform_remote_state.infra_catalogue.outputs
}
