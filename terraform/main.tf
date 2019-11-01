module "infra" {
  source = "infra"
}

module "prod_stack" {
  source = "stack"

  namespace     = "prod"
  release_label = "prod"

  vpc_id          = "${module.infra.vpc_id}"
  private_subnets = "${module.infra.private_subnets}"

  domain_name      = "stacks.api.wellcomecollection.org"
  cert_domain_name = "stacks.api.wellcomecollection.org"

  cognito_user_pool_arn = "${module.infra.cognito_user_pool_arn}"

  auth_scopes = [
    "${module.infra.cognito_api_identifier}/items",
    "${module.infra.cognito_api_identifier}/requests",
  ]

  static_content_bucket_name = "${module.infra.static_content_bucket_name}"
}