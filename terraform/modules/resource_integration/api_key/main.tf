module "auth_resource" {
  source = "../../resource"

  api_id = var.api_id

  parent_id        = var.root_resource_id
  path_part        = var.path_part
  api_key_required = true
}

locals {
  resource_domain = "${var.hostname}:${var.forward_port}"
  resource_uri    = "http://${local.resource_domain}/${var.forward_path}"
}

resource "aws_api_gateway_integration" "auth_resource" {
  rest_api_id = var.api_id
  resource_id = module.auth_resource.resource_id
  http_method = module.auth_resource.http_method

  integration_http_method = "ANY"
  type                    = "HTTP_PROXY"
  connection_type         = "VPC_LINK"
  connection_id           = var.connection_id
  uri                     = local.resource_uri

  request_parameters = {}
}

module "auth_subresource" {
  source = "../../resource"

  api_id = var.api_id

  parent_id = module.auth_resource.resource_id
  path_part = "{proxy+}"

  request_parameters = {
    "method.request.path.proxy" = true
  }

  api_key_required = true
}

locals {
  subresource_domain = "${var.hostname}:${var.forward_port}"
  subresource_uri    = "http://${local.subresource_domain}/${var.forward_path}/{proxy}"
}

resource "aws_api_gateway_integration" "auth_subresource" {
  rest_api_id = var.api_id
  resource_id = module.auth_subresource.resource_id
  http_method = module.auth_subresource.http_method

  integration_http_method = "ANY"
  type                    = "HTTP_PROXY"
  connection_type         = "VPC_LINK"
  connection_id           = var.connection_id
  uri                     = local.subresource_uri

  request_parameters = {
    "integration.request.path.proxy"              = "method.request.path.proxy"
    "integration.request.header.X-SierraPatronId" = "context.authorizer.claims.custom:patronId"
  }
}
