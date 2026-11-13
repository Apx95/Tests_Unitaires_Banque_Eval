package fr.esgi.banque.service.impl;

import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@ExtendWith(MockitoExtension.class)
class VirementServiceImplTest {

	@Mock
	private CompteRepository repository;

	@Mock
	private NotificationService notifications;

	// V1 : montant null, nul ou négatif refusé

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"0", "0.00", "-0.01", "-100"})
	void shouldRejectAmountNotStrictlyPositive(BigDecimal montant) {
		// Arrange
		Clock horloge = Clock.fixed(Instant.parse("2026-11-11T10:00:00Z"), ZoneId.of("Europe/Paris"));
		VirementServiceImpl service = new VirementServiceImpl(repository, notifications, horloge);

		// Act + Assert
		assertThatIllegalArgumentException()
				.isThrownBy(() -> service.virer("FR001", "FR002", montant));
	}

}
