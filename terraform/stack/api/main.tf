module "api" {
  source = "../../modules/service"

  namespace = "${var.name}-api"

  container_image = var.container_image
  container_port  = 9001

  cluster_arn = var.cluster_arn

  vpc_id = var.vpc_id

  security_group_ids = var.security_group_ids

  subnets = var.subnets

  nginx_container_port = 9000

  env_vars        = var.env_vars
  secret_env_vars = var.secret_env_vars

  lb_arn        = var.nlb_arn
  listener_port = var.nlb_port

  cpu    = 1024
  memory = 2048

  desired_task_count = 3

  deployment_service_name = var.deployment_service_name
  deployment_service_env  = var.deployment_service_env
}
