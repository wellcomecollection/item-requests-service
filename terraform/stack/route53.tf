data "aws_route53_zone" "dotorg" {
  provider = aws.dns
  name     = "wellcomecollection.org."
}

resource "aws_route53_record" "route53_record_prod" {
  name    = module.domain.domain_name
  type    = "A"
  zone_id = data.aws_route53_zone.dotorg.id

  alias {
    evaluate_target_health = true
    name                   = module.domain.regional_domain_name
    zone_id                = module.domain.regional_zone_id
  }

  provider = aws.dns
}
