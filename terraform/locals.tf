locals {
  namespace     = "requests"
  dlq_alarm_arn = "${data.terraform_remote_state.shared_infra.dlq_alarm_arn}"

  vpc_id = "${data.terraform_remote_state.shared_infra.catalogue_vpc_delta_id}"

  private_subnets = "${data.terraform_remote_state.shared_infra.catalogue_vpc_delta_private_subnets}"

  infra_bucket = "${data.terraform_remote_state.shared_infra.infra_bucket}"

  aws_region = "eu-west-1"

  nlb_dns_name = "catalogue-api-gw-nlb-211b51d62cf2e58f.elb.eu-west-1.amazonaws.com"
}

data "aws_vpc" "vpc" {
  id = "${local.vpc_id}"
}
