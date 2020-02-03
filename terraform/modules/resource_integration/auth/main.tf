resource "aws_api_gateway_resource" "auth_resource" {
  rest_api_id = var.api_id
  parent_id   = var.root_resource_id
  path_part   = var.path_part
}

resource "aws_api_gateway_method" "auth_resource" {
  rest_api_id = var.api_id
  resource_id = aws_api_gateway_resource.auth_resource.id
  http_method = "ANY"

  authorization        = "COGNITO_USER_POOLS"
  authorizer_id        = var.cognito_id
  authorization_scopes = var.auth_scopes

  request_parameters = {}
}

locals {
  resource_domain = "${var.hostname}:${var.forward_port}"
  resource_uri    = "http://${local.resource_domain}/${var.forward_path}"
}

resource "aws_api_gateway_integration" "auth_resource_integration" {
  rest_api_id = var.api_id
  resource_id = aws_api_gateway_resource.auth_resource.id
  http_method = "ANY"

  integration_http_method = "ANY"
  type                    = "HTTP_PROXY"
  connection_type         = "VPC_LINK"
  connection_id           = var.connection_id
  uri                     = local.resource_uri

  request_parameters = {
    "integration.request.header.Weco-Sierra-Patron-Id" = "context.authorizer.claims.custom:patronId"
  }
}

resource "aws_api_gateway_resource" "auth_subresource" {
  rest_api_id = var.api_id
  parent_id   = aws_api_gateway_resource.auth_resource.id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "auth_subresource" {
  rest_api_id = var.api_id
  resource_id = aws_api_gateway_resource.auth_subresource.id
  http_method = "ANY"

  authorization        = "COGNITO_USER_POOLS"
  authorizer_id        = var.cognito_id
  authorization_scopes = var.auth_scopes

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

locals {
  subresource_domain = "${var.hostname}:${var.forward_port}"
  subresource_uri    = "http://${local.subresource_domain}/${var.forward_path}/{proxy}"
}

resource "aws_api_gateway_integration" "auth_subresource_integration" {
  rest_api_id = var.api_id
  resource_id = aws_api_gateway_resource.auth_subresource.id
  http_method = "ANY"

  integration_http_method = "ANY"
  type                    = "HTTP_PROXY"
  connection_type         = "VPC_LINK"
  connection_id           = var.connection_id
  uri                     = local.subresource_uri

  request_parameters = {
    "integration.request.path.proxy"                   = "method.request.path.proxy"
    "integration.request.header.Weco-Sierra-Patron-Id" = "context.authorizer.claims.custom:patronId"
  }
}
