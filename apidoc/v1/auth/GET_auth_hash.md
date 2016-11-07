# Authentification

    GET auth/hash

## Description

Get hashed string.

## Parameters

- **str** _(required)_ — plain text.

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
      "hash": "hashed string"
    }
