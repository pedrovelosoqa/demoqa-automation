# language: pt
@ui @browser-windows
Funcionalidade: Interação com múltiplas janelas do navegador
  Como pessoa candidata ao desafio de automação
  Quero abrir e fechar uma nova janela
  Para validar a troca segura entre contextos do navegador

  Cenário: Abrir uma nova janela e retornar para a original
    Dado que acesso a página Browser Windows
    Quando abro uma nova janela
    Então a nova janela deve exibir a mensagem "This is a sample page"
    E fecho a nova janela e retorno para a janela original
