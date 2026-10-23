# DemoQA Automation

Projeto Maven de automação de testes para o site [DemoQA](https://demoqa.com), com cenários de API e interface escritos em Gherkin. A solução foi organizada para ser simples de compreender, manter e apresentar em uma entrevista técnica.

## Objetivo e escopo

O projeto cobre:

- fluxo completo da API Book Store: criação de usuário, token, autorização, consulta do catálogo, associação de dois livros e validação do usuário;
- preenchimento e validação do Practice Form, incluindo upload;
- abertura, validação e fechamento de uma nova janela;
- criação, pesquisa, edição e exclusão de registros em Web Tables;
- cenário bônus com 12 registros dinâmicos;
- controle da Progress Bar antes de 25%, conclusão em 100% e reset;
- reordenação da lista Sortable com ações reais de drag and drop;
- evidências de falha e relatórios HTML e JSON do Cucumber.

## Tecnologias

- Java 17;
- Maven;
- Selenium WebDriver 4 e Selenium Manager;
- RestAssured;
- Cucumber Java;
- Cucumber JUnit Platform Engine;
- JUnit 5 e JUnit Platform Suite;
- Chrome e Edge;
- Git.

As versões são centralizadas nas propriedades do `pom.xml`. O projeto não usa Spring, Lombok, PageFactory, TestNG, gerenciador externo de drivers nem biblioteca de geração de massa.

## Estrutura principal

```text
demoqa-automation/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    └── test/
        ├── java/br/com/desafioqa/
        │   ├── api/
        │   │   ├── AccountApiClient.java
        │   │   └── BookStoreApiClient.java
        │   ├── config/ConfigManager.java
        │   ├── driver/DriverManager.java
        │   ├── hooks/Hooks.java
        │   ├── model/
        │   │   ├── BookSelectionRequest.java
        │   │   ├── StudentData.java
        │   │   ├── UserCredentials.java
        │   │   └── WebTableRecord.java
        │   ├── pages/
        │   │   ├── BasePage.java
        │   │   ├── BrowserWindowsPage.java
        │   │   ├── HomePage.java
        │   │   ├── PracticeFormPage.java
        │   │   ├── ProgressBarPage.java
        │   │   ├── SortablePage.java
        │   │   └── WebTablesPage.java
        │   ├── runner/CucumberTestSuite.java
        │   ├── steps/
        │   │   ├── ApiBookStoreSteps.java
        │   │   ├── BrowserWindowsSteps.java
        │   │   ├── PracticeFormSteps.java
        │   │   ├── ProgressBarSteps.java
        │   │   ├── SortableSteps.java
        │   │   └── WebTablesSteps.java
        │   └── util/
        │       ├── RandomDataGenerator.java
        │       └── ResourceUtils.java
        └── resources/
            ├── config.properties
            ├── files/upload.txt
            └── features/
                ├── api_bookstore.feature
                ├── browser_windows.feature
                ├── practice_form.feature
                ├── progress_bar.feature
                ├── sortable.feature
                └── web_tables.feature
```

## Arquitetura

As responsabilidades são separadas sem criar camadas desnecessárias:

- **Features:** descrevem os comportamentos em português.
- **Steps:** coordenam o cenário e concentram as asserções.
- **Page Objects:** encapsulam seletores, esperas e ações da interface; não contêm asserções de negócio.
- **Clientes de API:** encapsulam somente as requisições HTTP. A sequência e as validações permanecem nos steps.
- **Modelos:** transportam os dados do usuário, estudante, livros e tabela.
- **Configuração:** lê primeiro uma propriedade de sistema e, quando ela não existe, usa `config.properties`.
- **Hooks:** iniciam o navegador apenas em cenários `@ui`, registram evidência de falha e encerram o driver.

`DriverManager` usa `ThreadLocal<WebDriver>`, não configura espera implícita e deixa a descoberta do executável para o Selenium Manager. A execução foi mantida sequencial de propósito, porque o DemoQA é um ambiente público e pode apresentar instabilidade.

## Page Object

`BasePage` mantém somente operações compartilhadas: espera explícita, preenchimento, texto, scroll e clique seguro. O clique começa pela interação normal; se houver interceptação, tenta novamente após o scroll e usa JavaScript somente como último fallback. Cada página expõe ações de negócio pequenas e locators privados.

## Clientes de API

`AccountApiClient` atende aos endpoints de usuário, token, autorização e detalhes. `BookStoreApiClient` consulta o catálogo e associa livros. Eles recebem a URL configurada, usam JSON e registram a requisição/resposta apenas quando uma validação falha. Senha e token não são enviados aos logs de diagnóstico pelos steps.

## Dados de teste

`RandomDataGenerator` utiliza apenas recursos nativos do Java. Os usernames e e-mails combinam timestamp e UUID; a senha dinâmica atende aos grupos exigidos pela API; telefone, data, idade e salário respeitam formatos válidos. Estado e cidade são escolhidos de um mapa de combinações aceitas pelo formulário.

Cada cenário cria a própria massa em memória. Não há dados pessoais reais, planilha, CSV, banco ou credencial fixa.

## Configuração

Valores padrão em `src/test/resources/config.properties`:

```properties
base.url=https://demoqa.com
api.base.url=https://demoqa.com
api.relaxed.https=true
browser=chrome
headless=false
explicit.wait.seconds=10
```

Qualquer valor pode ser substituído com uma propriedade de sistema, por exemplo `-Dbrowser=edge`, `-Dheadless=true`, `-Dbase.url=...` ou `-Dapi.base.url=...`.

`api.relaxed.https=true` permite que a API pública do DemoQA seja acessada em ambientes corporativos que inspecionam HTTPS e substituem o certificado apresentado à JVM. Essa opção é adequada apenas para este ambiente de demonstração. Para validar normalmente a cadeia de certificados, use `-Dapi.relaxed.https=false`.

## Pré-requisitos

- JDK 17 instalado;
- Maven 3.8 ou superior, ou o Maven integrado ao IntelliJ IDEA;
- IntelliJ IDEA;
- Chrome ou Edge instalado;
- acesso à internet na primeira execução para o Selenium Manager resolver o driver e para acessar o DemoQA.

## Abrir no IntelliJ IDEA

1. Abra o IntelliJ IDEA e escolha **File > Open**.
2. Selecione a pasta `demoqa-automation` — não apenas a pasta `src`.
3. Quando solicitado, escolha **Load Maven Project**. Se não houver aviso, abra o painel **Maven** e clique em **Reload All Maven Projects**.
4. Acesse **File > Project Structure > Project** e selecione o JDK 17.
5. Confirme que o nível de linguagem do projeto é Java 17.
6. Aguarde o IntelliJ indexar o projeto e baixar as dependências.

Para o suporte visual às features, mantenha habilitados os plugins **Cucumber for Java** e **Gherkin** do IntelliJ.

## Executar pelo IntelliJ IDEA

A forma mais simples de executar tudo é abrir `CucumberTestSuite.java` e usar o ícone verde ao lado da classe.

Para executar uma única feature, abra o arquivo `.feature` desejado e use o ícone verde ao lado da funcionalidade ou do cenário. A configuração criada pelo IntelliJ deve usar o classpath do módulo `demoqa-automation`.

Para filtrar por tag na execução da suíte:

1. escolha **Run > Edit Configurations**;
2. abra a configuração de `CucumberTestSuite`;
3. em **VM options**, adicione, por exemplo, `-Dcucumber.filter.tags="@api"`;
4. execute a configuração.

As mesmas VM options aceitam `-Dbrowser=edge` e `-Dheadless=true`.

## Comandos para execução posterior

Na pasta que contém o `pom.xml`:

```shell
mvn test
mvn test -Dcucumber.filter.tags="@api"
mvn test -Dcucumber.filter.tags="@ui"
mvn test -Dcucumber.filter.tags="@bonus"
mvn test -Dbrowser=chrome
mvn test -Dbrowser=edge
mvn test -Dheadless=true
```

Os filtros podem ser combinados. Exemplo para o bônus no Edge em headless:

```shell
mvn test -Dcucumber.filter.tags="@bonus" -Dbrowser=edge -Dheadless=true
```

## Relatórios e evidências

Após uma execução feita pelo usuário:

- relatório HTML: `target/cucumber-report.html`;
- resultado JSON: `target/cucumber.json`;
- screenshots de cenários `@ui` com falha: `target/screenshots/`.

O screenshot também é anexado ao cenário no relatório do Cucumber. A pasta `target` é gerada durante a execução e não é versionada.

## Decisões técnicas

- a navegação web sempre começa na Home e passa pelo card e submenu, reproduzindo o comportamento real do usuário;
- os ISBNs vêm do catálogo da API e são comparados sem depender da ordem;
- janelas são identificadas pela diferença entre os handles, e não pela posição de um `Set`;
- linhas da tabela são encontradas pelo e-mail único, nunca por índice;
- o bônus pesquisa e remove cada registro individualmente, o que evita depender da paginação;
- a barra de progresso usa polling curto e atributos acessíveis, sem `Thread.sleep`;
- o Sortable relê a lista durante a ordenação e garante uma movimentação mesmo quando o estado inicial já está ordenado;
- não há retry automático, paralelismo ou lógica que esconda falhas.

## Limitações conhecidas

O DemoQA é um ambiente público, com anúncios, overlays e variações de tempo de resposta. Isso pode interferir em cliques e na precisão de parada da Progress Bar. O Selenium Manager também depende da disponibilidade do navegador e de acesso à rede para resolver um driver compatível. A API pública pode manter os usuários criados e aplicar limites ou indisponibilidades fora do controle do projeto.

## Possíveis evoluções

- pipeline de integração contínua;
- publicação automática dos relatórios;
- execução controlada em uma grade de navegadores;
- configuração de ambientes adicionais;
- métricas de duração e estabilidade;
- estratégia de limpeza de dados quando a API disponibilizar um fluxo adequado;
- paralelismo opcional depois de validar o isolamento e a estabilidade do ambiente.

## Observação sobre esta entrega

Os arquivos foram elaborados e revisados estaticamente. Nenhum teste, comando Maven, compilação ou navegador foi executado durante a criação desta solução.
