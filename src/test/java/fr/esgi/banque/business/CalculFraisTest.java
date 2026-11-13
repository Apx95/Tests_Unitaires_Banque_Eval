package fr.esgi.banque.business;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

	// F3 : tenue offerte aux moins de 26 ans

	@ParameterizedTest(name = "{0} ans -> frais {1}")
	@CsvSource({
			"18, 0.00",
			"25, 0.00",
			"26, 2.00",
			"27, 2.00"
	})
	void shouldOfferAccountFeeOnlyUnder26(int age, BigDecimal fraisAttendus) {
		// Act
		BigDecimal frais = calcul.fraisMensuels(age, new BigDecimal("500"), false);

		// Assert
		assertThat(frais).isEqualByComparingTo(fraisAttendus);
	}

	// F4 : tenue offerte si le solde moyen atteint 1 000 €

	@ParameterizedTest(name = "solde moyen {0} -> frais {1}")
	@CsvSource({
			"999.99, 2.00",
			"1000, 0.00",
			"1000.00, 0.00",
			"1000.01, 0.00"
	})
	void shouldOfferAccountFeeFrom1000EurosAverageBalance(BigDecimal soldeMoyen, BigDecimal fraisAttendus) {
		// Act
		BigDecimal frais = calcul.fraisMensuels(40, soldeMoyen, false);

		// Assert
		assertThat(frais).isEqualByComparingTo(fraisAttendus);
	}

	// F5 : la carte premium ajoute 5,00 €, toujours dus

	@ParameterizedTest(name = "{0} ans, solde moyen {1}, premium -> frais {2}")
	@CsvSource({
			"40, 500, 7.00",
			"20, 500, 5.00",
			"40, 1000, 5.00",
			"20, 2000, 5.00"
	})
	void shouldAlwaysChargePremiumCardSupplement(int age, BigDecimal soldeMoyen, BigDecimal fraisAttendus) {
		// Act
		BigDecimal frais = calcul.fraisMensuels(age, soldeMoyen, true);

		// Assert
		assertThat(frais).isEqualByComparingTo(fraisAttendus);
	}

}
