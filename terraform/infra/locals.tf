locals {
  key_name = "wellcomedigitalcatalogue"

  vpc_id         = "${data.terraform_remote_state.infra_shared.catalogue_vpc_id}"
  public_subnets = "${data.terraform_remote_state.infra_shared.catalogue_vpc_public_subnets}"
}
