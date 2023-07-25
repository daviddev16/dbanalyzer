# db-analyzer

o db-analyzer foi projetado para analisar e encontrar o caractere hexdecimal inválido informado através do EurekaLog. Facilitando a
identificação do problema e localizando a tabela em que se encontra o caractere, economizando tempo na pesquisa e no atendimento. O 
problema geralmente ocorre quando o sistema tenta converter/mapear um caractere de 2 bytes (UTF-8) em um caractere de 1 byte (ISO-8859-1, pré-definido na 
execução da aplicação no Windows). Teóricamente, o sistema deveria (mesmo que ficassem diferentes) mapear os caracteres para o ISO-8859-1,
como no exemplo abaixo.

## Delphi

Fiz alguns testes no Delphi 11, não pude reproduzir o erro utilizando `TEncoding`. A aplicação fez o mapeamento correto, quando não é
possível mostrar o caractere utf-8, ele retorna a representação de cada byte de outra forma, como esperado. ([UTF8-Test](https://github.com/daviddev16/dbanalyzer/tree/master/Delphi/UTF8-Test))


```pascal
begin
  ContentText := TFile.ReadAllText('C:\Users\David\Desktop\0002\utf8.txt', TEncoding.Default);
  Encoding := TEncoding.Default;
  Memo1.Lines.Clear();
  Memo1.Lines.Add('Utf-8: ' + TEncoding.UTF8.GetString(Encoding.GetBytes(ContentText));
  Encoding := TEncoding.ANSI;
  Memo1.Lines.Add('(chars do erro) -> ' + Encoding.GetString(Encoding.GetBytes(ContentText)));
end;
```


![Teste](https://i.imgur.com/T88JTsM.png)


# Como utilizar o db-analyzer

```

-=-=-=- PARAMETROS DE UTILIZAÇÃO -=-=-=-=-

--host "<host>"               : IP/HOSTNAME DO SERVIDOR
--port "<port>"               : PORTA POSTGRESQL
--username "<username>"       : USUÁRIO DE ACESSO AO BANCO DE DADOS
--password "<password>"       : SENHA DE ACESSO AO BANCO DE DADOS
--database "<database>"       : NOME DO BANCO DE DADOS PARA ANALISE
--characters "<c0>,<c1>,..."  : CARACTERES ESPECIAIS PARA A LOCALIZAÇÃO
--filter "<filter_name:text>" : FILTRO DE TABELA, UTILIZADO PARA QUANDO O PROBLEMA FOR EM UMA TABELA ESPECIFICA OU CONHECIDA.
--only-ctid                   : NÃO APRESENTA O CONTEUDO DA LINHA COM O PROBLEMA


-=-=-=-=-=- EXEMPLO DE FILTROS -=-=-=-=-=-=-

# ANALISA APENAS A TABELA "wshop.produto"

--filter "IGUAL:wshop.produto"

# ANALISA TODAS AS TABELAS QUE CONTEM A PALAVRA "produto"

--filter "wshop.produto"

# PODE SER USADO MAIS DE UM PARAMETRO DE FILTRO
# SEM FILTRO VAI ANALISAR TODAS AS TABELAS

-=-=-=-=- EXEMPLO DE CARACTERES -=-=-=-=-=-

# APENAS 1:
--characters "0xC5"

# MAIS DE UM:
--characters "0xC5,0x4C,0xD8"

-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-
```
