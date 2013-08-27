#!/bin/sh -e

<% postrmCommands.each {command -> %><%= command %>
<% } %>
