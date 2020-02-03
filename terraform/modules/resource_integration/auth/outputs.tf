output "integration_uris" {
  value = [
    local.subresource_uri,
    local.resource_uri,
  ]
}

output "resource_id" {
  value = aws_api_gateway_resource.auth_resource.id
}

output "subresource_id" {
  value = aws_api_gateway_resource.auth_subresource.id
}
