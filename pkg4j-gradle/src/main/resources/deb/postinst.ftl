#!/bin/sh -e

ec() {
    echo "\$@" >&2
    "\$@"
}

case "\$1" in
    configure)
        <% dirs.each{ dir -> %>
        ec install -o <%= dir['owner'] %> -d <%= dir['name'] %>
        <% } %>

        <% postinstCommands.each {command -> %>
        ec <%= command %>
        <% } %>
        ;;
esac
