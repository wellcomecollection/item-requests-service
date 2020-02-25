module "logstash_transit_task" {
  source = "github.com/wellcomecollection/terraform-aws-ecs-service.git//task_definition/single_container?ref=v1.1.1"

  task_name = local.logstash_transit_service_name

  container_image = "wellcome/logstash_transit:edgelord"

  cpu    = 1024
  memory = 2048

  env_vars = {
    XPACK_MONITORING_ENABLED = "false"
    NAMESPACE                = "stacks"
  }

  secret_env_vars = {
    ES_HOST = "catalogue/logstash/es_host"
    ES_USER = "catalogue/logstash/es_user"
    ES_PASS = "catalogue/logstash/es_pass"
  }

  aws_region = var.aws_region
}

module "logstash_transit_service" {
  source = "github.com/wellcomecollection/terraform-aws-ecs-service.git//service?ref=v1.1.1"

  service_name = local.logstash_transit_service_name
  cluster_arn  = aws_ecs_cluster.cluster.arn

  task_definition_arn = module.logstash_transit_task.arn

  subnets = var.private_subnets

  namespace_id = aws_service_discovery_private_dns_namespace.namespace.id

  security_group_ids = [
    aws_security_group.service_egress_security_group.id,
    aws_security_group.interservice_security_group.id,
  ]
}
