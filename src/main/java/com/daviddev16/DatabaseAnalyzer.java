package com.daviddev16;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;

public class DatabaseAnalyzer {

    private static final String SQL_SELECT_ALL_TABLE = "SELECT ctid,* FROM %s";

    private static final String SQL_SELECT_ALL_TABLES_FROM_DATABASE = "SELECT (table_schema || '.' || table_name) AS fullname " +
            "FROM information_schema.tables WHERE NOT table_schema = 'pg_catalog' AND NOT table_type = 'VIEW' AND table_catalog = '%s'";

    private final Stack<String> tablesStack = new Stack<>();
    private final List<RowData> occurrences = new ArrayList<>(10);

    private final PostgreSQLInstance postgreSQLInstance;
    private final int[] characterCodes;
    private final String[] filters;
    private final boolean onlyCtid;

    public DatabaseAnalyzer(PostgreSQLInstance postgreSQLInstance, String[] filters, int[] characterCodes, boolean onlyCtid) {
        this.postgreSQLInstance = postgreSQLInstance;
        this.characterCodes = characterCodes;
        this.filters = filters;
        this.onlyCtid = onlyCtid;
    }

    public void analyze() throws SQLException, InterruptedException {
        System.out.println("\n\n-=-=-=-=-=-=-=-=-= INICIO DE ANALISE -=-=-=-=-=-=-=-=-=-=-\n\n");
        extractTables(tablesStack, filters);
        int globalOccurrences = 0;
        while (!tablesStack.isEmpty()) {

            String schemaTableName = tablesStack.pop();

            if (schemaTableName.contains("modulo_"))
                continue;

            Statement statement = getPostgreSQLInstance().getConnection().createStatement();
            ResultSet result = statement.executeQuery(String.format(SQL_SELECT_ALL_TABLE, schemaTableName));
            ResultSetMetaData metaData = result.getMetaData();
            int rowOccurrences = 0;
            while (result.next())
            {
                rowOccurrences = 0;
                RowData rowData = new RowData();
                rowData.setTableName(schemaTableName);
                StringJoiner rowDataJoiner = new StringJoiner(", ");
                for (int columnIndex = 0; columnIndex < metaData.getColumnCount(); columnIndex++) {
                    String rowContentData = result.getString(columnIndex + 1);
                    if (columnIndex == 0) {
                        rowData.setCtid(rowContentData);
                    } else {
                        rowDataJoiner.add(rowContentData);
                    }
                    rowOccurrences += processRowData(rowContentData);
                }
                rowData.setContent(rowDataJoiner.toString());
                rowData.setOccurrences(rowOccurrences);
                if (rowOccurrences > 0) {
                    occurrences.add(rowData);
                }
                globalOccurrences += rowOccurrences;
            }
            System.out.println(String.format("-> '%s' teve %d ocorrências dos caracteres" +
                    " especiais informados.", schemaTableName, rowOccurrences));
            result.close();
            statement.close();
            Thread.sleep(5);
        }
        System.out.println(String.format("\n\n-> No geral, o banco '%s' teve %d ocorrências dos caracteres" +
                " especiais informados.\n\n", getPostgreSQLInstance().getDatabase(), globalOccurrences));

        if (globalOccurrences > 0) {
            System.out.println("-=-=-=-=-=-=-=-=-= RESUMO DE OCORRÊNCIAS -=-=-=-=-=-=-=-=-=-=-\n");
            for (RowData rowData : occurrences) {
                System.out.println("--------------------------------------------------------------");
                System.out.println("  - Tabela: " + rowData.getTableName());
                System.out.println("    | Ocorrências: " + rowData.getOccurrences());
                System.out.println("    | DQL: " + locateByCtid(rowData.getTableName(), rowData.getCtid()));
                System.out.println("    | ctid: " + rowData.getCtid());
                if (!onlyCtid) {
                    System.out.println("    | Dados: " + rowData.getContent());
                }
            }
            System.out.println("--------------------------------------------------------------\n");
            System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n\n");
        }

        System.out.println("-=-=-=-=-=-=-=-=-= FIM DE ANALISE -=-=-=-=-=-=-=-=-=-=-=");
        System.out.println(" !  EM CASO DE ERRO, CONTATE DAVID.SUP.SHOP, OBRIGADO  !");
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n");
    }

    private static String locateByCtid(String tableSchema, String ctid) {
        return String.format("SELECT * FROM %s WHERE ctid='%s'", tableSchema, ctid);
    }

    private int processRowData(String rowData) {

        if (rowData == null || rowData.isEmpty())
            return 0;

        int localCount = 0;

        String newRowData = new String(rowData.getBytes(), StandardCharsets.ISO_8859_1);
        for (int charIndex = 0; charIndex < newRowData.length(); charIndex++) {
            for (int decimalCharCodes : characterCodes) {
                if (decimalCharCodes == ((int)newRowData.charAt(charIndex)))
                    localCount++;
            }
        }
        return localCount;
    }


    public synchronized void extractTables(Stack<String> tablesStack, String[] filter) throws SQLException {
        Statement statement = getPostgreSQLInstance().getConnection().createStatement();
        ResultSet result = statement.executeQuery(String.format(SQL_SELECT_ALL_TABLES_FROM_DATABASE,
                getPostgreSQLInstance().getDatabase()));
        while (result.next())
        {
            String tableFullname = result.getString(1);
            if (filter == null || approve(tableFullname, filter)) {
                tablesStack.push(tableFullname);
            }
        }
        result.close();
        statement.close();
    }


    private boolean approve(String tableName, String[] filters) {
        for (String filter : filters) {
            if (checkFilter(tableName, filter))
                return true;
        }
        return false;
    }

    private boolean checkFilter(String tableName, String filter) {
        if (filter.startsWith("IGUAL:")) {
            return tableName.equalsIgnoreCase(filter.split(":")[1].trim());
        }
        return tableName.contains(filter);
    }

    public PostgreSQLInstance getPostgreSQLInstance() {
        return postgreSQLInstance;
    }


}
