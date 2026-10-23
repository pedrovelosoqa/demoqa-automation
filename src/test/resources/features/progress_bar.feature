# language: pt
@ui
Funcionalidade: Controlar a barra de progresso
  Como pessoa usuária da área Widgets
  Quero iniciar, interromper, concluir e reiniciar a barra
  Para garantir que o componente responda corretamente em cada estado

  Cenário: Interromper em uma faixa segura, concluir e reiniciar o progresso
    Dado que acesso a funcionalidade Progress Bar
    Quando inicio e interrompo a barra ao atingir pelo menos 15 por cento
    Então a barra deve permanecer parada acima de zero e em no máximo 25 por cento
    Quando reinicio a barra e aguardo a conclusão
    Então a barra deve atingir exatamente 100 por cento
    Quando aciono o reset da barra de progresso
    Então a barra de progresso deve voltar para 0 por cento
