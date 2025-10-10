# Fullstack Scala application with Smithy4s

This is a full-stack Scala application (simple key-value management store), made with the following excellent libraries and technologies:

1. [Scala 3](https://docs.scala-lang.org/scala3/new-in-scala3.html)
2. [Smithy4s](https://disneystreaming.github.io/smithy4s/) and [Smithy](https://awslabs.github.io/smithy/2.0/index.html#) for the API and protocol
   definition between backend and frontend (and in tests)
3. [Scala.js](https://www.scala-js.org) and [Laminar](https://laminar.dev) for the [SPA](https://en.wikipedia.org/wiki/Single-page_application) frontend
4. [PostgreSQL](https://www.postgresql.org) and [Skunk](https://typelevel.org/skunk/) for database access
5. [Weaver-Test](https://disneystreaming.github.io/weaver-test/) for unit and integration testing
6. [Playwright](https://playwright.dev/) tests for frontend testing, using the dedicated [Weaver integration](https://github.com/indoorvivants/weaver-playwright)

~~The entire app is auto-deployed to [Fly.io](https://fly.io), you can see the live version [here](https://smithy4s-fullstack-template.fly.dev)~~. This has moved to my own cluster, [**live version here**]( https://smithy4s-fullstack-template.indoorvivants.com). Fly.io specific code left in the repository, but the app won't be deployed there anymore.

It started out as a trimmed down version of an app I've built in my [4-part blogpost series](https://blog.indoorvivants.com/2022-06-10-smithy4s-fullstack-part-1) about full-stack Scala and Smithy4s.
The blog series should answer a lot of the design questions you might have in much
greater detail than the margins of this tome allow.

![2022-08-22 17 21 03](https://user-images.githubusercontent.com/1052965/185972992-fb49f348-a33d-4e1a-aafa-7f9a01d3a9c1.gif)


## What is it for?

Starting a full stack application with Smithy4s is by far not the most lightweight approach,
but if you are comfortable with some of the technologies involved, this puts your project
immediately on a more solid footing than if you started small and re-wrote later (such rewrites tend to rarely happen, instead cruft is being added on and accumulated).

By using Smithy as the contract between frontend and backend, we immediately turn backend into
a solid API, and ensure that the interaction between the two never goes out of sync.

This template incorporates some of the lessons I've learned after deploying several personal
full stack applications:

1. Frontend is deployed separately from the backend, with NGINX serving both
2. We use [smithy4s-fetch](https://github.com/neandertech/smithy4s-fetch) to significantly reduce bundle size on the frontend
3. The Dockerfile is multi-stage and self-contained, producing a working application just by running `docker build . -t smithy4s-fullstack`

The current choices of the libraries on the backend is deliberate, it makes it possible
to [soon run the backend on Scala Native](https://blog.indoorvivants.com/2025-09-22-snbindgenweb-typelevel-stack-on-scala-native-05).

## Running

While most of the defaults are already configured, the only missing bit is the Postgres communication.
It can be picked up from the environment, but best way to do it would be to put a file named `.env`
at the root of the project with a JDBC-compatible Postgres URL:

```
DATABASE_URL=postgres://postgres:mysecretpassword@localhost:5432/postgres
```

1. To run just the backend, run `~app/reStart` in SBT shell, it will continuously restart the backend on any changes

2. To run the frontend development server, go to `./modules/frontend` and run `npm run dev` there. In a separate SBT shell run `~frontend/fastLinkJS` to continuously rebuild the frontend.
   Go to http://localhost:5173 (or whatever port Vite chooses) to see the full app running

## Features

- Database
  - uses Skunk for database access and [Dumbo](https://github.com/rolang/dumbo) to apply migrations

- Deployment
  - [sbt-native-packager](https://sbt-native-packager.readthedocs.io/en/latest/)
    to bundle a JVM app into something runnable

  - Vite and NPM package the Scala.js frontend

  - NGINX is used to serve both the static assets and the backend API

- Testing

   - **stub** tests that use in-memory database implementation and don't exercise
     the network or SQL queries

   - **integration** tests that start a PostgreSQL container using [Testcontainers](https://www.testcontainers.org),
     migrate a fresh copy of the database, start the actual HTTP server and run the tests
     through that

  - **frontend** tests that use Playwright to launch the browser and
    execute some basic tests to make sure the frontend works correctly
