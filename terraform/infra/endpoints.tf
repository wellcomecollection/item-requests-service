resource "aws_route53_zone" "internal" {
  name = "stacks.internal."

  vpc {
    vpc_id = "${local.vpc_id}"
  }
}
