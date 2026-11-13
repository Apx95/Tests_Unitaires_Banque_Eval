package fr.esgi.banque.business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CompteTest {

	@Test
	void shouldIncreaseBalanceWhenCrediting() {
		// Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		// Act
		compte.crediter(new BigDecimal("50.00"));

		// Assert
		assertThat(compte.getSolde()).isEqualByComparingTo("150.00");
	}

	// C1 : numéro et titulaire obligatoires

	@Test
	void shouldCreateAccountWhenAllFieldsAreValid() {
		// Act
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		// Assert
		assertThat(compte.getNumero()).isEqualTo("FR001");
		assertThat(compte.getTitulaire()).isEqualTo("Alice");
		assertThat(compte.getSolde()).isEqualByComparingTo("100.00");
		assertThat(compte.getDecouvertAutorise()).isEqualByComparingTo("200");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"   "})
	void shouldRejectMissingNumero(String numero) {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new Compte(numero, "Alice", new BigDecimal("100.00"), new BigDecimal("200")));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"   "})
	void shouldRejectMissingTitulaire(String titulaire) {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new Compte("FR001", titulaire, new BigDecimal("100.00"), new BigDecimal("200")));
	}

	// C2 : solde initial et découvert obligatoires, découvert positif ou nul

	@ParameterizedTest
	@CsvSource(value = {
			"NULL, 200",
			"100.00, NULL"
	}, nullValues = "NULL")
	void shouldRejectMissingSoldeOrDecouvert(BigDecimal soldeInitial, BigDecimal decouvert) {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new Compte("FR001", "Alice", soldeInitial, decouvert));
	}

	@Test
	void shouldRejectNegativeDecouvert() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("-0.01")));
	}

	@Test
	void shouldAcceptZeroDecouvert() {
		// Act
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), BigDecimal.ZERO);

		// Assert
		assertThat(compte.getDecouvertAutorise()).isEqualByComparingTo("0");
	}

	// C3 : crediter un montant strictement positif

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"0", "0.00", "-0.01", "-50"})
	void shouldRejectCreditWhenAmountIsNotStrictlyPositive(BigDecimal montant) {
		// Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		// Act + Assert
		assertThatIllegalArgumentException().isThrownBy(() -> compte.crediter(montant));
		assertThat(compte.getSolde()).isEqualByComparingTo("100.00");
	}

	@Test
	void shouldAcceptSmallestPositiveCredit() {
		// Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		// Act
		compte.crediter(new BigDecimal("0.01"));

		// Assert
		assertThat(compte.getSolde()).isEqualByComparingTo("100.01");
	}

	// C4 : debiter un montant strictement positif

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"0", "0.00", "-0.01", "-50"})
	void shouldRejectDebitWhenAmountIsNotStrictlyPositive(BigDecimal montant) {
		// Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		// Act + Assert
		assertThatIllegalArgumentException().isThrownBy(() -> compte.debiter(montant));
		assertThat(compte.getSolde()).isEqualByComparingTo("100.00");
	}

	@Test
	void shouldDecreaseBalanceWhenDebiting() {
		// Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		// Act
		compte.debiter(new BigDecimal("30.00"));

		// Assert
		assertThat(compte.getSolde()).isEqualByComparingTo("70.00");
	}

}
