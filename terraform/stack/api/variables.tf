variable "name" {}
variable "container_image" {}

variable "service_discovery_namespace_id" {}
variable "cluster_id" {}
variable "vpc_id" {}

variable "security_group_ids" {
  type = "list"
}

variable "subnets" {
  type = "list"
}

variable "env_vars" {
  type = "map"
}

variable "secret_env_vars" {
  type = "map"
}

variable "env_vars_length" {}
variable "secret_env_vars_length" {}

variable "nlb_arn" {}
variable "nlb_port" {}