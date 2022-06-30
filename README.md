# Arbiter2

Second generation of the project for Table Football community

---
[![CircleCI](https://circleci.com/gh/RMaiun/arbiter2/tree/dev.svg?style=shield)](https://app.circleci.com/pipelines/github/RMaiun/arbiter2)
<a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" /></a>
---
Application which manages results for Table football league.
Was created for educational purpose as a part of investigation of functional scala world
as a potential candidate for replacement for application which was already written
with Spring WebFlux.
After positive experience whole system was migrated to scala stack.
---

# Core functionality:

- workspace initialization
    - algorithm management
    - round management
- simple user management
    - user-bot relations
    - user rights check
- storing game history
- preparation and storing of results
- displaying results
- season stats calculation
- ELO rating calculation
- preparation of season reports
- data archiving and dump processing
    - archiving of all dataset
    - loading data dump to particular service

---

# Tech stack:

| Status | Tool      | Description                             |
|--------|-----------|-----------------------------------------|
| ✅      | MySql     | RDBM                                    |
| ✅      | doobie    | JDBC layer                              |
| ✅      | http4s    | HTTP server                             |
| ✅      | cats      | abstractions for functional programming |
| ✅      | circe     | JSON serde                              |
| ✅      | fs2rabbit | RabbitMQ connector                      |
| ✅      | scaffeine | cache implementation                    |
| ❌      | tapir     | routing DSL  + API documentation        |
| ❌      | scala 3   | -                                       |