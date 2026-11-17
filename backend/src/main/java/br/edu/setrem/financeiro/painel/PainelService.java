package br.edu.setrem.financeiro.painel;

import br.edu.setrem.financeiro.conta.ContaResponse;
import br.edu.setrem.financeiro.conta.ContaService;
import br.edu.setrem.financeiro.transacao.TipoTransacao;
import br.edu.setrem.financeiro.transacao.TransacaoService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@ApplicationScoped
public class PainelService {

    private static final int QUANTIDADE_ULTIMAS = 6;
    // O "mês atual" segue o horário de Brasília, independente do fuso do servidor.
    private static final ZoneId FUSO_DO_USUARIO = ZoneId.of("America/Sao_Paulo");

    @Inject
    ContaService contaService;

    @Inject
    TransacaoService transacaoService;

    public PainelResponse resumo() {
        List<ContaResponse> contas = contaService.listarComSaldo();
        BigDecimal saldoTotal = contas.stream()
                .map(ContaResponse::saldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth mesAtual = YearMonth.now(FUSO_DO_USUARIO);
        LocalDate inicio = mesAtual.atDay(1);
        LocalDate fim = mesAtual.atEndOfMonth();

        return new PainelResponse(
                saldoTotal,
                contas,
                transacaoService.total(TipoTransacao.ENTRADA, inicio, fim),
                transacaoService.total(TipoTransacao.SAIDA, inicio, fim),
                transacaoService.ultimas(QUANTIDADE_ULTIMAS));
    }
}
