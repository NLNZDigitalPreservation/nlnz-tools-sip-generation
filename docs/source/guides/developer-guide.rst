===============
Developer Guide
===============


Introduction
============

This guide, designed for a NLNZ Tools SIP Generation developer and contributor, covers how to develop and contribute to
the NLNZ Tools SIP Generation. The source for both code and documentation can be found at:
https://github.com/NLNZDigitalPreservation/nlnz-tools-sip-generation/

Contents of this document
-------------------------

Following this introduction, the NLNZ Tools SIP Generation includes the following sections:

-   **Contributing** - Covers how to contribute to the project.

-   **Basic packages and classes**  - Covers the packages and classes in the project.

-   **Building** - Covers building the nlnz-tools-sip-generation jars from source.

-   **Developer guidelines** - Covers coding practice and development workflow.

-   **Future milestones** - Covers plans for future development.


Contributing
============

This describes how to contribute to the NLNZ Tools SIP Generation project. General contribution guidelines follow the
guidelines outlined in *Contributing* section of the *Developer Guide* of the
*National Library of New Zealand Developer Guidelines* for a description of the build commands used for this project.
These guidelines can be found at https://nlnz-developer-guidelines.readthedocs.io .

Source Code Repository
----------------------

Source code for the NLNZ Tools SIP Generation is stored in github at:
https://github.com/NLNZDigitalPreservation/nlnz-tools-sip-generation/
Contributors to the codebase will require a github account.

Major Contributors
------------------

Major contributors to NLNZ Tools SIP Generation are NLNZ (The National Library of New Zealand)
(https://natlib.govt.nz/). This institution currently drive most development. All contributors are welcome. Making your
interest in NLNZ Tools SIP Generation known can help to ensure that the tools meets your needs.


Basic packages and classes
==========================

TODO a diagram illustrates the interactions between key classes.


Building
========

Requirements
------------

Build requirements
~~~~~~~~~~~~~~~~~~
Building the NLNZ Tools SIP Generation from source requires the following:

-   Java 11 JDK or above (64bit recommended). Current development assumes the use of OpenJDK.

-   Gradle 5.2.1 or later.

-   Groovy 2.5.4 or later.

-   Git (required to clone the project source from Github).

-   Access to maven central either directly or through a proxy.

As the artifact targets are Java-based, it should be possible to build the artifacts on either Linux, Solaris or Windows
targets.

Development platforms
~~~~~~~~~~~~~~~~~~~~~
The following platforms have been used during the development of the NLNZ Tools Sip Generation:

-  Ubuntu GNU/Linux 18.04 LTS and later


Installation
------------
The artifacts are built using gradle and will deploy to a maven repository when various gradle publishing options are
used.

Build commands
--------------

See the *Build commands for Gradle-based projects* section of the *Java Development Guide* of the
*National Library of New Zealand Developer Guidelines* for a description of the build commands used for this project.
These guidelines can be found at https://nlnz-developer-guidelines.readthedocs.io .


Developer Guidelines
====================

See the *National Library of New Zealand Developer Guidelines* found at:
https://nlnz-developer-guidelines.readthedocs.io .


Future milestones
=================

This sections discusses plans for future development.

TODO Discuss plans for future development.
