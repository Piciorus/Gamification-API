
zgrep -i "SendTechnicalEmail" * | grep "<ns2:data>" | sed -n 's/.*<ns2:data>\(.*\)<\/ns2:data>.*/\1/p' | head -n 1
