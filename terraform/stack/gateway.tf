# API

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
  context_url = "${var.context_url}"
}

# Stages

module "v1" {
  source = "git::https://github.com/wellcometrust/terraform.git//api_gateway/modules/stage?ref=v18.2.3"

  stage_name = "v1"

  api_id = "${aws_api_gateway_rest_api.api.id}"

  variables = {
    requests_port = "${local.requests_listener_port}"
    status_port  = "${local.status_listener_port}"
  }

  # All integrations
  depends_on = [
    "${concat(module.context.integration_uris, module.items.integration_uris,module.requests.integration_uris)}",
  ]
}

# Resources

module "context" {
  source = "../modules/resource/context"

  api_id = "${aws_api_gateway_rest_api.api.id}"
  api_root_resource_id = "${aws_api_gateway_rest_api.api.root_resource_id}"
  aws_region = "${var.aws_region}"
  namespace = "${var.namespace}"

  static_content_bucket_name = "${var.static_content_bucket_name}"
}

module "items" {
  source = "../modules/resource/auth"

  api_id    = "${aws_api_gateway_rest_api.api.id}"
  path_part = "items"

  root_resource_id = "${aws_api_gateway_rest_api.api.root_resource_id}"
  connection_id    = "${aws_api_gateway_vpc_link.link.id}"

  cognito_id  = "${aws_api_gateway_authorizer.cognito.id}"
  auth_scopes = ["${var.auth_scopes}"]

  forward_port = "$${stageVariables.items_port}"
  forward_path = "items"
}

module "requests" {
  source = "../modules/resource/auth"

  api_id    = "${aws_api_gateway_rest_api.api.id}"
  path_part = "requests"

  root_resource_id = "${aws_api_gateway_rest_api.api.root_resource_id}"
  connection_id    = "${aws_api_gateway_vpc_link.link.id}"

  cognito_id  = "${aws_api_gateway_authorizer.cognito.id}"
  auth_scopes = ["${var.auth_scopes}"]

  forward_port = "$${stageVariables.requests_port}"
  forward_path = "requests"
}

# Link

resource "aws_api_gateway_vpc_link" "link" {
  name        = "${var.namespace}-api_vpc_link"
  target_arns = ["${module.nlb.arn}"]
}
