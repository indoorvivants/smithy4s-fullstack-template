# Fullstack Scala application with Smithy4s

This is a full-stack Scala application (simple key-value management store), made with the following excellent libraries and technologies:

1. [Scala 3](https://docs.scala-lang.org/scala3/new-in-scala3.html)
2. [Smithy4s](https://disneystreaming.github.io/smithy4s/) and [Smithy](https://awslabs.github.io/smithy/2.0/index.html#) for the API and protocol 
   definition between backend and frontend (and in tests)
3. [Scala.js](https://www.scala-js.org) and [Laminar](https://laminar.dev) for the [SPA](https://en.wikipedia.org/wiki/Single-page_application) frontend
4. [PostgreSQL](https://www.postgresql.org) and [Skunk](https://github.com/typelevel/skunk) for database access 
5. [Weaver-Test](https://disneystreaming.github.io/weaver-test/) for unit and integration testing
6. [Playwright](https://playwright.dev/) tests for frontend testing, using the dedicated [Weaver integration](https://github.com/indoorvivants/weaver-playwright)

The entire app is auto-deployed to [Fly.io](https://fly.io), you can see the **live version [here](https://smithy4s-fullstack-template.fly.dev)**

It is a dialed down version of the [Jobby app](https://jobby-smithy4s.herokuapp.com) I've built in my [4-part blogpost series](https://blog.indoorvivants.com/2022-06-10-smithy4s-fullstack-part-1).
The blog series should answer a lot of the design questions you might have in much 
greater detail than the margins of this tome allow.

![2022-08-22 17 21 03](https://user-images.githubusercontent.com/1052965/185972992-fb49f348-a33d-4e1a-aafa-7f9a01d3a9c1.gif)


## What is it for?

I'm building this template mainly for myself - there's a whole menagerie of borderline
idiotic services I want to create at various points in time.

Having a non-trivial amount of glue code pre-written should let me deploy it very quickly.

Additionally, if you are interested in full-stack Scala development, you might be interested 
in this template.

Bear in mind, that everything about this is styled to my liking - but you don't have to 
do it the same way! Once you cloned the template, the copy of the code is yours - be 
as ruthless and as pragmatic as you want about ripping out my precious code weeds.

That said, if your quarrel with templated code is of functional, rather than stylistical nature - 
i.e. _"stuff doesn't work"_ vs. _"I hate how you structure your code"_ - then please open an issue and let's make it better!

## Running 

While most of the defaults are already configured, the only missing bit is the Postgres communication.
It can be picked up from the environment, but best way to do it would be to put a file named `env.opts`
at the root of the project with a JDBC-compatible Postgres URL:

```
DATABASE_URL=postgres://postgres:mysecretpassword@localhost:5432/postgres
```

After that, just do `sbt app/reStart` and go to http://localhost:9000

## Features

- Database 
  - uses Skunk for database access and [Flyway](https://flywaydb.org) to apply migrations

- Deployment
  - uses [sbt-native-packager](https://sbt-native-packager.readthedocs.io/en/latest/) 
    to build a Docker container 

  - CI pushes the Docker container to Fly.io

- Testing

   - **stub** tests that use in-memory database implementation and don't exercise 
     the network or SQL queries

   - **integration** tests that start a PostgreSQL container using [Testcontainers](https://www.testcontainers.org),
     migrate a fresh copy of the database, start the actual HTTP server and run the tests 
     through that
  
   - both types of tests use exactly the same specifications, increasing code reuse

