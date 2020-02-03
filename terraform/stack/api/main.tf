module "api" {
  source = "../../modules/service"

  namespace = "${var.name}-api"

  container_image = "${var.container_image}"
  container_port  = "9001"

  namespace_id = "${var.service_discovery_namespace_id}"

  cluster_arn = var.cluster_arn

  vpc_id = "${var.vpc_id}"

  security_group_ids = "${var.security_group_ids}"

  subnets = "${var.subnets}"

  nginx_container_port  = "9000"
  nginx_container_image = "wellcome/nginx_api-gw:77d1ba9b060a184097a26bc685735be343b1a754"

  env_vars        = "${var.env_vars}"
  secret_env_vars = "${var.secret_env_vars}"

  lb_arn        = "${var.nlb_arn}"
  listener_port = "${var.nlb_port}"

  nginx_cpu    = 1024
  nginx_memory = 2048
  app_cpu      = 1024
  app_memory   = 2048

  desired_task_count = 3
}
