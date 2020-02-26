#!/usr/bin/env python
"""
This file implements a version of the Cognito "authorisation user code grant"
flow, as described in Amazon's docs at
https://aws.amazon.com/blogs/mobile/understanding-amazon-cognito-user-pool-oauth-2-0-grants/

It is designed to mimic the flow of a user who authenticates through the
browser -- it does some page scraping and nastiness which could be simplified
by using the Cognito IdentityProvider SDK, but we do it this way to get as close
as possible to the "real" flow.

This implementation is useful if:

-   You want to test the API
-   You don't have local AWS credentials

See the example at the bottom of the file for how to use this function.

"""

import bs4
import hyperlink
import requests


def get_authorization_token(
    auth_domain, client_id, redirect_uri, scope, username, password
):
    """
    Get an authorization token that can be passed as a header in requests
    to the Stacks API.
    """
    sess = requests.Session()

    # Step 1: Make an HTTP GET request to https://AUTH_DOMAIN/oauth2/authorize,
    # where AUTH_DOMAIN represents the user pool’s configured domain.
    authorize_resp = sess.get(
        f"https://{auth_domain}/oauth2/authorize",
        params={
            "response_type": "code",
            "client_id": client_id,
            "redirect_uri": redirect_uri,
            "scope": scope,
        },
    )

    authorize_resp.raise_for_status()

    # Step 2: A CSRF token is returned in a cookie. […] The end user is
    # redirected to https://AUTH_DOMAIN/login (which hosts the auto-generated UI)
    # with the same query parameters set from step 1.
    #
    # To see how this response works, we went through the auth flow
    # in Chrome and watched the network requests in the Dev Tools.

    # The auto-generated login form presents a <form> with a CSRF token.
    # We need to include the CSRF token in our login request, or it
    # will be rejected.
    soup = bs4.BeautifulSoup(authorize_resp.text, "html.parser")
    csrf_token = soup.find("input", attrs={"name": "_csrf"}).attrs["value"]

    login_resp = sess.post(
        authorize_resp.url,
        data={"_csrf": csrf_token, "username": username, "password": password,},
    )

    # Step 4: After Amazon Cognito verifies the user pool credentials or
    # provider tokens/assertion it receives, the user is redirected to the URL
    # that was specified in the original redirect_uri query parameter. The
    # redirect also sets a code query parameter that specifies the
    # authorization code that was vended to the user by Amazon Cognito.
    auth_code_url = login_resp.url

    if not auth_code_url.startswith(redirect_uri):
        raise RuntimeError(
            f"Error in the login form: not redirected to {redirect_uri}!"
        )

    url = hyperlink.URL.from_text(auth_code_url)
    (authorization_code,) = url.get("code")

    # Step 5: The custom application that’s hosted at the redirect URL
    # can then extract the authorization code from the query parameters
    # and exchange it for user pool tokens. The exchange occurs by submitting
    # a POST request to https://AUTH_DOMAIN/oauth2/token
    token_resp = sess.post(
        f"https://{auth_domain}/oauth2/token",
        data={
            "grant_type": "authorization_code",
            "code": authorization_code,
            "client_id": client_id,
            "redirect_uri": redirect_uri,
        },
    )

    token_resp.raise_for_status()

    return token_resp.json()["id_token"]


if __name__ == "__main__":
    import getpass
    import pprint

    password = getpass.getpass()

    auth_header = get_authorization_token(
        auth_domain="id.wellcomecollection.org",
        client_id="5n4vt54rjsg6t691c5b5kiacdv",
        redirect_uri="http://localhost:3000/works/auth-code",
        scope="openid",
        username="stacks_test_user",
        password=password,
    )

    requests_resp = requests.post(
        "https://stacks.api.wellcomecollection.org/stacks/v1/requests",
        headers={
            "Authorization": auth_header,
            "Content-Type": "application/json",
            "Weco-Sierra-Patron-Id": "doesnotexist",
        },
        json={
            "item": {"id": "xbq6d9rg", "type": "Item"},
            "pickupDate": "2020-02-28T14:02:09.497Z",
            "type": "Request",
        },
    )

    pprint.pprint(requests_resp.json())
