module "aws_ecr_repository_requests_api" {
  source    = "git::https://github.com/wellcometrust/terraform.git//ecr?ref=v19.5.1"
  id        = "requests_api"
  namespace = "uk.ac.wellcome"
}

module "aws_ecr_repository_items_api" {
  source    = "git::https://github.com/wellcometrust/terraform.git//ecr?ref=v19.5.1"
  id        = "items_api"
  namespace = "uk.ac.wellcome"
}