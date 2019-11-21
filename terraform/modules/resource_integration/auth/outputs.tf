output "integration_uris" {
  value = [
    "${module.auth_subresource_integration.uri}",
    "${module.auth_resource_integration.uri}",
  ]
}

output "resource_id" {
  value = "${module.auth_resource.resource_id}"
}

output "subresource_id" {
  value = "${module.auth_subresource.resource_id}"
}
