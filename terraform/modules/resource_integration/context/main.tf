resource "aws_api_gateway_resource" "resource" {
  rest_api_id = "${var.api_id}"
  parent_id   = "${var.api_root_resource_id}"
  path_part   = "context.json"
}

resource "aws_s3_bucket_object" "context" {
  bucket  = "${var.static_content_bucket_name}"
  key     = "static/context.json"
  content = "${file("${path.module}/context.json")}"
  etag    = "${md5(file("${path.module}/context.json"))}"
}

resource "aws_api_gateway_method" "no_auth" {
  rest_api_id = var.api_id
  resource_id = aws_api_gateway_resource.resource.id
  http_method = "GET"

  authorization = "NONE"

  request_parameters = {}
}


resource "aws_api_gateway_integration" "root_static_response" {
  rest_api_id             = var.api_id
  resource_id             = aws_api_gateway_resource.resource.id
  http_method             = "GET"
  integration_http_method = "GET"
  type                    = "AWS"
  uri                     = "arn:aws:apigateway:${var.aws_region}:s3:path//${aws_s3_bucket_object.context.bucket}/${aws_s3_bucket_object.context.key}"

  credentials = aws_iam_role.static_resource_role.arn
}

resource "aws_api_gateway_method_response" "http_200" {
  rest_api_id = var.api_id
  resource_id = aws_api_gateway_resource.resource.id
  http_method = "GET"
  status_code = "200"
}

resource "aws_api_gateway_integration_response" "http_200" {
  rest_api_id = var.api_id
  resource_id = aws_api_gateway_resource.resource.id
  http_method = "GET"
  status_code = aws_api_gateway_method_response.http_200.status_code
}
