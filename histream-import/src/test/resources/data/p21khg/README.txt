Example dataset for data transfer following 
the German paragraph 21 KHEntgG.

Data is available at
https://www.g-drg.de/Datenlieferung_gem._21_KHEntgG/Dokumente_zur_Datenlieferung

Slight modifications (until the sorting/filtering
functionality is implemented in code):

- Tables OPS.csv, ICD.csv, Entgelte.csv are sorted 
  by "KH-internes-Kennzeichen" 

- File _PAT.csv is added and contains distinct
  patients from FALL.csv (distinct "Patientennummer")
  