provider "aws" {
  assume_role {
    role_arn = "arn:aws:iam::756629837203:role/catalogue-developer"
  }

  region  = "${var.aws_region}"
  version = "~> 2.47.0"
}
