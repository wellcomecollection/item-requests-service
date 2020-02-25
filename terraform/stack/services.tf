module "items_api" {
  source = "./api"
  name   = "items"

  cluster_arn = aws_ecs_cluster.cluster.arn

  container_image = local.items_api_image

  env_vars = {
    app_base_url       = "https://api.wellcomecollection.org/stacks/v1/items"
    context_url        = "https://api.wellcomecollection.org/stacks/v1/context.json"
    catalogue_base_url = "https://api.wellcomecollection.org/catalogue/v2"

    metrics_namespace = "items_api"
  }

  nlb_arn  = aws_lb.network_load_balancer.arn
  nlb_port = local.items_listener_port

  secret_env_vars = {
    sierra_api_key    = "stacks/prod/sierra_api_key"
    sierra_api_secret = "stacks/prod/sierra_api_secret"
  }

  security_group_ids = local.api_security_groups

  service_discovery_namespace_id = aws_service_discovery_private_dns_namespace.namespace.id

  subnets = var.private_subnets
  vpc_id  = var.vpc_id
}

module "requests_api" {
  source = "./api"
  name   = "requests"

  cluster_arn = aws_ecs_cluster.cluster.arn

  container_image = local.requests_api_image

  env_vars = {
    app_base_url       = "https://api.wellcomecollection.org/stacks/v1/requests"
    context_url        = "https://api.wellcomecollection.org/stacks/v1/context.json"
    catalogue_base_url = "https://api.wellcomecollection.org/catalogue/v2"

    metrics_namespace = "requests_api"
  }

  nlb_arn  = aws_lb.network_load_balancer.arn
  nlb_port = local.requests_listener_port

  secret_env_vars = {
    sierra_api_key    = "stacks/prod/sierra_api_key"
    sierra_api_secret = "stacks/prod/sierra_api_secret"
  }

  security_group_ids = local.api_security_groups

  service_discovery_namespace_id = aws_service_discovery_private_dns_namespace.namespace.id

  subnets = var.private_subnets
  vpc_id  = var.vpc_id
}
