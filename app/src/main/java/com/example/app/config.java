package com.example.app;

public class config {
    public static String ADDRESS = "192.168.171.89";
    public static String DATABASE="test";
    public static String TABLENAME="uzytkownicy";
    public static String DBUSER="API";
    public static String DB_PASS=")Xcm*.H2OHn*THJl";


    public static String API_GETDATA_URL="http://"+ADDRESS+"/getData.php";
    public static String API_INSERTDATA_URL="http://"+ADDRESS+"/insertData.php";
    public static String API_LOGIN_URL="http://"+ADDRESS+"/login.php";
    public static String API_REGISTER_URL="http://"+ADDRESS+"/register.php";
    public static String API_CREATETABLE_URL="http://"+ADDRESS+"/createTable.php";
    public static String API_GETTABLES_URL="http://"+ADDRESS+"/getTables.php";
    public static String API_MANAGECOLUMNS_URL="http://"+ADDRESS+"/editTable.php";
    public static String API_EXECUTE_SQL_URL="http://"+ADDRESS+"/executeSQL.php";
}
