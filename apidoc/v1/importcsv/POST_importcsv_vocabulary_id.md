# Import CSV

    POST importcsv/vocabulary/:id

## Description

Send CSV to triplestore and create labels.

## Parameters

- **fileName** _(required)_ — name of the file.

***

## Request Headers

    Content-Type: multipart/form-data

***

## Access Allow Origin

    *

***

## Response Format

    application/json;charset=UTF-8

## Response Headers

- **200 OK** — import details.
- **400 Bad Request** — error details in csv file.

***

## Errors

- **500 Internal Server Error** — error json.

***

## Example
**Request**

    none

**Response**

    {
      "importedlabels": 2,
      "triples": 20
    }


    {
      "errors": 1,
      "messages": ["message"]
    }
