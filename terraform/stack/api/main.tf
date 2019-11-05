module "api" {
  source = "../../modules/service"

  namespace = "${var.name}-api"

  container_image = "${var.container_image}"
  container_port  = "9001"

  namespace_id = "${var.service_discovery_namespace_id}"

  cluster_id = "${var.cluster_id}"

  vpc_id = "${var.vpc_id}"

  security_group_ids = "${var.security_group_ids}"

  subnets = "${var.subnets}"

  nginx_container_port  = "9000"
  nginx_container_image = "wellcome/nginx_api-gw:77d1ba9b060a184097a26bc685735be343b1a754"

  env_vars        = "${var.env_vars}"
  env_vars_length = "${var.env_vars_length}"

  secret_env_vars        = "${var.secret_env_vars}"
  secret_env_vars_length = "${var.secret_env_vars_length}"

  lb_arn        = "${var.nlb_arn}"
  listener_port = "${var.nlb_port}"

  cpu                = 2048
  memory             = 4096
  sidecar_cpu        = 1024
  sidecar_memory     = 2048
  app_cpu            = 1024
  app_memory         = 2048
  task_desired_count = "3"
}
