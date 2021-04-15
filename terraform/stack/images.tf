locals {
  services = [
    "items_api",
    "requests_api",
  ]
}

data "aws_ecr_repository" "service" {
  count = length(local.services)
  name  = "uk.ac.wellcome/${local.services[count.index]}"
}

locals {
  repo_urls = [for repo_url in data.aws_ecr_repository.service.*.repository_url : "${repo_url}:env.${var.release_label}"]
  image_ids = zipmap(local.services, local.repo_urls)
}
