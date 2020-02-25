locals {
  items_listener_port    = "65535"
  requests_listener_port = "65534"

  api_security_groups = [
    aws_security_group.service_egress_security_group.id,
    aws_security_group.service_lb_ingress_security_group.id,
  ]

  context_url = "https://api.wellcomecollection.org/stacks/v1/context.json"

  requests_api_image = local.image_ids["requests_api"]
  items_api_image    = local.image_ids["items_api"]

  // This is taken from the routemaster AWS account which doesn't expose it's terraform state
  routermaster_router53_zone_id = "Z3THRVQ5VDYDMC"

  logstash_transit_service_name = "${local.namespace_hyphen}_logstash_transit"
  logstash_host                 = "${local.logstash_transit_service_name}.${local.namespace_hyphen}"
  namespace_hyphen              = replace(var.namespace, "_", "-")
}

data "aws_vpc" "vpc" {
  id = var.vpc_id
}

/aws/reference/secretsmanager/items/logstash/es_host
/aws/reference/secretsmanager/items/logstash/es_pass
/aws/reference/secretsmanager/items/logstash/es_user