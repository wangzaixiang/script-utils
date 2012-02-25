
Some utils for text process

1.h2 function "textextract"
 usage:
 create alias textextract for "wangzx.script.TextExtract.textExtract";
 select * from textextract('./test/yychanloginfalse_201202242000', 
 $$(?<ip>.*?),(?<code1>.*?),(<?code2>.*?),act=(?<act>.*?)&uid=(?<uid>.*?)&sid=(?<sid>.*?)&sdt=(?<sdt>.*?)&ver=(?<ver>.*?)&key=(?<key>.*?)&time=(?<time>.*?)&ylog=(?<ylog>.*?),.*$$
 )
 the function maps the text file as a table using a regular-expression with named group

2.


