# Authentification

    POST auth/logout

## Description

Logout from the editor mode.

## Parameters

- **user** _(required)_ — user name.

***

## Request Headers

    none

***

## Access Allow Origin

    localhost

***

## Response Format

    application/json;charset=UTF-8

## Response Headers

- **200 OK** — login details and user object.

***

## Errors

- **500 Internal Server Error** — error json.

***

## Example
**Request**

    none

**Response**

    {
      "verified": false,
      "user": "demo"
    }
