# language: pt
@ui
Funcionalidade: Reordenar a lista Sortable
  Como pessoa usuária da área Interactions
  Quero mover os itens com ações reais de drag and drop
  Para validar a ordem apresentada após cada interação

  Cenário: Desordenar e restaurar a sequência de One até Six
    Dado que acesso a lista da funcionalidade Sortable
    Então a lista Sortable deve exibir os textos One até Six
    Quando desordeno a lista Sortable de forma controlada
    Então a ordem da lista deve ser diferente da sequência esperada
    Quando reordeno todos os itens da lista por drag and drop
    Então a lista Sortable deve ficar na sequência One até Six
