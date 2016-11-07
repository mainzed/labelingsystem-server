# Authentification

    GET auth/users

## Description

Get list of users uncluding roles and status.

## Parameters

    none

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

- **500 Internal Server Error** — error json.

***

## Example
**Request**

    none

**Response**

    {
      "role": "administrator",
      "username": "admin",
      "status": "active"
    }
