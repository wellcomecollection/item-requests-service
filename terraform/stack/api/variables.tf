variable "name" {}
variable "container_image" {}

variable "service_discovery_namespace_id" {}
variable "cluster_arn" {}
variable "vpc_id" {}

variable "security_group_ids" {
  type = list(string)
}

variable "subnets" {
  type = list(string)
}

variable "env_vars" {
  type = map(string)
}

variable "secret_env_vars" {
  type = map(string)
}

variable "nlb_arn" {}
variable "nlb_port" {}