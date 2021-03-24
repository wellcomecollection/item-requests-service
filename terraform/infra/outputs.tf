output "private_subnets" {
  value = local.private_subnets
}

output "vpc_id" {
  value = local.vpc_id
}

output "static_content_bucket_name" {
  value = aws_s3_bucket.static_content.id
}
