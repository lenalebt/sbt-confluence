# sbt-confluence

This is an sbt plugin to publish docs to a confluence installation. You will
need this macro installed in your confluence installation:
https://marketplace.atlassian.com/plugins/com.atlassian.plugins.confluence.markdown.confluence-markdown-macro/server/overview

See folder test-project for an example usage. Main settings you need are:

* confluencePages
* confluenceSettings

Type `confluencePublish` to publish your docs to the configured location.

Dont expect too much, this was a weekend hack and my first SBT plugin ;). It has not been
published so far on any central server.
