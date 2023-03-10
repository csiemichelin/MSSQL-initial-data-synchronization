# MSSQL-initial-data-synchronization
透過SQL Query對SQL server進行欄位比對，依照時間排序，將遠端磁碟資料載入到自己的磁碟中
## 環境配置
1. Windows 11
2. Project SDK 17 (Eclipse Temurin version 17.0.5)
## Table 創建/調整欄位/清空全部欄位
1. 
2. 
3. 
ALTER TABLE FISH.SCRIPT_ATT ALTER COLUMN CRMYY VARCHAR(6) NOT NULL;
## Input範例
1. Algorithm1 input example :    
>10.0.2.15 1433 FISHDB michelin itricuju FISH TestTable C E fs  
2. Algorithm2 input example :    
>10.0.2.15 1433 FISHDB michelin itricuju FISH SCRIPT C E fs  
>10.0.2.15 1433 FISHDB michelin itricuju FISH SCRIPT_ATT C E fs  
3. Algorithm3 input example :   
>C:\fs\nw\FISH\alg3 E   
4. XXXinsert input example :     
>10.0.2.15 1433 FISHDB michelin itricuju  

