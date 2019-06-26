==========================
Frequently Asked Questions
==========================

Additional TODO
===============

-   Placeholder for needed changes to this document. In future it may be useful to organize the questions in sections.


Introduction
============

NLNZ Tools SIP Generation has many useful classes. This document aims to answer any frequently asked questions regarding
the classes provided.

Contents of this document
-------------------------

Following this introduction, the FAQ covers each issue in a question and answer format.

Index of Questions
==================

-   `Q: Why does thumbnail page generation take so long?`_

Questions
=========

Why does thumbnail page generation take so long?

Q: Why does thumbnail page generation take so long?
---------------------------------------------------

A: Thumbnail page generation takes so long (and is memory intensive) because it is reading in the given pdfs,
writing them to thumbnails using interpolation, and then finally writing all the thumbnails to a thumbnail page.
Depending on the size and complexity of the PDF this can take a lot of processing and memory overhead. One other option
is to move that processing to a third-party tool, but that would still take significant overhead (and the processing
would lose the customisation possible with in-house code).
