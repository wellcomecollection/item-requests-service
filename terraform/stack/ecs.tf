resource "aws_ecs_cluster" "cluster" {
  name = "stacks-api"
}

resource "aws_service_discovery_private_dns_namespace" "namespace" {
  name = "stacks_api"
  vpc  = var.vpc_id
}
