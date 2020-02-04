output "integration_uris" {
  value = [
    local.subresource_uri,
    local.resource_uri,
  ]
}

output "subresource_id" {
  value = module.auth_subresource.resource_id
}

output "resource_id" {
  value = module.auth_resource.resource_id
}
