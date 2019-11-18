# API
locals {
  stage_name = "v1"
}

resource "aws_api_gateway_rest_api" "api" {
  name = "Stacks API (${var.namespace})"

  endpoint_configuration = {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_authorizer" "cognito" {
  name          = "cognito"
  type          = "COGNITO_USER_POOLS"
  rest_api_id   = "${aws_api_gateway_rest_api.api.id}"
  provider_arns = ["${var.cognito_user_pool_arn}"]
}

# Domains

module "domain" {
  source = "../modules/domain"

  domain_name      = "${var.domain_name}"
  cert_domain_name = "${var.cert_domain_name}"
}

# Responses

module "responses" {
  source = "../modules/responses"

  api_id      = "${aws_api_gateway_rest_api.api.id}"
  context_url = "${local.context_url}"
}

# Stages

module "v1" {
  source = "git::https://github.com/wellcometrust/terraform.git//api_gateway/modules/stage?ref=v18.2.3"

  stage_name = "${local.stage_name}"

  api_id = "${aws_api_gateway_rest_api.api.id}"

  variables = {
    requests_port = "${local.requests_listener_port}"
    items_port    = "${local.items_listener_port}"
  }

  # All integrations
  depends_on = [
    "${concat(module.context.integration_uris, module.items.integration_uris,module.requests.integration_uris)}",
  ]
}

# Resources

module "context" {
  source = "../modules/resource_integration/context"

  api_id               = "${aws_api_gateway_rest_api.api.id}"
  api_root_resource_id = "${aws_api_gateway_rest_api.api.root_resource_id}"
  aws_region           = "${var.aws_region}"
  namespace            = "${var.namespace}"

  static_content_bucket_name = "${var.static_content_bucket_name}"
}

module "items" {
  source = "../modules/resource_integration/api_key"

  api_id    = "${aws_api_gateway_rest_api.api.id}"
  path_part = "items"

  root_resource_id = "${aws_api_gateway_rest_api.api.root_resource_id}"
  connection_id    = "${aws_api_gateway_vpc_link.link.id}"

  forward_port = "$${stageVariables.items_port}"
  forward_path = "items"
}

module "items_resource_cors" {
  source              = "../modules/resource_integration/cors"
  gateway_api_id      = "${aws_api_gateway_rest_api.api.id}"
  gateway_resource_id = "${module.items.resource_id}"
}

module "items_subresource_cors" {
  source              = "../modules/resource_integration/cors"
  gateway_api_id      = "${aws_api_gateway_rest_api.api.id}"
  gateway_resource_id = "${module.items.subresource_id}"
}

module "requests" {
  source = "../modules/resource_integration/auth"

  api_id    = "${aws_api_gateway_rest_api.api.id}"
  path_part = "requests"

  root_resource_id = "${aws_api_gateway_rest_api.api.root_resource_id}"
  connection_id    = "${aws_api_gateway_vpc_link.link.id}"

  cognito_id  = "${aws_api_gateway_authorizer.cognito.id}"
  auth_scopes = ["${var.auth_scopes}"]

  forward_port = "$${stageVariables.requests_port}"
  forward_path = "requests"
}

module "requests_resource_cors" {
  source              = "../modules/resource_integration/cors"
  gateway_api_id      = "${aws_api_gateway_rest_api.api.id}"
  gateway_resource_id = "${module.requests.resource_id}"
}

module "requests_subresource_cors" {
  source              = "../modules/resource_integration/cors"
  gateway_api_id      = "${aws_api_gateway_rest_api.api.id}"
  gateway_resource_id = "${module.requests.subresource_id}"
}

# Link

resource "aws_api_gateway_vpc_link" "link" {
  name        = "${var.namespace}-api_vpc_link"
  target_arns = ["${module.nlb.arn}"]
}

resource "aws_api_gateway_base_path_mapping" "stacks" {
  api_id      = "${aws_api_gateway_rest_api.api.id}"
  stage_name  = "${local.stage_name}"
  domain_name = "${module.domain.domain_name}"
  base_path   = "stacks"
}

resource "aws_api_gateway_api_key" "stacks_item_request_key" {
  name = "stacks_item_request_key"
}
