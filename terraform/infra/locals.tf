locals {
  key_name = "wellcomedigitalcatalogue"

  vpc_id         = data.terraform_remote_state.infra_shared.outputs.catalogue_vpc_id
  public_subnets = data.terraform_remote_state.infra_shared.outputs.catalogue_vpc_public_subnets

  admin_cidr_ingress = "${data.aws_ssm_parameter.admin_cidr_ingress.value}"
}

data "aws_ssm_parameter" "admin_cidr_ingress" {
  name = "/stacks/config/prod/admin_cidr_ingress"
}
