module "images" {
  source = "git::https://github.com/wellcometrust/terraform.git//ecs/modules/images?ref=v19.8.0"

  label   = "${var.release_label}"
  project = "storage"

  services = [
    "items_api",
    "requests_api",
  ]
}
