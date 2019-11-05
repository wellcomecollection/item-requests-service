variable "namespace" {}
variable "cognito_user_pool_arn" {}

variable "domain_name" {}
variable "cert_domain_name" {}

variable "aws_region" {
  default = "eu-west-1"
}

variable "vpc_id" {}

variable "auth_scopes" {
  type = "list"
}

variable "private_subnets" {
  type = "list"
}

variable "static_content_bucket_name" {}

variable "release_label" {}