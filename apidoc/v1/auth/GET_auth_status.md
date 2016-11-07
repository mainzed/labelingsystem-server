# Authentification

    GET auth/status

## Description

Get status of logged in user.

## Parameters

- **user** _(required)_ — user name.
- **token** _(required)_ — secret token.

***

## Request Headers

    none

***

## Access Allow Origin

    *

***

## Response Format

    application/json;charset=UTF-8

## Response Headers

- **200 OK** — login details and user object.

***

## Errors

- **403 Forbidden** — empty json.
- **500 Internal Server Error** — error json.

***

## Example
**Request**

    none

**Response**

    {
      "status": {
      "verified": true,
      "role": "user",
      "date": "2016-07-29T09:48:24.722+0200",
      "user": "demo",
      "token": "token"
      }, "user": {
        "id": "demo",
        "title": "Dr.",
        "firstName": "Max",
        "lastName": "Mustermann",
        "orcid": "http://orcid.org/0000-0002-3246-3531",
        "affiliation": "http://d-nb.info/gnd/1063654211"
      }
    }


    {
      "verified": false,
      "user": "demo"
    }
