- The command curl --data @<message_filename> "http://<server>:<port>/message  DOES NOT preserve newline character.
Therefore, the received message is in one line, which is impossible to parse

- There are two solution to this: 
	1. in the file that contains the message ( message.txt ), explicitly specify newline character by "\n"
	2. use "curl --data-binary" instead of "curl --data", this command sends data verbatim, thus preserving new line character

SOLUTION 2 IS CHOSEN IN MY PROJECT. Therefore, please use "curl --data-binary @<message_filename> http://<server>:<port>/message" 
for POST message requests !
