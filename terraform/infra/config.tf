data "aws_ssm_parameter" "admin_cidr_ingress" {
  name = "/stacks/config/prod/admin_cidr_ingress"
}

locals {
  admin_cidr_ingress = "${data.aws_ssm_parameter.admin_cidr_ingress.value}"
}
