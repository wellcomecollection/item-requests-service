output "integration_uris" {
  value = [
    "${module.root_resource_method_static.integration_id}",
  ]
}
