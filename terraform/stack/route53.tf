resource "aws_route53_record" "route53_record_prod" {
  name    = module.domain.domain_name
  type    = "A"
  zone_id = local.routermaster_router53_zone_id

  alias {
    evaluate_target_health = true
    name                   = module.domain.regional_domain_name
    zone_id                = module.domain.regional_zone_id
  }

  provider = aws.routermaster
}

provider "aws" {
  region  = "eu-west-1"
  version = "~> 2.47.0"
  alias   = "routermaster"

  assume_role {
    role_arn = "arn:aws:iam::250790015188:role/wellcomecollection-assume_role_hosted_zone_update"
  }
}
