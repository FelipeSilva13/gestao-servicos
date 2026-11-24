package com.felipe.gestao_servicos.repository;

import com.felipe.gestao_servicos.domain.DiaAtendimento;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaAtendimentoRepository extends JpaRepository<DiaAtendimento, Long> {

    List<DiaAtendimento> findByTenantIdAndDataBetweenOrderByData(Long tenantId, LocalDate inicio, LocalDate fim);

    Optional<DiaAtendimento> findByTenantIdAndData(Long tenantId, LocalDate data);

    /** Trava os dias envolvidos para que dois agendamentos simultâneos não peguem o mesmo horário. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<DiaAtendimento> findForUpdateByTenantIdAndDataBetweenOrderByData(Long tenantId, LocalDate inicio, LocalDate fim);
}
