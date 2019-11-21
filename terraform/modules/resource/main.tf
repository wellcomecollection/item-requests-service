resource "aws_api_gateway_resource" "resource" {
  rest_api_id = "${var.api_id}"
  parent_id   = "${var.parent_id}"
  path_part   = "${var.path_part}"
}

resource "aws_api_gateway_method" "no_auth" {
  rest_api_id      = "${var.api_id}"
  resource_id      = "${aws_api_gateway_resource.resource.id}"
  http_method      = "${var.http_method}"
  authorization    = "NONE"
  api_key_required = true

  request_parameters = "${var.request_parameters}"
}
