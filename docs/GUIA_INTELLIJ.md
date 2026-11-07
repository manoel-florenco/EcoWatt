# Guia para executar o EcoWatt no IntelliJ IDEA

Este guia serve para qualquer integrante da equipe abrir e executar o projeto em outro computador.

## 1. Instalar os programas necessarios

- IntelliJ IDEA Community ou Ultimate;
- JDK 21;
- Git, caso o projeto seja baixado com `git clone`.

Não é necessário instalar Maven nem banco de dados. O IntelliJ inclui suporte ao Maven e o projeto usa o banco H2 embarcado.

## 2. Baixar o projeto

Há duas opções:

1. no GitHub, clicar em **Code > Download ZIP**, extrair o arquivo e guardar a pasta em um local de fácil acesso; ou
2. usar **Get from VCS** no IntelliJ, colar o endereço do repositório e clicar em **Clone**.

## 3. Abrir no IntelliJ

1. Na tela inicial, clicar em **Open**.
2. Selecionar a pasta `EcoWatt`, que contém o arquivo `pom.xml`.
3. Confirmar **Trust Project**, se essa mensagem aparecer.
4. Aguardar o IntelliJ importar e baixar as dependências Maven.
5. Conferir em **File > Project Structure > Project** se o SDK selecionado é o Java 21.

## 4. Executar a aplicação

1. Abrir `src/main/java/br/edu/ecowatt/EcoWattApplication.java`.
2. Clicar no triângulo verde ao lado da classe.
3. Escolher **Run 'EcoWattApplication'**.
4. Esperar aparecer no console uma mensagem indicando que a aplicação iniciou na porta 8081.
5. Abrir [http://localhost:8081](http://localhost:8081) no navegador.

No primeiro acesso, informe cidade/UF, tarifa-base de energia e bandeira tarifária vigente. Depois, esses dados podem ser alterados pelo botão de configuração no topo da tela.

## 5. Encerrar

No IntelliJ, clicar no quadrado vermelho **Stop**. Isso libera a porta 8081.

## 6. Executar os testes

No painel Maven do IntelliJ, abrir **Lifecycle** e clicar duas vezes em **test**. Também é possível clicar com o botão direito na pasta `src/test/java` e escolher **Run 'All Tests'**.

## Soluções rápidas

### A porta 8081 já está em uso

Provavelmente outra execução do EcoWatt continua aberta. Encerre-a pelo botão **Stop** e execute novamente.

### O Java 21 não aparece

Abra **File > Project Structure > SDK > Add SDK > Download JDK**, escolha a versão 21 e use uma distribuição como Eclipse Temurin.

### As dependências aparecem em vermelho

Abra o painel Maven e clique em **Reload All Maven Projects**. Confira também se o computador está conectado à internet no primeiro carregamento.

### Erro de bloqueio do banco H2

Não execute duas instâncias do EcoWatt ao mesmo tempo. Encerre todas as execuções e inicie apenas uma.

## Onde os dados ficam

O banco é criado automaticamente em `data/ecowatt.mv.db`. Essa pasta é local e não é enviada ao GitHub, para que cada integrante trabalhe com seus próprios dados de teste.
