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

module "root_resource_method_static" {
  source = "git::https://github.com/wellcometrust/terraform.git//api_gateway/prebuilt/method/static?ref=v18.2.3"

  api_id      = "${var.api_id}"
  resource_id = "${aws_api_gateway_resource.resource.id}"

  aws_region  = "${var.aws_region}"
  bucket_name = "${aws_s3_bucket_object.context.bucket}"
  s3_key      = "${aws_s3_bucket_object.context.key}"

  static_resource_role_arn = "${aws_iam_role.static_resource_role.arn}"
}
