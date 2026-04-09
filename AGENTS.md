# Repository Guidelines

## Project Overview
- This repository contains a small Scala 3 `sbt` application that consumes PostgreSQL logical replication messages.
- Runtime dependencies and versions are defined in `build.sbt`.
- Local PostgreSQL setup for development lives in `docker-compose.yaml` and `init/init-db.sql`.

## Source Layout
- `src/main/scala/postgres/replication` contains the entrypoint and PostgreSQL client code.
- `src/main/scala/postgres/replication/protocol` contains logical replication protocol models and codecs.
- `src/main/scala/postgres/replication/streams` contains streaming helpers built on FS2.
- `src/main/resources` contains application and logging configuration.
- There is currently no `src/test` tree; add tests only when they directly support the requested change.

## Working Agreements
- Keep changes focused and minimal; do not refactor unrelated files.
- Follow existing Scala 3 style and naming patterns already used in the repo.
- Prefer pure, small helper methods over introducing extra abstractions.
- Do not add new libraries unless they are necessary for the requested task.
- When changing protocol parsing or streaming logic, preserve current package structure.

## Commands
- Format: `sbtn scalafmtAll`
- Test: `sbtn test`
- Run locally: `sbtn run`
- Start PostgreSQL locally: `docker compose up -d`
- Stop PostgreSQL locally: `docker compose down`

## Validation
- Run the smallest relevant validation first.
- For Scala source changes, prefer `sbt test` and then broader checks only if needed.
- If you change formatting-sensitive code, run `sbt scalafmtAll`.

## Notes
- CI currently runs `sbt test` on GitHub Actions with JDK 17.
- The project uses Scala `3.6.4` and `scalafmt` with the Scala 3 dialect.
