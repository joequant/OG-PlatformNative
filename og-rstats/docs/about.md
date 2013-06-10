[TOC]

This directory contains source for docs relating to the OG-RStats package.

These docs are designed to be run using Dexy http://dexy.it

### R Package Reference Documentation

R has its own built-in system for generating reference documentation. A traditional PDF document containing reference documentation for the R package can be generated via:

{{ d['scripts/generate-traditional-rdocs.sh|idio']['run-rd2pdf'] }}

Or via ant:

{{ d['scripts/generate-traditional-rdocs.sh|idio']['ant'] }}

### Customized R Package Reference Documentation

In order to be able to have more control over the appearance of documentation and to incorporate reference information in other types of documentation, we also process the standard R info into a database which we can access later.have more control over the appearance of documentation and to incorporate reference information in other types of documentation, we also process the standard R info into a database which we can access later.

{{ d['rdoc.R|pyg'] }}

Here are a few of the available entities:

{% set total_number_of_entries = len(d['rdoc.R|rdoc'].storage().keys()) -%}

{% for i, k in enumerate(sorted(d['rdoc.R|rdoc'].storage().keys())) -%}
{% if i < 10 -%}
+ {{ k }}
<pre>
{{ d['rdoc.R|rdoc'][k] }}
</pre>
{% endif -%}
{% endfor -%}

<br />

There are a total of {{ total_number_of_entries }} entries in the database. You can search for keys within the database by using the `dexy grep` command, for example:

{{ d['scripts/run-dexy-grep.sh|shint|pyg'] }}

Note that the `dexy grep` command only works after Dexy has been run once (it greps in the results of previous runs).
