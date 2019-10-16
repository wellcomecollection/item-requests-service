module "requests_api" {
  source = "./modules/service/api"

  namespace = "requests-api"

  container_image = "760097843905.dkr.ecr.eu-west-1.amazonaws.com/uk.ac.wellcome/requests_api:67dffaf892b4464a01dd70df249369c34558a92b"
  container_port  = "9001"

  namespace_id = "${aws_service_discovery_private_dns_namespace.namespace.id}"

  cluster_id = "${aws_ecs_cluster.cluster.name}"

  vpc_id = "${local.vpc_id}"

  security_group_ids = [
    "${aws_security_group.service_egress_security_group.id}",
    "${aws_security_group.service_lb_ingress_security_group.id}",
  ]

  subnets               = ["${local.private_subnets}"]
  nginx_container_port  = "9000"
  nginx_container_image = "wellcome/nginx_api-gw:77d1ba9b060a184097a26bc685735be343b1a754"

  env_vars = {
    app_base_url      = "https://api.wellcomecollection.org/requests/v1/storage/v1/bags"
    context_url       = "https://api.wellcomecollection.org/requests/v1/context.json"
    metrics_namespace = "requests_api"
  }

  env_vars_length = 1

  secret_env_vars = {
    sierra_auth_user = "catalogue/requests/sierra_auth_user"
    sierra_auth_pass = "catalogue/requests/sierra_auth_pass"
  }

  secret_env_vars_length = 2

  lb_arn             = "${data.terraform_remote_state.catalogue_api.catalogue_api_nlb_arn}"
  listener_port      = "65525"
  cpu                = 2048
  memory             = 4096
  sidecar_cpu        = 1024
  sidecar_memory     = 2048
  app_cpu            = 1024
  app_memory         = 2048
  task_desired_count = "1"
}

resource "aws_service_discovery_private_dns_namespace" "namespace" {
  name = "platform"
  vpc  = "${local.vpc_id}"
}

resource "aws_ecs_cluster" "cluster" {
  name = "requests-api"
}
