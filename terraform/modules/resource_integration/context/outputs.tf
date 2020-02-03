output "integration_uris" {
  value = [
    aws_api_gateway_integration.root_static_response.id,
  ]
}
