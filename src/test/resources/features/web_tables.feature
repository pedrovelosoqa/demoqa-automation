# language: pt
Funcionalidade: Gerenciar registros na Web Tables
  Como pessoa usuária da área Elements
  Quero cadastrar, alterar, consultar e excluir registros pela identidade do e-mail
  Para garantir que a tabela manipule a linha correta sem depender de sua posição

  @ui
  Cenário: Realizar o fluxo CRUD completo de um registro
    Dado que acesso a funcionalidade Web Tables
    Quando cadastro um registro válido na Web Tables
    Então o registro deve ser encontrado pelo e-mail cadastrado
    Quando edito o registro criado com novos dados válidos
    Então a Web Tables deve exibir somente os novos dados para o registro
    Quando excluo o registro criado pelo seu e-mail
    Então o registro excluído não deve mais ser encontrado

  @ui @bonus
  Cenário: Cadastrar, validar e limpar doze registros gerados na execução
    Dado que acesso a funcionalidade Web Tables
    Quando cadastro 12 registros aleatórios na Web Tables
    Então cada registro criado deve ser localizado e validado pelo e-mail
    Quando excluo somente os registros criados neste cenário
    Então nenhum registro criado neste cenário deve permanecer na Web Tables
