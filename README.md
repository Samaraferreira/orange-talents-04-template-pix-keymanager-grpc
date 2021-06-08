# Desafio: Pix

Recentemente o Banco Central do Brasil (BCB) trouxe ao mercado o **Pix**. Em poucas palavras, o Pix é um novo meio de pagamentos para fazer transferências de forma rápida, sem esperar dias para que o pagamento “caia” na conta de quem o receberá ou ter que fazê-los só em dias da semana, no horário comercial. Além de poder transferir dinheiro para outras pessoas, será possível também fazer pagamentos a estabelecimentos usando o Pix, por exemplo.

Com o Pix, pagamentos e transferências são **concluídos em alguns segundos** e podem ser feitos a qualquer horário e dia, incluindo finais de semana e feriados. O Pix vai, portanto, facilitar e agilizar as transferências de valores entre pessoas e estabelecimentos comerciais, o pagamento de contas e até recolhimento de impostos e taxas de serviços, entre outras possibilidades.

Vale dizer que para enviar ou receber um Pix, não é necessário fazer nenhum cadastro ou baixar um aplicativo – ele pode ser usado diretamente no aplicativo de sua instituição; é necessário somente que ela ofereça esse meio de pagamento.

Dessa forma, nosso **objetivo** é garantir que nossos usuários possam:
1. efetuar pagamentos e transferências via Pix na nossa plataforma;
2. registrar suas chaves Pix em nossa plataforma usando suas informações pessoais: CPF, telefone celular, email ou uma chave aleatória. Com uma chave registrada, será possível receber e pagar via Pix;

Para facilitar ainda mais o entendimento sobre o Desafio PIX e arquitetura proposta para esse projeto, nós [gravamos esse vídeo](https://www.youtube.com/watch?v=pe0m6ATJlOI&feature=youtu.be) para que você não tenha dúvidas!

# Entendendo a Arquitetura do Projeto

Por o Pix se tratar de produto novo no mercado, decidimos criar uma **fachada (façade)** com o intuito aproveitar e estender os recursos existentes na nossa atual plataforma (ERP-Itau) e assim diminuir o impacto das mudanças na mesma. Portanto, para esse projeto, nossa arquitetura será baseada em microservices para trazer maior flexibilidade e produtividade durante o desenvolvimento. Não só isso, com essa distribuição dos módulos nós poderemos escalá-los de forma independente e isolada.

![Arquitetura do projeto Pix](assets/pix.png "Arquitetura do projeto Pix")

Para desenvolver todas as funcionalidades, precisaremos implementar 4 (quatro) microsserviços utilizando a **Orange Stack**, que nada mais é do que as tecnologias, serviços nas nuvens e práticas de deployment recomendadas pelas equipes de engenharia da Zup e Itau. Ao falarmos dessa stack, nós falamos de tecnologias como:

* Kotlin como linguagem de programação dos microsserviços;
* Micronaut e todos seus módulos como base de desenvolvimento para tirar proveitos de recursos nas nuvens;
* gRPC para integração dos microsserviços dentro da nossa infraestrutura;
* Kafka para mensageria assincrona e desacoplamento entre os microsserviços;
* Redis para cache distribuído;
* OAuth2, OpenId e Keycloak para segurança: autenticação e autorização;
* RDBMS (PostgreSQL e MySQL) e DynamoDB para armazenar e gerenciar nossos dados;
* e vários serviços especializados da AWS;

Falando em microsserviços, nossos 2 serviços serão estes:

- **KeyManager-gRPC**: microsserviço responsável por fazer todo o gerenciamento das chaves Pix dos nossos clientes (usuários), além de ser o ponto central de comunicação da nossa arquitetura para busca de chaves;
- **KeyManager-REST**: microsserviço responsável por expor serviço KeyManager-gRPC através de uma API REST de tal forma que ela possa ser consumida pelo time de frontend de forma eficiente e segura;