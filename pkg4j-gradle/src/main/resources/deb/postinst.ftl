#!/bin/sh
# postinst script for ${name}
#
# see: dh_installdeb(1)

set -e

# summary of how this script can be called:
#        * <postinst> `configure' <most-recently-configured-version>
#        * <old-postinst> `abort-upgrade' <new version>
#        * <conflictor's-postinst> `abort-remove' `in-favour' <package>
#          <new-version>
#        * <postinst> `abort-remove'
#        * <deconfigured's-postinst> `abort-deconfigure' `in-favour'
#          <failed-install-package> <version> `removing'
#          <conflicting-package> <version>
# for details, see http://www.debian.org/doc/debian-policy/ or
# the debian-policy package


case "\$1" in
    configure)
        <% dirs.each{ dir -> %>
        <% if (dir['owner']) { %>
        install -o <%= dir['owner'] %> -d <%= dir['name'] %>
        <% } else { %>
        install -d <%= dir['name'] %>
        <% } %>
        <% } %>

        <% postinstCommands.each {command -> %><%= command %>
        <% } %>
    ;;

    abort-upgrade|abort-remove|abort-deconfigure)
    ;;

    *)
        echo "postinst called with unknown argument \\`\$1'" >&2
        exit 1
    ;;
esac

exit 0
