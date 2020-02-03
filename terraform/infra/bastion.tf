module "bastion" {
  source = "../modules/bastion_host"

  name = "stacks-bastion"

  subnet_list = local.public_subnets

  key_name    = local.key_name

  vpc_id = local.vpc_id

  controlled_access_cidr_ingress = [local.admin_cidr_ingress]
}
