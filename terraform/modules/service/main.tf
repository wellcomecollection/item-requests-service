resource "aws_lb_target_group" "tcp" {
  # Must only contain alphanumerics and hyphens.
  name = var.namespace

  target_type = "ip"

  protocol = "TCP"
  port     = var.nginx_container_port
  vpc_id   = var.vpc_id

  health_check {
    protocol = "TCP"
  }
}

resource "aws_lb_listener" "tcp" {
  load_balancer_arn = var.lb_arn
  port              = var.listener_port
  protocol          = "TCP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.tcp.arn
  }
}

module "service" {
  source = "github.com/wellcomecollection/terraform-aws-ecs-service.git//modules/service?ref=v3.4.0"

  service_name = var.namespace
  cluster_arn  = var.cluster_arn

  desired_task_count = var.desired_task_count

  task_definition_arn = module.task_definition.arn

  subnets = var.subnets

  security_group_ids = var.security_group_ids

  target_group_arn = aws_lb_target_group.tcp.arn
  container_name   = "nginx"
  container_port   = var.nginx_container_port
}

module "log_router_container" {
  source    = "git::github.com/wellcomecollection/terraform-aws-ecs-service.git//modules/firelens?ref=v3.4.0"
  namespace = var.namespace

  use_privatelink_endpoint = true
}

module "app_container" {
  source = "github.com/wellcomecollection/terraform-aws-ecs-service.git//modules/container_definition?ref=v3.4.0"
  name   = "app"

  image = var.container_image

  environment = var.env_vars
  secrets     = var.secret_env_vars

  log_configuration = module.log_router_container.container_log_configuration
}

module "nginx_container" {
  source = "git::github.com/wellcomecollection/terraform-aws-ecs-service.git//modules/nginx/apigw?ref=v3.4.0"

  forward_port      = var.container_port
  log_configuration = module.log_router_container.container_log_configuration
}

module "task_definition" {
  source = "git::github.com/wellcomecollection/terraform-aws-ecs-service.git//modules/task_definition?ref=v3.4.0"

  cpu    = var.cpu
  memory = var.memory

  container_definitions = [
    module.log_router_container.container_definition,
    module.app_container.container_definition,
    module.nginx_container.container_definition
  ]

  task_name = var.namespace
}
