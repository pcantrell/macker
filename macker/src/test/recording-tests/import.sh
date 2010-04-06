#!/bin/bash

rulesfile="$1"
srcdir="$2"

echo "<?xml version=\"1.0\"?>"
echo
echo "<macker-ruleset-test>"
echo ""
echo "    <rules-file>"
cat "$rulesfile" | perl -ne 's/^/    /; s/<\?.*\?>//; s/<\/?macker>//; print if $_'
echo "    </rules-file>"
echo "        "
echo "    <expected-events>"
echo "    </expected-events>"
echo "    "
echo "    <test-classes>"
cd "$srcdir"
for f in $(find . -name '*.java'); do
    class="${f##*/}"
    class="${class%.java}"
    pack="$(echo ${f%/*} | perl -pe 's/\//./g; s/^\.+//')"
    echo "        <source package=\"$pack\" class=\"$class\">"
    echo "          <![CDATA["
    cat "$f" | perl -pe 's/^/            /'
    echo
    echo "          ]]>"
    echo "        </source>"
done
echo "    </test-classes>"
echo "    "
echo "</macker-ruleset-test>"
