# Information

    GET info

## Description

Get Labeling System information.

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
      "owner": "some people",
      "license": "All labels are under CC BY 4.0 licence.",
      "triples": 58969,
      "name": "demo Labeling System",
      "revisions": 2057,
      "agents": 2,
      "vocabs": {
        "public": 4,
        "count": 6
      },
      "labels": {
        "public": 2035,
        "count": 2050
      }
    }
