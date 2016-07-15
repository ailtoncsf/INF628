# Patterns for MapReduce Applications

##Instituto Federal de Educação, Ciência e Tecnologia da Bahia (IFBA) - GSORT
 - Curso: Pós graduação em Computação Ubíqua e Distribuída
 - Disciplina: INF628 [Engenharia de Software para Sistemas Distribuídos]
 - Aluno: Ailton Filho 
 - Prof.: Sandro Andrade
  
### Objetivo: 
 - Aplicação de Design Patterns para sistemas MapReduce utilizando DataSets do StackOverFlow.
 
#### Atividades:
	- Realizar filtro dos usuários brasileiros [ok]
	- Realizar reduce side join (inner join) com dataset de posts [ok]
	- Obter os top ten usuários que mais postaram [ok]
	- Realizar join exibindo id do usuario, displayName e dados da outra tabela do join [ok]
  
#### Observações: 
	- Executar o comando: mvn clean install p/ compilar projeto e gerar o arquivo .jar
	- Para executar os JOBS: hadoop jar nome_do_jar.jar hadoop.ifba.Inf628 [INPUT_PATH_FILTER] [INPUT_PATH_POSTS]
 
