variable "rest_api_id" {
}

variable "response_type" {
}

variable "status_code" {
  default = 400
}

variable "label" {
}

variable "context_url" {
}

data "template_file" "error" {
  template = <<EOF
{
"@context":"${var.context_url}",
"errorType":"http",
"httpStatus":"${var.status_code}",
"label":"${var.label}",
"description":$context.error.messageString,
"type":"Error"
}
EOF

}

resource "aws_api_gateway_gateway_response" "response" {
  rest_api_id   = var.rest_api_id
  response_type = var.response_type
  status_code   = var.status_code

  response_templates = {
    "application/json" = replace(data.template_file.error.rendered, "\n", "")
  }

  response_parameters = {
    "gatewayresponse.header.Access-Control-Allow-Headers" = "'Content-Type,User-Agent,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
    "gatewayresponse.header.Access-Control-Allow-Methods" = "'DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT'"
    "gatewayresponse.header.Access-Control-Allow-Origin"  = "'*'"
  }
}

