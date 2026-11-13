package fr.esgi.banque.service.impl;

import fr.esgi.banque.business.Compte;
import fr.esgi.banque.business.CompteIntrouvableException;
import fr.esgi.banque.business.SoldeInsuffisantException;
import fr.esgi.banque.business.Virement;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VirementServiceImplTest {

	@Mock
	private CompteRepository repository;

	@Mock
	private NotificationService notifications;

	// mercredi 11 novembre 2026
	private static final Clock MERCREDI = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), ZoneId.of("Europe/Paris"));

	private VirementServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new VirementServiceImpl(repository, notifications, MERCREDI);
	}

	// V1 : montant null, nul ou négatif refusé

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"0", "0.00", "-0.01", "-100"})
	void shouldRejectAmountNotStrictlyPositive(BigDecimal montant) {
		// Act + Assert
		assertThatIllegalArgumentException()
				.isThrownBy(() -> service.virer("FR001", "FR002", montant));
	}

	// V2 : virement vers le même compte refusé

	@Test
	void shouldRejectTransferToSameAccount() {
		// Act + Assert
		assertThatIllegalArgumentException()
				.isThrownBy(() -> service.virer("FR001", "FR001", new BigDecimal("100.00")));
	}

	// V3 : compte introuvable

	@Test
	void shouldThrowWhenSourceAccountDoesNotExist() {
		// Arrange
		when(repository.trouver("FR001")).thenReturn(Optional.empty());

		// Act + Assert
		assertThatThrownBy(() -> service.virer("FR001", "FR002", new BigDecimal("100.00")))
				.isInstanceOf(CompteIntrouvableException.class);
	}

	@Test
	void shouldThrowWhenDestinationAccountDoesNotExist() {
		// Arrange
		when(repository.trouver("FR001")).thenReturn(Optional.of(compte("FR001", "Alice", "500.00")));
		when(repository.trouver("FR002")).thenReturn(Optional.empty());

		// Act + Assert
		assertThatThrownBy(() -> service.virer("FR001", "FR002", new BigDecimal("100.00")))
				.isInstanceOf(CompteIntrouvableException.class);
	}

	// V4 : cas nominal

	@Test
	void shouldDebitSourceCreditDestinationAndSaveBoth() {
		// Arrange
		Compte source = compte("FR001", "Alice", "500.00");
		Compte destination = compte("FR002", "Bob", "100.00");
		comptesExistants(source, destination);

		// Act
		Virement virement = service.virer("FR001", "FR002", new BigDecimal("200.00"));

		// Assert
		assertThat(source.getSolde()).isEqualByComparingTo("300.00");
		assertThat(destination.getSolde()).isEqualByComparingTo("300.00");
		verify(repository).sauvegarder(source);
		verify(repository).sauvegarder(destination);
		assertThat(virement).isEqualTo(new Virement("FR001", "FR002", new BigDecimal("200.00"), LocalDate.of(2026, 11, 11)));
	}

	// V5 : solde insuffisant, rien n'est modifié ni sauvegardé

	@Test
	void shouldNotSaveAnythingWhenSourceBalanceIsInsufficient() {
		// Arrange
		Compte source = compte("FR001", "Alice", "100.00");
		Compte destination = compte("FR002", "Bob", "100.00");
		comptesExistants(source, destination);

		// Act + Assert
		assertThatThrownBy(() -> service.virer("FR001", "FR002", new BigDecimal("200.00")))
				.isInstanceOf(SoldeInsuffisantException.class);
		assertThat(source.getSolde()).isEqualByComparingTo("100.00");
		assertThat(destination.getSolde()).isEqualByComparingTo("100.00");
		verify(repository, never()).sauvegarder(any());
	}

	// V6 : alerte au titulaire source au-delà de 10 000 €

	@Test
	void shouldAlertSourceHolderWhenAmountIsAbove10000() {
		// Arrange
		comptesExistants(compte("FR001", "Alice", "20000.00"), compte("FR002", "Bob", "0.00"));

		// Act
		service.virer("FR001", "FR002", new BigDecimal("10000.01"));

		// Assert
		verify(notifications).alerter(eq("Alice"), anyString());
	}

	@Test
	void shouldNotAlertWhenAmountIsExactly10000() {
		// Arrange
		comptesExistants(compte("FR001", "Alice", "20000.00"), compte("FR002", "Bob", "0.00"));

		// Act
		service.virer("FR001", "FR002", new BigDecimal("10000.00"));

		// Assert
		verify(notifications, never()).alerter(anyString(), anyString());
	}

	private void comptesExistants(Compte... comptes) {
		for (Compte compte : comptes) {
			when(repository.trouver(compte.getNumero())).thenReturn(Optional.of(compte));
		}
	}

	private static Compte compte(String numero, String titulaire, String solde) {
		return new Compte(numero, titulaire, new BigDecimal(solde), BigDecimal.ZERO);
	}

}
