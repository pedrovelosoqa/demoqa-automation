# language: pt
@api
Funcionalidade: Gerenciar livros de um usuário pela API da DemoQA

  Cenário: Criar e autenticar um usuário e adicionar dois livros à sua coleção
    Dado que gero credenciais válidas e dinâmicas para um novo usuário
    Quando crio o usuário pela API
    Então a API deve criar o usuário com status 201, identificador, nome e lista de livros vazia
    Quando gero um token para o usuário criado
    Então a API deve retornar status 200, token preenchido e status Success
    Quando consulto a autorização do usuário
    Então a API deve retornar status 200 e autorização verdadeira
    Quando consulto os livros disponíveis na Book Store
    Então a API deve retornar status 200 e pelo menos dois livros válidos e distintos
    Quando adiciono os dois livros selecionados à coleção do usuário
    Então a API deve adicionar a coleção com status 201
    Quando consulto o usuário autenticado pelo identificador
    Então a API deve retornar o usuário correto com exatamente os dois livros selecionados
