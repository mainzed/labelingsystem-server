# Authentification

    GET dump/repository/:id

## Description

Get current Labeling System dump.

## Parameters

- **id** _(required)_ — name of triplestore repository.

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
      "file": "some file"
    }
