module "auth_resource" {
  source = "../../resource"

  api_id = "${var.api_id}"

  parent_id        = "${var.root_resource_id}"
  path_part        = "${var.path_part}"
  api_key_required = true
}

module "auth_resource_integration" {
  source = "git::https://github.com/wellcometrust/terraform.git//api_gateway/modules/integration/proxy?ref=v16.1.8"

  api_id        = "${var.api_id}"
  resource_id   = "${module.auth_resource.resource_id}"
  connection_id = "${var.connection_id}"

  hostname    = "${var.hostname}"
  http_method = "${module.auth_resource.http_method}"

  forward_port = "${var.forward_port}"
  forward_path = "${var.forward_path}"
}

module "auth_subresource" {
  source = "../../resource"

  api_id = "${var.api_id}"

  parent_id = "${module.auth_resource.resource_id}"
  path_part = "{proxy+}"

  request_parameters = {
    "method.request.path.proxy" = true
  }

  api_key_required = true
}

module "auth_subresource_integration" {
  source = "git::https://github.com/wellcometrust/terraform.git//api_gateway/modules/integration/proxy?ref=v16.1.8"

  api_id        = "${var.api_id}"
  resource_id   = "${module.auth_subresource.resource_id}"
  connection_id = "${var.connection_id}"

  hostname    = "${var.hostname}"
  http_method = "${module.auth_subresource.http_method}"

  forward_port = "${var.forward_port}"
  forward_path = "{proxy}"

  request_parameters = {
    "integration.request.path.proxy"              = "method.request.path.proxy"
    "integration.request.header.X-SierraPatronId" = "context.authorizer.claims.custom:patronId"
  }
}
