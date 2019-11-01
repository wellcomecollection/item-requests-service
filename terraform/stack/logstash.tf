module "logstash_transit" {
  source = "git::https://github.com/wellcometrust/terraform.git//ecs/prebuilt/default?ref=4ceb43fb9c08f5ac8ec3bc43b03f8c3c81621b97"

  security_group_ids = [
    "${aws_security_group.service_egress_security_group.id}",
    "${aws_security_group.interservice_security_group.id}",
  ]

  cluster_id   = "${aws_ecs_cluster.cluster.id}"
  namespace_id = "${aws_service_discovery_private_dns_namespace.namespace.id}"
  subnets      = "${var.private_subnets}"
  service_name = "${var.namespace}_logstash_transit"

  env_vars = {
    XPACK_MONITORING_ENABLED = "false"
    NAMESPACE                = "items"
  }

  env_vars_length = 2

  secret_env_vars = {
    ES_HOST = "items/logstash/es_host"
    ES_USER = "items/logstash/es_user"
    ES_PASS = "items/logstash/es_pass"
  }

  secret_env_vars_length = 3

  cpu    = 1024
  memory = 2048

  container_image = "wellcome/logstash_transit:114"
}
