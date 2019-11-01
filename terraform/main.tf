module "infra" {
  source = "infra"
}

module "prod_stack" {
  source = "stack"
  namespace = "prod"

  vpc_id          = "${module.infra.vpc_id}"
  private_subnets = "${module.infra.private_subnets}"

  domain_name      = ""
  cert_domain_name = ""

  cognito_user_pool_arn = "${module.infra.cognito_user_pool_arn}"

  auth_scopes = [
    "${module.infra.cognito_api_identifier}/items",
    "${module.infra.cognito_api_identifier}/requests",
  ]

  context_url = ""

  static_content_bucket_name = "${module.infra.static_content_bucket_name}"

  requests_container_image = ""
  status_container_image = ""
}