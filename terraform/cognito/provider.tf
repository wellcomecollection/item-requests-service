provider "aws" {
  region = "eu-west-1"

  assume_role {
    role_arn = "arn:aws:iam::756629837203:role/catalogue-developer"
  }
}

# This provider is being kept around to clean up the associated ACM cert
# for id.wellcomecollection.org.
#
# If there's no longer an `id.wellcomecollection.org` certificate in
# us-east-1 in the catalogue account, you can delete this provider.
#
# ACM won't let you delete a certificate that it thinks is in use – and it's
# taking a while to realise we've removed this certificate from a
# Cognito/CloudFront distribution.
provider "aws" {
  region = "us-east-1"
  alias  = "us_east_1"

  assume_role {
    role_arn = "arn:aws:iam::756629837203:role/catalogue-developer"
  }
}
