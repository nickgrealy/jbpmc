# JBPMC - *A Java BPM Client API*
---

The `jbpmc-api` library is an API for the Java programming language, that defines how a client may access a BPM server. 
It provides methods for programmatically executing a BPM server's runtime, configuration and deployment services.

---

## Table of contents

1. [Why have a common API?](#why-have-a-common-api)
1. [What can I use this library for?](#what-can-i-use-this-library-for)
1. [Want to contribute?](#want-to-contribute)
1. [Libraries](#libraries)

---

## Why have a common API?

For the same reasons you have [standards](http://www.standards.org.au/StandardsDevelopment/What_is_a_Standard/Pages/Benefits-of-Standards.aspx).
Currently each BPM server implementation provides a bespoke API for accessing 
their services. Our aim is to provide a consistent interface, to facilitate 
building tools that are agnostic of the underlying BPM implementation. 

---

## What can I use this library for?
 
Here are some potential applications of this library:

- deployment automation
- testing automation
- production support
- creating generic bpm clients
- server migration/upgrades
- process instance migration

---

## Want to contribute?

This project is just in it's infancy, so we're looking to get it off the ground in terms of features and support. 
If you see value in this project or just have some features that you want to implement/share with everyone else, 
please contact us to become a contributor, and/or [fork the project](https://help.github.com/articles/fork-a-repo/) 
and [submit a pull request](https://help.github.com/articles/creating-a-pull-request/).

---

## Libraries

- `jbmpc-api` - An API for accessing a BPM server.

### Library Implementations

- `jbpmc-api`
    - `jbpmc-ibm`
        - `7.5.1.2` [ runtime (partial) | <s>configuration</s> | deployment (partial) ]
    - <s>`jbpmc-activiti`</s>
    - <s>`jbpmc-appian`</s>
    - <s>`jbpmc-camunda`</s>
    - <s>`jbpmc-jbpm`</s>
    - <s>`jbpmc-pega`</s>

---

## TODO

- Setup "runtime" project to test dependencies (i.e. removing groovy after compilation)
- Document Example Usage (with @Grape('...')) or libs?
- Document Example Config
- Document Example wsadmin folder structure on windows
- Add project to OSS Sonatype repo
- Setup main method application?

---