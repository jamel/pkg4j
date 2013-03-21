#!/bin/sh -e

ec() {
    echo "\$@" >&2
    "\$@"
}

<% prermCommands.each {command -> %>
ec <%= command %>
<% } %>
