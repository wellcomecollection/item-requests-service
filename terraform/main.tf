module "infra" {
  source = "infra"
}

//module "prod_stack" {
//  source = "stack"
//  namespace = "prod"
//
//  vpc_id          = "${module.infra.vpc_id}"
//  private_subnets = "${module.infra.private_subnets}"
//
//  domain_name      = "stacks.api.wellcomecollection.org"
//  cert_domain_name = "stacks.api.wellcomecollection.org"
//
//  cognito_user_pool_arn = "${module.infra.cognito_user_pool_arn}"
//
//  auth_scopes = [
//    "${module.infra.cognito_api_identifier}/items",
//    "${module.infra.cognito_api_identifier}/requests",
//  ]
//
//  static_content_bucket_name = "${module.infra.static_content_bucket_name}"
//
//  requests_container_image = "${module.infra.requests_repository_url}:d4fb6cc08b9559bbcd0e1c78ad0f0966d458d3d3"
//  items_container_image   = "${module.infra.items_repository_url}:acefecad19bbcb438abec7cd459a27f154620aad"
//}