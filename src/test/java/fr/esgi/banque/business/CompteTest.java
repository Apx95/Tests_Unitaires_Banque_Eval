package fr.esgi.banque.business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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

}
