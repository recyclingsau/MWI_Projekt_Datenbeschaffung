Diese Views werden im Datenbeschaffungsprogramm eingelesen und als SQL-Statements an die DB geschickt.

Die Namen des Ordners und der .sql-Files dürfen NICHT verändert werden, da diese statisch im Quellcode verankert sind.

Inhalte der .sql-Files können natürlich verändert werden, dafür sind diese Dateien ja ausgelagert. 
Bitte hier darauf achten, dass man in Kommentaren keine ";" schreibt. 
Die Queries werden nämlich nach diesen Zeichen gesplittet und einzeln abgearbeitet.