package com.dio.banco.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um empréstimo bancário.
 */
public class Emprestimo {
    private Cliente cliente;
    private Conta contaDestino;
    private double valorTotal;
    private double taxaJuros; // Taxa de juros mensal
    private int prazoMeses;
    private LocalDate dataContratacao;
    private double valorParcela;
    private List<Parcela> parcelas;
    private StatusEmprestimo status;
    
    /**
     * Enum que representa os status possíveis de um empréstimo.
     */
    public enum StatusEmprestimo {
        SOLICITADO, APROVADO, NEGADO, LIQUIDADO, EM_ANDAMENTO
    }
    
    public Emprestimo(Cliente cliente, Conta contaDestino, double valorTotal, double taxaJuros, int prazoMeses) {
        this.cliente = cliente;
        this.contaDestino = contaDestino;
        this.valorTotal = valorTotal;
        this.taxaJuros = taxaJuros;
        this.prazoMeses = prazoMeses;
        this.dataContratacao = LocalDate.now();
        this.valorParcela = calcularValorParcela();
        this.parcelas = new ArrayList<>();
        this.status = StatusEmprestimo.SOLICITADO;
    }
    
    /**
     * Calcula o valor de cada parcela do empréstimo.
     * @return Valor da parcela
     */
    private double calcularValorParcela() {
        double taxaMensal = 1 + (taxaJuros / 100);
        return (valorTotal * Math.pow(taxaMensal, prazoMeses)) / prazoMeses;
    }
    
    /**
     * Aprova o empréstimo, deposita o valor na conta do cliente e gera as parcelas.
     */
    public void aprovarEmprestimo() {
        if (status == StatusEmprestimo.SOLICITADO) {
            status = StatusEmprestimo.APROVADO;
            contaDestino.depositar(valorTotal);
            gerarParcelas();
            status = StatusEmprestimo.EM_ANDAMENTO;
        }
    }
    
    /**
     * Nega o empréstimo.
     */
    public void negarEmprestimo() {
        if (status == StatusEmprestimo.SOLICITADO) {
            status = StatusEmprestimo.NEGADO;
        }
    }
    
    /**
     * Gera as parcelas do empréstimo.
     */
    private void gerarParcelas() {
        parcelas = new ArrayList<>();
        LocalDate dataVencimento = dataContratacao.plusMonths(1);
        
        for (int i = 1; i <= prazoMeses; i++) {
            Parcela parcela = new Parcela(i, valorParcela, dataVencimento, false);
            parcelas.add(parcela);
            dataVencimento = dataVencimento.plusMonths(1);
        }
    }
    
    /**
     * Paga uma parcela do empréstimo.
     * @param numeroParcela Número da parcela a ser paga
     * @param contaPagamento Conta de onde será debitado o valor
     * @return true se o pagamento foi realizado com sucesso, false caso contrário
     */
    public boolean pagarParcela(int numeroParcela, Conta contaPagamento) {
        if (numeroParcela < 1 || numeroParcela > parcelas.size()) {
            return false;
        }
        
        Parcela parcela = parcelas.get(numeroParcela - 1);
        if (parcela.isPaga()) {
            return false;
        }
        
        boolean sucesso = contaPagamento.sacar(parcela.getValor());
        if (sucesso) {
            parcela.setPaga(true);
            
            // Verifica se todas as parcelas foram pagas
            boolean todasPagas = true;
            for (Parcela p : parcelas) {
                if (!p.isPaga()) {
                    todasPagas = false;
                    break;
                }
            }
            
            if (todasPagas) {
                status = StatusEmprestimo.LIQUIDADO;
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Calcula o valor total a ser pago (principal + juros).
     * @return Valor total a ser pago
     */
    public double calcularValorTotalAPagar() {
        return valorParcela * prazoMeses;
    }
    
    /**
     * Calcula o valor total dos juros.
     * @return Valor total dos juros
     */
    public double calcularValorTotalJuros() {
        return calcularValorTotalAPagar() - valorTotal;
    }
    
    /**
     * Classe interna que representa uma parcela de empréstimo.
     */
    public static class Parcela {
        private int numero;
        private double valor;
        private LocalDate dataVencimento;
        private boolean paga;
        
        public Parcela(int numero, double valor, LocalDate dataVencimento, boolean paga) {
            this.numero = numero;
            this.valor = valor;
            this.dataVencimento = dataVencimento;
            this.paga = paga;
        }
        
        public int getNumero() {
            return numero;
        }
        
        public double getValor() {
            return valor;
        }
        
        public LocalDate getDataVencimento() {
            return dataVencimento;
        }
        
        public boolean isPaga() {
            return paga;
        }
        
        public void setPaga(boolean paga) {
            this.paga = paga;
        }
    }
} 