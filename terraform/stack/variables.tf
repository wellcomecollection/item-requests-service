variable "namespace" {}
variable "cognito_user_pool_arn" {}

variable "domain_name" {}
variable "cert_domain_name" {}
variable "aws_region" {}

variable "auth_scopes" {}

variable "subnets" {
  type = "list"
}

# Likely this will be internalisable
variable "context_url" {}