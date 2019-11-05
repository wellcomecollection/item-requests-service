resource "aws_s3_bucket" "infra" {
  bucket = "wellcomecollection-catalogue-infra-delta"
  acl    = "private"

  lifecycle {
    prevent_destroy = true
  }

  versioning {
    enabled = true
  }
}

resource "aws_s3_bucket" "static_content" {
  bucket = "wellcomecollection-public-stacks-static"
  acl    = "private"
}
