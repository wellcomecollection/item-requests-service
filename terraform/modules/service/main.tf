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
  source = "github.com/wellcomecollection/terraform-aws-ecs-service.git//service?ref=v1.1.1"

  service_name = var.namespace
  cluster_arn  = var.cluster_arn

  desired_task_count = var.desired_task_count

  task_definition_arn = module.task.arn

  subnets = var.subnets

  namespace_id = var.namespace_id

  security_group_ids = var.security_group_ids

  target_group_arn = aws_lb_target_group.tcp.arn
  container_name   = "nginx"
  container_port   = var.nginx_container_port
}

module "task" {
  source = "github.com/wellcomecollection/terraform-aws-ecs-service.git//task_definition/container_with_sidecar?ref=v1.1.1"

  task_name = var.namespace

  cpu    = var.app_cpu + var.nginx_cpu
  memory = var.app_memory + var.nginx_memory

  app_container_image = var.container_image
  app_container_port  = var.container_port
  app_cpu             = var.app_cpu
  app_memory          = var.app_memory

  app_env_vars        = var.env_vars
  secret_app_env_vars = var.secret_env_vars

  sidecar_container_image = var.nginx_container_image
  sidecar_container_port  = var.nginx_container_port
  sidecar_cpu             = var.nginx_cpu
  sidecar_memory          = var.nginx_memory

  sidecar_env_vars = {
    APP_HOST = "localhost"
    APP_PORT = var.container_port
  }

  aws_region = var.aws_region
}
