module "service" {
  source = "git::https://github.com/wellcometrust/terraform.git//ecs/modules/service/prebuilt/rest/tcp?ref=v19.6.0"

  vpc_id  = "${var.vpc_id}"
  subnets = ["${var.subnets}"]

  task_desired_count = "${var.task_desired_count}"

  ecs_cluster_id = "${var.cluster_id}"

  service_name = "${var.namespace}"
  namespace_id = "${var.namespace_id}"

  lb_arn              = "${var.lb_arn}"
  listener_port       = "${var.listener_port}"
  security_group_ids  = ["${var.security_group_ids}"]
  launch_type         = "${var.launch_type}"
  task_definition_arn = "${module.task.task_definition_arn}"
  container_port      = "${var.nginx_container_port}"
  container_name      = "sidecar"
}

module "task" {
  source = "github.com/wellcomecollection/terraform-aws-ecs-service.git//task_definition/container_with_sidecar?ref=v1.1.1"

  task_name = var.namespace

  cpu    = var.app_cpu + var.sidecar_cpu
  memory = var.app_memory + var.sidecar_memory

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
