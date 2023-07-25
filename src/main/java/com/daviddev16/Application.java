package com.daviddev16;

import org.apache.commons.cli.*;

public class Application {

    private static final String HOST_OPT = "h";
    private static final String PORT_OPT = "p";
    private static final String PASSWORD_OPT = "a";
    private static final String USERNAME_OPT = "u";
    private static final String CHARACTERS_OPT = "c";
    private static final String FILTER_OPT = "f";
    private static final String DATABASE_OPT = "d";
    private static final String ONLY_CTID_OPT = "y";


    public static void main(String[] args) {

        //args = new String[] {"--host", "192.168.1.251", "--port", "5432", "--username", "postgres",
        //        "--password", "#abc123#", "--database", "SHOPDB12", "--characters", "0xC5", "--filter", "produto", "--only-ctid"};

        final Options options = new Options();
        options.addRequiredOption(HOST_OPT, "host", true, "Hostname/IP Servidor");
        options.addRequiredOption(PORT_OPT, "port", true, "Porta PostgreSQL");
        options.addRequiredOption(PASSWORD_OPT, "password", true, "Senha do usuário");
        options.addRequiredOption(USERNAME_OPT, "username", true, "Nome de usuário");
        options.addRequiredOption(CHARACTERS_OPT, "characters", true, "Caracteres inválidos.");
        options.addRequiredOption(DATABASE_OPT, "database", true, "Banco de dados para analise");
        options.addOption(FILTER_OPT, "filter", true, "Filtro de tabelas");
        options.addOption(ONLY_CTID_OPT, "only-ctid", false, "Apresenta apenas o ctid ao invés do conteudo todo da linha");
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            final String[] filters = commandLine.getOptionValues(FILTER_OPT);
            final String[] characters = getMultipleValues(commandLine.getOptionValue(CHARACTERS_OPT));
            final String host = commandLine.getOptionValue(HOST_OPT);
            final String port = commandLine.getOptionValue(PORT_OPT);
            final String username = commandLine.getOptionValue(USERNAME_OPT);
            final String password = commandLine.getOptionValue(PASSWORD_OPT);
            final String database = commandLine.getOptionValue(DATABASE_OPT);
            final boolean onlyCtid = commandLine.hasOption(ONLY_CTID_OPT);

            PostgreSQLInstance postgreSQLInstance = new PostgreSQLInstance(host, port, username, password, database);

            new DatabaseAnalyzer(postgreSQLInstance,filters,
                    convertToDecimals(characters), onlyCtid)
                    .analyze();

            synchronized (DatabaseAnalyzer.class) {
                postgreSQLInstance.getConnection().close();
            }

        } catch (ParseException e) {
            new HelpFormatter().printHelp("dbanalyzer", options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int[] convertToDecimals(String[] hexadecimalCodes) {
        int[] decimals = new int[hexadecimalCodes.length];
        for (int i = 0; i < decimals.length; i++) {
            decimals[i] = Integer.parseInt(hexadecimalCodes[i].substring(2), 16);
        }
        return decimals;
    }

    public static String[] getMultipleValues(String value) {
        if (value == null) {
            return null;
        }
        value = value.replaceAll("\\s+", "");
        String[] slices = value.split(",");
        if (slices.length == 0) {
            return new String[]{value};
        }
        return slices;
    }

}
