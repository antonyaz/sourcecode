SELECT c1, row_num FROM (SELECT c1, row_number() OVER ( PARTITION BY c2 ORDER BY c1 ASC nulls last ) row_num FROM "tblWnulls.parquet") sub_query WHERE row_num IS NOT null AND c1 IS NOT null