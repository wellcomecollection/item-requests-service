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

  # TODO: This is hard-coded so that Terraform will do a no-op plan,
  # but we should come back and update this to use our latest approach
  # like the catalogue API at some point.
  nginx_image = "760097843905.dkr.ecr.eu-west-1.amazonaws.com/uk.ac.wellcome/nginx_apigw:f1188c2a7df01663dd96c99b26666085a4192167"
}

data "aws_vpc" "vpc" {
  id = var.vpc_id
}
