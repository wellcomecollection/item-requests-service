module "bastion" {
  source = "git::https://github.com/wellcometrust/terraform.git//ec2/prebuilt/bastion?ref=v17.1.0"

  vpc_id = "${local.vpc_id}"

  name = "stacks-bastion"

  controlled_access_cidr_ingress = ["${local.admin_cidr_ingress}"]

  key_name    = "${local.key_name}"
  subnet_list = "${local.public_subnets}"
}
