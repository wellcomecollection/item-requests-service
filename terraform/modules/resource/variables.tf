variable "api_id" {}

variable "http_method" {
  default = "ANY"
}

variable "request_parameters" {
  type    = map(string)
  default = {}
}

variable "authorization" {
  default = ""
}

variable "authorizer_id" {
  default = "not_real"
}

variable "auth_scopes" {
  type    = list(string)
  default = []
}

variable "path_part" {}
variable "parent_id" {}
variable "api_key_required" {}
