output "private_subnets" {
  value = data.terraform_remote_state.infra_shared.outputs.catalogue_vpc_private_subnets
}

output "vpc_id" {
  value = local.vpc_id
}

output "static_content_bucket_name" {
  value = aws_s3_bucket.static_content.id
}
