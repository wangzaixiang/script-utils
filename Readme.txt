
Some utils for text process

1.h2 function "textextract"
 usage:
 create alias textextract for "wangzx.script.TextExtract.textExtract";
 select * from textextract('./test/filename.txt', 
 	$$(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})$$
 )
 the function maps the text file as a table using a regular-expression with named group

2.


