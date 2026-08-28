-- Migration inicial para as tabelas da aplicação

CREATE TABLE IF NOT EXISTS clientes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  cpf_cnpj VARCHAR(50),
  telefone VARCHAR(50),
  email VARCHAR(255),
  endereco VARCHAR(512),
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tecnicos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL,
  especialidade VARCHAR(255),
  telefone VARCHAR(50),
  email VARCHAR(255),
  status VARCHAR(50),
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS servicos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  descricao TEXT,
  preco DECIMAL(10,2),
  duracao VARCHAR(50),
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ordem_servico (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cliente_id BIGINT,
  tecnico_id BIGINT,
  servico_id BIGINT,
  status VARCHAR(50),
  abertura DATE,
  FOREIGN KEY (cliente_id) REFERENCES clientes(id),
  FOREIGN KEY (tecnico_id) REFERENCES tecnicos(id),
  FOREIGN KEY (servico_id) REFERENCES servicos(id)
);
