# language: pt
@ui @practice-form
Funcionalidade: Preenchimento do formulário de estudante
  Como pessoa candidata ao desafio de automação
  Quero preencher e enviar o Practice Form
  Para validar os dados apresentados na confirmação

  Cenário: Enviar o formulário com todos os dados válidos
    Dado que acesso o formulário Practice Form
    Quando preencho e submeto o formulário com dados válidos
    Então o modal de confirmação deve exibir todos os dados informados
    E posso fechar o modal de confirmação
