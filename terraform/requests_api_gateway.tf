resource "aws_api_gateway_rest_api" "requests_api" {
  name        = "Item requests API"
  description = "Gateway for the requests API"
}

resource "aws_api_gateway_resource" "requests_api_proxy" {
  rest_api_id = "${aws_api_gateway_rest_api.requests_api.id}"
  parent_id   = "${aws_api_gateway_rest_api.requests_api.root_resource_id}"
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "requests_api_proxy" {
  rest_api_id   = "${aws_api_gateway_rest_api.requests_api.id}"
  resource_id   = "${aws_api_gateway_resource.requests_api_proxy.id}"
  http_method   = "GET"
  authorization = "NONE"

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

resource "aws_api_gateway_integration" "requests_api_proxy" {
  rest_api_id = "${aws_api_gateway_rest_api.requests_api.id}"
  resource_id = "${aws_api_gateway_method.requests_api_proxy.resource_id}"
  http_method = "${aws_api_gateway_method.requests_api_proxy.http_method}"

  integration_http_method = "GET"
  type                    = "HTTP_PROXY"
  uri                     = "http://${local.nlb_dns_name}:65525/{proxy}"

  connection_type = "VPC_LINK"
  connection_id   = "${data.aws_api_gateway_vpc_link.catalogue_api_vpc_link.id}"

  request_parameters = {
    integration.request.path.proxy = "method.request.path.proxy"
  }
}

resource "aws_api_gateway_method" "requests_api_proxy_post" {
  rest_api_id   = "${aws_api_gateway_rest_api.requests_api.id}"
  resource_id   = "${aws_api_gateway_resource.requests_api_proxy.id}"
  http_method   = "POST"
  authorization = "NONE"

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

resource "aws_api_gateway_integration" "requests_api_proxy_post" {
  rest_api_id = "${aws_api_gateway_rest_api.requests_api.id}"
  resource_id = "${aws_api_gateway_method.requests_api_proxy_post.resource_id}"
  http_method = "${aws_api_gateway_method.requests_api_proxy_post.http_method}"

  integration_http_method = "POST"
  type                    = "HTTP_PROXY"
  uri                     = "http://${local.nlb_dns_name}:65525/{proxy}"

  connection_type = "VPC_LINK"
  connection_id   = "${data.aws_api_gateway_vpc_link.catalogue_api_vpc_link.id}"

  request_parameters = {
    integration.request.path.proxy = "method.request.path.proxy"
  }
}

resource "aws_api_gateway_method" "requests_api_proxy_delete" {
  rest_api_id   = "${aws_api_gateway_rest_api.requests_api.id}"
  resource_id   = "${aws_api_gateway_resource.requests_api_proxy.id}"
  http_method   = "DELETE"
  authorization = "NONE"

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

resource "aws_api_gateway_integration" "requests_api_proxy_delete" {
  rest_api_id = "${aws_api_gateway_rest_api.requests_api.id}"
  resource_id = "${aws_api_gateway_method.requests_api_proxy_delete.resource_id}"
  http_method = "${aws_api_gateway_method.requests_api_proxy_delete.http_method}"

  integration_http_method = "DELETE"
  type                    = "HTTP_PROXY"
  uri                     = "http://${local.nlb_dns_name}:65525/{proxy}"

  connection_type = "VPC_LINK"
  connection_id   = "${data.aws_api_gateway_vpc_link.catalogue_api_vpc_link.id}"

  request_parameters = {
    integration.request.path.proxy = "method.request.path.proxy"
  }
}

resource "aws_api_gateway_method" "requests_api" {
  rest_api_id   = "${aws_api_gateway_rest_api.requests_api.id}"
  resource_id   = "${aws_api_gateway_rest_api.requests_api.root_resource_id}"
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "requests_api" {
  rest_api_id = "${aws_api_gateway_rest_api.requests_api.id}"
  resource_id = "${aws_api_gateway_method.requests_api.resource_id}"
  http_method = "${aws_api_gateway_method.requests_api.http_method}"

  integration_http_method = "GET"
  type                    = "HTTP_PROXY"
  uri                     = "http://${local.nlb_dns_name}:65525"

  connection_type = "VPC_LINK"
  connection_id   = "${data.aws_api_gateway_vpc_link.catalogue_api_vpc_link.id}"
}

resource "aws_api_gateway_deployment" "request_api" {
  depends_on = [
    "aws_api_gateway_integration.requests_api",
    "aws_api_gateway_integration.requests_api_proxy",
    "aws_api_gateway_integration.requests_api_proxy_post",
    "aws_api_gateway_integration.requests_api_proxy_delete",
  ]

  rest_api_id = "${aws_api_gateway_rest_api.requests_api.id}"
  stage_name  = "test"
}
