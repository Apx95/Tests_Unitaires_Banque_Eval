package fr.esgi.banque.business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CalculFraisTest {

	private final CalculFrais calcul = new CalculFrais();

	// F1 : titulaire majeur et solde moyen obligatoire

	@ParameterizedTest
	@ValueSource(ints = {17, 0, -1})
	void shouldRejectMinorHolder(int age) {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> calcul.fraisMensuels(age, new BigDecimal("500"), false));
	}

	@Test
	void shouldAcceptHolderOfExactly18() {
		// Act
		BigDecimal frais = calcul.fraisMensuels(18, new BigDecimal("500"), false);

		// Assert : pas d'exception, tenue offerte car moins de 26 ans
		assertThat(frais).isEqualByComparingTo("0.00");
	}

	@Test
	void shouldRejectMissingAverageBalance() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> calcul.fraisMensuels(30, null, false));
	}

	// F2 : frais de tenue de 2,00 € par mois

	@Test
	void shouldChargeAccountFeeOf2Euros() {
		// Act
		BigDecimal frais = calcul.fraisMensuels(40, new BigDecimal("500"), false);

		// Assert
		assertThat(frais).isEqualByComparingTo("2.00");
	}

}
