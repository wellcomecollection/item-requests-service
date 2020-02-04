locals {
  services = [
    "items_api",
    "requests_api",
  ]
}

data "aws_ssm_parameter" "image_ids" {
  count = length(local.services)

  name = "/stacks/images/${var.release_label}/${local.services[count.index]}"
}

locals {
  image_ids = zipmap(local.services, data.aws_ssm_parameter.image_ids.*.value)
}
