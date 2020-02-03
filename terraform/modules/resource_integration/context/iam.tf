resource "aws_iam_role_policy" "allow_gateway_s3_access" {
  policy = data.aws_iam_policy_document.static_content_get.json
  role   = aws_iam_role.static_resource_role.id
}

resource "aws_iam_role" "static_resource_role" {
  name               = "${var.namespace}_static_resource_role"
  assume_role_policy = data.aws_iam_policy_document.api_gateway_assume_role.json
}

data "aws_iam_policy_document" "api_gateway_assume_role" {
  statement {
    actions = [
      "sts:AssumeRole",
    ]

    principals {
      type        = "Service"
      identifiers = ["apigateway.amazonaws.com"]
    }
  }
}

data "aws_s3_bucket" "static_content_bucket" {
  bucket = var.static_content_bucket_name
}

data "aws_iam_policy_document" "static_content_get" {
  statement {
    actions = [
      "s3:GetObject*",
    ]

    resources = [
      "${data.aws_s3_bucket.static_content_bucket.arn}/${aws_s3_bucket_object.context.key}",
    ]
  }
}
