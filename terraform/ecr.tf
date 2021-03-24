resource "aws_ecr_repository" "items_api" {
  name = "uk.ac.wellcome/items_api"
}

resource "aws_ecr_repository" "requests_api" {
  name = "uk.ac.wellcome/requests_api"
}
