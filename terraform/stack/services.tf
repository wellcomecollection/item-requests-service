module "items_api" {
  source = "./api"
  name   = "items"

  cluster_id = "${aws_ecs_cluster.cluster.id}"

  container_image = "${local.items_api_image}"

  env_vars = {
    app_base_url      = "https://api.wellcomecollection.org/stacks/v1/items"
    context_url       = "https://api.wellcomecollection.org/stacks/v1/context.json"
    metrics_namespace = "items_api"
  }

  env_vars_length = 3

  nlb_arn  = "${module.nlb.arn}"
  nlb_port = "${local.items_listener_port}"

  secret_env_vars = {
    sierra_api_key    = "stacks/prod/sierra_api_key"
    sierra_api_secret = "stacks/prod/sierra_api_secret"
  }

  secret_env_vars_length = "2"

  security_group_ids = "${local.api_security_groups}"

  service_discovery_namespace_id = "${aws_service_discovery_private_dns_namespace.namespace.id}"

  subnets = "${var.private_subnets}"
  vpc_id  = "${var.vpc_id}"
}

module "requests_api" {
  source = "./api"
  name   = "requests"

  cluster_id = "${aws_ecs_cluster.cluster.id}"

  container_image = "${local.requests_api_image}"

  env_vars = {
    app_base_url      = "https://api.wellcomecollection.org/stacks/v1/requests"
    context_url       = "https://api.wellcomecollection.org/stacks/v1/context.json"
    metrics_namespace = "requests_api"
  }

  env_vars_length = 3

  nlb_arn  = "${module.nlb.arn}"
  nlb_port = "${local.requests_listener_port}"

  secret_env_vars = {
    sierra_api_key    = "stacks/prod/sierra_api_key"
    sierra_api_secret = "stacks/prod/sierra_api_secret"
  }

  secret_env_vars_length = "2"

  security_group_ids = "${local.api_security_groups}"

  service_discovery_namespace_id = "${aws_service_discovery_private_dns_namespace.namespace.id}"

  subnets = "${var.private_subnets}"
  vpc_id  = "${var.vpc_id}"
}
