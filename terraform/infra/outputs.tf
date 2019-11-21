output "private_subnets" {
  value = "${data.terraform_remote_state.infra_shared.catalogue_vpc_private_subnets}"
}

output "vpc_id" {
  value = "${data.terraform_remote_state.infra_shared.catalogue_vpc_id}"
}

output "cognito_user_pool_arn" {
  # value = "${data.terraform_remote_state.infra_critical.cognito_user_pool_arn}"
  # the cognito stuff is in TF0.12
  # TODO: Update this to TF0.12
  value = "arn:aws:cognito-idp:eu-west-1:760097843905:userpool/eu-west-1_oToO0mWFj"
}

output "cognito_api_identifier" {
  # value = "${data.terraform_remote_state.infra_critical.cognito_stacks_api_identifier}"
  # the cognito stuff is in TF0.12
  # TODO: Update this to TF0.12
  value = "eu-west-1_oToO0mWFj"
}

output "static_content_bucket_name" {
  value = "${aws_s3_bucket.static_content.id}"
}

output "static_content_bucket_arn" {
  value = "${aws_s3_bucket.static_content.arn}"
}

output "items_repository_url" {
  value = "${module.aws_ecr_repository_items_api.repository_url}"
}

output "requests_repository_url" {
  value = "${module.aws_ecr_repository_requests_api.repository_url}"
}
