# MSSQL-initial-data-synchronization
透過SQL Query對SQL server進行欄位比對，依照時間排序，將遠端磁碟資料載入到自己的磁碟中
## 環境配置
1. Windows 11
2. Project SDK 17 (Eclipse Temurin version 17.0.5)
3. 利用java程式像是client端與sql server建立操作，要先在File->Project Structure->Modules->Dependencies->加入JARs導入兩個jar檔 :  
![](https://i.imgur.com/iO9IfRZ.png)
## Table 創建/調整欄位/清空全部欄位
```SQL
USE FISHDB
GO

CREATE TABLE FISH.TestTable
(
	CRMYY nvarchar(6) NULL,
	CRMID nvarchar(20) NOT NULL,
	CRMNO nvarchar(6) NOT NULL,
	CHKNO nvarchar(6) NOT NULL,
	ORGNO nvarchar(6) NULL,
	FILENM nvarchar(60) NULL
)
```
```SQL
ALTER TABLE FISH.SCRIPT_ATT ALTER COLUMN CRMYY VARCHAR(6) NOT NULL;
```
```SQL
TRUNCATE TABLE FISH.SCRIPT
```
## Table欄位測試資料建立
1. SQL_Algorithm1 包含:
* TestTableinsert input :   
>10.0.2.15 1433 FISHDB michelin itricuju    
* R01insert input :    
>10.0.2.15 1433 FISHDB michelin itricuju    
2. SQL_Algorithm2 包含:
* SCRIPTinsert input :   
>10.0.2.15 1433 FISHDB michelin itricuju    
* SCRIPT_ATTinsert input :    
>10.0.2.15 1433 FISHDB michelin itricuju   
3. SQL_Algorithm2 :  
## Input範例
1. SQL_Algorithm1 input example :    
>10.0.2.15 1433 FISHDB michelin itricuju FISH TestTable C E fs  
2. SQL_Algorithm2 input example :    
>10.0.2.15 1433 FISHDB michelin itricuju FISH SCRIPT C E fs  
>10.0.2.15 1433 FISHDB michelin itricuju FISH SCRIPT_ATT C E fs  
3. SQL_Algorithm3 input example :   
>C:\fs\nw\FISH\alg3 E   
4. SQL_Algorithm4 input example :   
>C:\fs\nw\FISH E  

