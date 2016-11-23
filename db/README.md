# SQlite databases

## labeling system database

### Structure

#### users table

manages users within the system.

| id         | user_name | pwd | activation_token | role |
| ---------- | --------- | --- | ---------------- | ---- |
| serial id  |           |     | -1 or date       |      |

#### login table

manages login and logout.

| user_name | role | date |
| --------- | ---- | ---- |
|           |      |      |

#### statistics table

contains statistics about a vocabulary

| vocabulary | lastModifyAction | wayback | translations | descriptions | linksexternal | linksinternal | linkscount | labelsount |
| ---------- | ---------------- | ------- | ------------ | ------------ | ------------- | ------------- | ---------- | ---------- |
|            |                  |         |              |              |               |               |            |            |

#### retcat table

contains the list of reference thesauri (comma seperated) for each vocabulary.

| vocabulary | retcat |
| ---------- | ------ |
|            |        |

#### retcatlist table

contains the vocabulary id for each vocabulary for list search.

| vocabulary | retcat |
| ---------- | ------ |
|            |        |

### TODO

* change admin password using /auth/hash
* change demo password using /auth/hash

## persons database

### Structure

| lastName | affilliation | firstName | id |
| -------- | ------------ | --------- | -- |
|          |              |           |    |
