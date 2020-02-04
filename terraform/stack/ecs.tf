resource "aws_ecs_cluster" "cluster" {
  name = "${var.namespace}-stacks"
}

resource "aws_service_discovery_private_dns_namespace" "namespace" {
  name = "${var.namespace}-stacks"
  vpc  = var.vpc_id
}
