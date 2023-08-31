# Clojurist Together Update 1

#### 2023-07-15

## TL;DR

It was a little busy lately so my update is a bit uneventful.
But my summer holidays are coming and I hope to spend some quality time working on making the Lucene library a little easier to use for the fellow Clojurists.

## Context

The goal of the project is to tear apart `lucene-grep` project into a bunch of libraries.
`lucene-grep` is a CLI app that was created to scratch an itch of making Lucene to be compiled by GraalVM native image during the COVID lockdowns.
Due to the nature of the effort the code was not designed to be used elsewhere.
By participating in the Clojurist Together I want to refactor and re-design the existing codebase for reuse.

## Updates

As of now, out of the `lucene-grep` 3 libraries are extracted:
- [lucene-custom-analyzer](https://github.com/dainiusjocas/lucene-custom-analyzer): data-driven Lucene Analyzers; 
- [lucene-query-parsing](https://github.com/dainiusjocas/lucene-query-parsing): data-driven Lucene Query Parsers;
- [lucene-text-analysis](https://github.com/dainiusjocas/lucene-text-analysis): helpers to play with the Lucene Analyzers.

All these libraries were updated to depend on the newest Lucene version.
I hope to write proper blog posts and/or demo apps with potential uses of the libraries.

## Other things I've worked on

I've been having fun with other hacks.

### The new GraalVM

I've upgrade `lucene-grep` to be compiled with the new [GraalVM](https://github.com/dainiusjocas/lucene-grep/commit/7343db1413ef5d3c1c89547b091a1bdb7c5d2fe2).
The biggest adventure there was that GraalVM changed the way how compile time environment variables are used.
Now, the env vars should be passed with `-E` [property](https://www.graalvm.org/latest/reference-manual/native-image/overview/BuildOptions/#non-standard-options) e.g. `-ELMGREP_FEATURE_STEMPEL=true`.

### Clerk based publishing 

I've set up a publishing system based on [Clerk](https://github.com/nextjournal/clerk) and Github Pages: [journal](https://www.jocas.lt/journal/), [source](https://github.com/dainiusjocas/journal).
Hopefully, in the Journal I'll publish proper writeups and presentations of the work I'm about to do in this project.

## What is next?

In the coming months I want to design a Clojure library to use the Lucene Monitor Library.

## That is it!

---

P.S. A huge shout-out for [Clojurist Together](https://www.clojuriststogether.org) for sponsoring my open source work!
