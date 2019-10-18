resource "aws_api_gateway_rest_api" "status_api" {
  name        = "Item status API"
  description = "Gateway for the requests API"
}

resource "aws_api_gateway_resource" "status_api_proxy" {
  rest_api_id = "${aws_api_gateway_rest_api.status_api.id}"
  parent_id   = "${aws_api_gateway_rest_api.status_api.root_resource_id}"
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "status_api_proxy" {
  rest_api_id   = "${aws_api_gateway_rest_api.status_api.id}"
  resource_id   = "${aws_api_gateway_resource.status_api_proxy.id}"
  http_method   = "GET"
  authorization = "NONE"

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

resource "aws_api_gateway_integration" "status_api_proxy" {
  rest_api_id = "${aws_api_gateway_rest_api.status_api.id}"
  resource_id = "${aws_api_gateway_method.status_api_proxy.resource_id}"
  http_method = "${aws_api_gateway_method.status_api_proxy.http_method}"

  integration_http_method = "GET"
  type                    = "HTTP_PROXY"
  uri                     = "http://${local.nlb_dns_name}:65524/{proxy}"

  connection_type = "VPC_LINK"
  connection_id   = "${data.aws_api_gateway_vpc_link.catalogue_api_vpc_link.id}"

  request_parameters = {
    integration.request.path.proxy = "method.request.path.proxy"
  }
}

resource "aws_api_gateway_method" "status_api" {
  rest_api_id   = "${aws_api_gateway_rest_api.status_api.id}"
  resource_id   = "${aws_api_gateway_rest_api.status_api.root_resource_id}"
  http_method   = "GET"
  authorization = "NONE"
}

resource "aws_api_gateway_integration" "status_api" {
  rest_api_id = "${aws_api_gateway_rest_api.status_api.id}"
  resource_id = "${aws_api_gateway_method.status_api.resource_id}"
  http_method = "${aws_api_gateway_method.status_api.http_method}"

  integration_http_method = "GET"
  type                    = "HTTP_PROXY"
  uri                     = "http://${local.nlb_dns_name}:65524"

  connection_type = "VPC_LINK"
  connection_id   = "${data.aws_api_gateway_vpc_link.catalogue_api_vpc_link.id}"
}

resource "aws_api_gateway_deployment" "status_api" {
  depends_on = [
    "aws_api_gateway_integration.status_api",
    "aws_api_gateway_integration.status_api_proxy",
  ]

  rest_api_id = "${aws_api_gateway_rest_api.status_api.id}"
  stage_name  = "test"
}
