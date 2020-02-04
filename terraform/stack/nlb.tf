resource "aws_lb" "network_load_balancer" {
  name               = "${var.namespace}-stacks-api-nlb"
  internal           = true
  load_balancer_type = "network"
  subnets            = var.private_subnets
}
