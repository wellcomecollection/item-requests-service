resource "aws_cognito_user_pool" "pool" {
  name                     = "Wellcome Collection Identity"
  auto_verified_attributes = ["email"]

  admin_create_user_config {
    allow_admin_create_user_only = false
  }

  verification_message_template {
    default_email_option = "CONFIRM_WITH_CODE"
  }

  schema {
    attribute_data_type      = "String"
    developer_only_attribute = false
    mutable                  = false
    name                     = "email"
    required                 = true

    string_attribute_constraints {
      max_length = "2048"
      min_length = "0"
    }
  }
  schema {
    attribute_data_type      = "String"
    developer_only_attribute = false
    mutable                  = true
    name                     = "patronId"
    required                 = false

    string_attribute_constraints {
      max_length = "256"
      min_length = "1"
    }
  }
}

resource "aws_cognito_resource_server" "stacks_api" {
  identifier = "https://api.wellcomecollection.org/stacks/v1"
  name       = "Stacks API V1"

  scope {
    scope_name        = "requests_readwrite"
    scope_description = "Read and write requests"
  }

  scope {
    scope_name        = "items_readonly"
    scope_description = "Read the status of items"
  }

  user_pool_id = aws_cognito_user_pool.pool.id
}

resource "aws_cognito_user_pool_client" "web_auth" {
  name                                 = "Web auth"
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = concat(["openid", "email"], aws_cognito_resource_server.stacks_api.scope_identifiers)
  explicit_auth_flows                  = ["USER_PASSWORD_AUTH"]

  user_pool_id    = aws_cognito_user_pool.pool.id
  generate_secret = false

  callback_urls                = ["https://wellcomecollection.org/works/auth-code"]
  default_redirect_uri         = "https://wellcomecollection.org/works/auth-code"
  logout_urls                  = ["https://wellcomecollection.org/logout"]
  supported_identity_providers = ["COGNITO"]
}

resource "aws_cognito_user_pool_client" "web_auth_test" {
  name                                 = "Web auth test"
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = concat(["openid", "email"], aws_cognito_resource_server.stacks_api.scope_identifiers)
  explicit_auth_flows = [
    "ALLOW_CUSTOM_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH",
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_USER_SRP_AUTH",
  ]

  user_pool_id    = aws_cognito_user_pool.pool.id
  generate_secret = false

  callback_urls                = ["http://localhost:3000/works/auth-code"]
  logout_urls                  = ["http://localhost:3000/works/logout"]
  supported_identity_providers = ["COGNITO"]
}
