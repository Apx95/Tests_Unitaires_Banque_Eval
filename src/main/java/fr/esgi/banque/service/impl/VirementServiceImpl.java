package fr.esgi.banque.service.impl;

import fr.esgi.banque.business.Compte;
import fr.esgi.banque.business.CompteIntrouvableException;
import fr.esgi.banque.business.Virement;
import fr.esgi.banque.business.VirementRefuseException;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class VirementServiceImpl {

	static final BigDecimal SEUIL_ALERTE = new BigDecimal("10000");

	private final CompteRepository repository;
	private final NotificationService notifications;
	private final Clock horloge;

	public VirementServiceImpl(CompteRepository repository, NotificationService notifications, Clock horloge) {
		this.repository = repository;
		this.notifications = notifications;
		this.horloge = horloge;
	}

	public Virement virer(String numeroSource, String numeroDestination, BigDecimal montant) {
		verifierMontant(montant);
		verifierComptesDifferents(numeroSource, numeroDestination);
		LocalDate aujourdhui = LocalDate.now(horloge);
		verifierJourOuvert(aujourdhui);

		Compte source = trouverCompte(numeroSource);
		Compte destination = trouverCompte(numeroDestination);

		source.debiter(montant);
		destination.crediter(montant);
		repository.sauvegarder(source);
		repository.sauvegarder(destination);

		alerterSiMontantEleve(source, montant);
		return new Virement(numeroSource, numeroDestination, montant, aujourdhui);
	}

	private void verifierMontant(BigDecimal montant) {
		if (montant == null || montant.signum() <= 0) {
			throw new IllegalArgumentException("Le montant du virement doit être strictement positif");
		}
	}

	private void verifierComptesDifferents(String numeroSource, String numeroDestination) {
		if (numeroSource.equals(numeroDestination)) {
			throw new IllegalArgumentException("Le compte source et le compte destination doivent être différents");
		}
	}

	private void verifierJourOuvert(LocalDate date) {
		if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			throw new VirementRefuseException("Aucun virement n'est accepté le dimanche");
		}
	}

	private Compte trouverCompte(String numero) {
		return repository.trouver(numero)
				.orElseThrow(() -> new CompteIntrouvableException(numero));
	}

	private void alerterSiMontantEleve(Compte source, BigDecimal montant) {
		if (montant.compareTo(SEUIL_ALERTE) > 0) {
			notifications.alerter(source.getTitulaire(),
					"Virement de " + montant + " € depuis le compte " + source.getNumero());
		}
	}

}
