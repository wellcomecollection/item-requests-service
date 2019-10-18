resource "aws_ecs_cluster" "requests_api" {
  name = "item-apis"
}

resource "aws_service_discovery_private_dns_namespace" "namespace" {
  name = "platform"
  vpc  = "${local.vpc_id}"
}
