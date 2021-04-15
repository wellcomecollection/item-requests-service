resource "aws_api_gateway_usage_plan" "dotorg" {
  name         = "wellcomecollection.org/works-${var.namespace}"
  description  = "Usage plan for the Wellcome Collection website"

  api_stages {
    api_id = aws_api_gateway_rest_api.api.id
    stage  = aws_api_gateway_stage.v1.stage_name
  }
}

resource "aws_api_gateway_api_key" "dotorg" {
  name = "wellcomecollection.org/works-${var.namespace}"
}

resource "aws_api_gateway_usage_plan_key" "dotorg" {
  key_id        = aws_api_gateway_api_key.dotorg.id
  key_type      = "API_KEY"
  usage_plan_id = aws_api_gateway_usage_plan.dotorg.id
}

module "dotorg_secrets" {
  source = "../modules/secrets"

  providers = {
    aws = aws.experience
  }

  key_value_map = {
    "stacks/${var.namespace}/api_key" = aws_api_gateway_api_key.dotorg.value
  }
}
