# JBPMC - *A Java BPM Client API*
---

The `jbpmc-api` library is an API for the Java programming language, that defines how a client may access a BPM server. 
It provides methods for programmatically executing a BPM server's runtime, configuration and deployment services.

## Why have a common API?

For the same reasons you have [standards](http://www.standards.org.au/StandardsDevelopment/What_is_a_Standard/Pages/Benefits-of-Standards.aspx).
Currently each BPM server implementation provides a bespoke API for accessing 
their services. Our aim is to provide a consistent interface, to facilitate 
building tools that are agnostic of the underlying BPM implementation. 

## What can I use this library for?
 
Here are some potential applications of this library:

- deployment automation
- testing automation
- production support
- creating generic bpm clients
- server migration/upgrades
- process instance migration

---

## Libraries

- `jbmpc-api` - An API for accessing a BPM server.

### The following libraries are (Groovy) Implementations of the `jbpmc-api` library:

- `jbpmc-api`
    - `jbpmc-ibm`
        - `7.5.1.2` [ runtime (partial) | <s>configuration</s> | <s>deployment</s> ]
    - <s>`jbpmc-activiti`</s>
    - <s>`jbpmc-appian`</s>
    - <s>`jbpmc-camunda`</s>
    - <s>`jbpmc-jbpm`</s>
    - <s>`jbpmc-pega`</s>

---
