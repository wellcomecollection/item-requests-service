module "infra" {
  source = "./infra"

  aws_region = var.aws_region
}

module "prod_stack" {
  source = "./stack"

  namespace     = "prod"
  release_label = "prod"

  vpc_id          = module.infra.vpc_id
  private_subnets = module.infra.private_subnets

  domain_name      = "stacks.api.wellcomecollection.org"
  cert_domain_name = "stacks.api.wellcomecollection.org"

  # Note: I've just hard-coded this value for now to get Terraform to
  # do a no-op plan.  I copied it from an old version of this infra,
  # but we can remove it when we delete all the Cognito infra.
  cognito_user_pool_arn = "arn:aws:cognito-idp:eu-west-1:760097843905:userpool/eu-west-1_oToO0mWFj"

  elastic_cloud_vpce_sg_id = local.elastic_cloud_vpce_sg_id

  auth_scopes = []

  static_content_bucket_name = module.infra.static_content_bucket_name

  providers = {
    aws.dns = aws.dns
  }
}
