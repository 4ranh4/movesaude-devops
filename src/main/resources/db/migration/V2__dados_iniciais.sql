-- Dados de demonstração (fictícios): operadoras, rede de prestadores, passageiros e corridas concluídas.
-- Os IDs são gerados pelo banco; os vínculos usam chaves naturais (CNPJ, placa, nome).

-- Planos de convênio com benefício mobilidade (operadoras fictícias, como no protótipo)
INSERT INTO plano_convenio (id, operadora, nome, creditos_mensais, coparticipacao_por_trecho, desconto_pct,
                            modalidades_cobertas, finalidades_elegiveis, autorizacao_para) VALUES
('aurora_essencial', 'Aurora Saúde', 'Essencial', 8, 10.00, 0.15,
 'ASSISTIDO,ACESSIVEL', 'CONSULTA,EXAME,TERAPIA,HEMODIALISE,ONCOLOGIA,ALTA_HOSPITALAR', ''),
('aurora_renal', 'Aurora Saúde', 'Programa Cuidado Renal', 30, 0.00, 0.20,
 'ASSISTIDO,ACESSIVEL,MEDICO', 'CONSULTA,EXAME,HEMODIALISE', 'MEDICO'),
('serena_master', 'Serena Saúde', 'Master', 12, 15.00, 0.20,
 'ASSISTIDO,ACESSIVEL,MEDICO', 'CONSULTA,EXAME,TERAPIA,HEMODIALISE,ONCOLOGIA,ALTA_HOSPITALAR', 'MEDICO'),
('vertice_empresarial', 'Vértice Saúde', 'Empresarial', 0, 0.00, 0.10, '', '', '');

-- Rede de prestadores
INSERT INTO prestador (nome_fantasia, cnpj, cidade, modalidades, status_verificacao, pausado, possui_tecnico,
                       possui_enfermeiro, hora_inicio, hora_fim, taxa_oxigenio, taxa_tecnico, taxa_enfermeiro,
                       avaliacao, corridas_concluidas) VALUES
('Acessa Vans', '12.345.678/0001-01', 'São Paulo', 'ACESSIVEL', 'VERIFICADO', FALSE, FALSE, FALSE,
 '06:00', '20:00', 0, 0, 0, 4.8, 640),
('Cuidar Transportes', '23.456.789/0001-02', 'São Paulo', 'ASSISTIDO,ACESSIVEL', 'VERIFICADO', FALSE, FALSE, FALSE,
 '07:00', '19:00', 0, 0, 0, 4.6, 320),
('Vida Remoções', '34.567.890/0001-03', 'Guarulhos', 'MEDICO', 'VERIFICADO', FALSE, TRUE, TRUE,
 '00:00', '23:59', 60.00, 90.00, 150.00, 4.9, 1200),
('Mobi Saúde Express', '45.678.901/0001-04', 'São Paulo', 'ASSISTIDO', 'VERIFICADO', FALSE, FALSE, FALSE,
 '08:00', '18:00', 0, 0, 0, 4.3, 85),
('Rota Inclusiva', '56.789.012/0001-05', 'Osasco', 'ACESSIVEL', 'EM_ANALISE', FALSE, FALSE, FALSE,
 '06:00', '22:00', 0, 0, 0, 5.0, 0);

INSERT INTO prestador_tarifa (prestador_id, modalidade, base, por_km)
SELECT id, 'ACESSIVEL', 45.00, 4.20 FROM prestador WHERE cnpj = '12.345.678/0001-01';
INSERT INTO prestador_tarifa (prestador_id, modalidade, base, por_km)
SELECT id, 'ASSISTIDO', 25.00, 2.80 FROM prestador WHERE cnpj = '23.456.789/0001-02';
INSERT INTO prestador_tarifa (prestador_id, modalidade, base, por_km)
SELECT id, 'ACESSIVEL', 40.00, 3.90 FROM prestador WHERE cnpj = '23.456.789/0001-02';
INSERT INTO prestador_tarifa (prestador_id, modalidade, base, por_km)
SELECT id, 'MEDICO', 180.00, 7.50 FROM prestador WHERE cnpj = '34.567.890/0001-03';
INSERT INTO prestador_tarifa (prestador_id, modalidade, base, por_km)
SELECT id, 'ASSISTIDO', 20.00, 2.50 FROM prestador WHERE cnpj = '45.678.901/0001-04';
INSERT INTO prestador_tarifa (prestador_id, modalidade, base, por_km)
SELECT id, 'ACESSIVEL', 38.00, 3.60 FROM prestador WHERE cnpj = '56.789.012/0001-05';

-- Frota (assentos de passageiro, sem contar o motorista)
INSERT INTO veiculo (prestador_id, tipo, modelo, placa, assentos, vagas_cadeira, maca, oxigenio, combustivel)
SELECT id, 'VAN_RAMPA', 'Renault Kangoo E-Tech adaptada', 'EVA1C23', 3, 1, FALSE, FALSE, 'ELETRICO'
FROM prestador WHERE cnpj = '12.345.678/0001-01';
INSERT INTO veiculo (prestador_id, tipo, modelo, placa, assentos, vagas_cadeira, maca, oxigenio, combustivel)
SELECT id, 'VAN_ELEVADOR', 'Fiat Ducato com elevador', 'ACV2D34', 4, 2, FALSE, FALSE, 'DIESEL'
FROM prestador WHERE cnpj = '12.345.678/0001-01';
INSERT INTO veiculo (prestador_id, tipo, modelo, placa, assentos, vagas_cadeira, maca, oxigenio, combustivel)
SELECT id, 'SEDAN', 'Toyota Corolla Hybrid', 'CDR3E45', 4, 0, FALSE, FALSE, 'HIBRIDO'
FROM prestador WHERE cnpj = '23.456.789/0001-02';
INSERT INTO veiculo (prestador_id, tipo, modelo, placa, assentos, vagas_cadeira, maca, oxigenio, combustivel)
SELECT id, 'MINIVAN', 'Chevrolet Spin', 'CDR4F56', 6, 0, FALSE, FALSE, 'FLEX'
FROM prestador WHERE cnpj = '23.456.789/0001-02';
INSERT INTO veiculo (prestador_id, tipo, modelo, placa, assentos, vagas_cadeira, maca, oxigenio, combustivel)
SELECT id, 'VAN_RAMPA', 'Peugeot Expert com rampa', 'CDR5G67', 5, 1, FALSE, FALSE, 'DIESEL'
FROM prestador WHERE cnpj = '23.456.789/0001-02';
INSERT INTO veiculo (prestador_id, tipo, modelo, placa, assentos, vagas_cadeira, maca, oxigenio, combustivel)
SELECT id, 'AMBULANCIA', 'Mercedes Sprinter UTI básica', 'VRM6H78', 2, 0, TRUE, TRUE, 'DIESEL'
FROM prestador WHERE cnpj = '34.567.890/0001-03';
INSERT INTO veiculo (prestador_id, tipo, modelo, placa, assentos, vagas_cadeira, maca, oxigenio, combustivel)
SELECT id, 'SEDAN', 'BYD Dolphin', 'MSE7J89', 4, 0, FALSE, FALSE, 'ELETRICO'
FROM prestador WHERE cnpj = '45.678.901/0001-04';
INSERT INTO veiculo (prestador_id, tipo, modelo, placa, assentos, vagas_cadeira, maca, oxigenio, combustivel)
SELECT id, 'VAN_RAMPA', 'Renault Master com rampa', 'RIN8K90', 6, 1, FALSE, FALSE, 'FLEX'
FROM prestador WHERE cnpj = '56.789.012/0001-05';

-- Passageiros
INSERT INTO passageiro (nome, mobilidade, acompanhantes, oxigenio, equipe, obesidade, cao_guia, plano_id, carteirinha) VALUES
('Lúcia Ferreira', 'CADEIRA_PERMANECE', 1, 'NAO', 'NAO', FALSE, FALSE, 'aurora_renal', 'AUR-2048-7731'),
('Antônio Pereira', 'MACA', 1, 'FORNECER', 'TECNICO', FALSE, FALSE, 'serena_master', 'SER-5512-0098'),
('Marina Costa', 'ANDA_SEM_AJUDA', 0, 'NAO', 'NAO', FALSE, TRUE, 'aurora_essencial', 'AUR-3301-4420'),
('Carlos Souza', 'ANDA_COM_APOIO', 1, 'NAO', 'NAO', FALSE, FALSE, NULL, NULL);

-- Corridas concluídas (alimentam os indicadores ESG)
INSERT INTO corrida (passageiro_id, origem, destino, distancia_km, data_corrida, hora_embarque, ida_e_volta, finalidade,
                     modalidade_indicada, status, prestador_id, veiculo_id, modalidade_atendida, horario_confirmado,
                     preco, cliente_paga, convenio_paga, trechos_cobertos, comissao, resumo_cobertura, criada_em, atualizada_em)
SELECT pa.id, 'Rua Voluntários da Pátria, 1200 - Santana', 'Clínica Renal Norte - Tucuruvi', 8.4, DATE '2026-10-06',
       TIME '06:30:00', TRUE, 'HEMODIALISE', 'ACESSIVEL', 'CONCLUIDA', pr.id, v.id, 'ACESSIVEL', TIME '06:30:00',
       161.00, 0.00, 161.00, 2, 28.98, 'Coberto pelo convênio: usa 2 créditos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM passageiro pa, prestador pr, veiculo v
WHERE pa.nome = 'Lúcia Ferreira' AND pr.cnpj = '12.345.678/0001-01' AND v.placa = 'EVA1C23';

INSERT INTO corrida (passageiro_id, origem, destino, distancia_km, data_corrida, hora_embarque, ida_e_volta, finalidade,
                     modalidade_indicada, status, prestador_id, veiculo_id, modalidade_atendida, horario_confirmado,
                     preco, cliente_paga, convenio_paga, trechos_cobertos, comissao, resumo_cobertura, criada_em, atualizada_em)
SELECT pa.id, 'Av. Paulista, 900 - Bela Vista', 'Hospital das Clínicas - Cerqueira César', 5.2, DATE '2026-10-02',
       TIME '09:00:00', FALSE, 'CONSULTA', 'ASSISTIDO', 'CONCLUIDA', pr.id, v.id, 'ASSISTIDO', TIME '09:00:00',
       33.00, 10.00, 23.00, 1, 5.94, 'Coberto pelo convênio: usa 1 crédito + coparticipação de R$ 10,00',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM passageiro pa, prestador pr, veiculo v
WHERE pa.nome = 'Marina Costa' AND pr.cnpj = '45.678.901/0001-04' AND v.placa = 'MSE7J89';

INSERT INTO corrida (passageiro_id, origem, destino, distancia_km, data_corrida, hora_embarque, ida_e_volta, finalidade,
                     modalidade_indicada, status, prestador_id, veiculo_id, modalidade_atendida, horario_confirmado,
                     preco, cliente_paga, convenio_paga, trechos_cobertos, comissao, resumo_cobertura, criada_em, atualizada_em)
SELECT pa.id, 'Rua Tuiuti, 300 - Tatuapé', 'Laboratório Central - Mooca', 11.0, DATE '2026-09-29',
       TIME '07:30:00', TRUE, 'EXAME', 'ASSISTIDO', 'CONCLUIDA', pr.id, v.id, 'ASSISTIDO', TIME '07:30:00',
       112.00, 112.00, 0.00, 0, 20.16, 'Pagamento particular', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM passageiro pa, prestador pr, veiculo v
WHERE pa.nome = 'Carlos Souza' AND pr.cnpj = '23.456.789/0001-02' AND v.placa = 'CDR3E45';

INSERT INTO corrida (passageiro_id, origem, destino, distancia_km, data_corrida, hora_embarque, ida_e_volta, finalidade,
                     modalidade_indicada, status, prestador_id, veiculo_id, modalidade_atendida, horario_confirmado,
                     preco, cliente_paga, convenio_paga, trechos_cobertos, comissao, resumo_cobertura, criada_em, atualizada_em)
SELECT pa.id, 'Hospital Municipal - Guarulhos', 'Rua das Acácias, 45 - Vila Galvão', 14.5, DATE '2026-10-08',
       TIME '14:00:00', FALSE, 'ALTA_HOSPITALAR', 'MEDICO', 'CONCLUIDA', pr.id, v.id, 'MEDICO', TIME '14:00:00',
       439.00, 15.00, 424.00, 1, 79.02, 'Coberto pelo convênio: usa 1 crédito + coparticipação de R$ 15,00',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM passageiro pa, prestador pr, veiculo v
WHERE pa.nome = 'Antônio Pereira' AND pr.cnpj = '34.567.890/0001-03' AND v.placa = 'VRM6H78';
