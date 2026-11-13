# Rendu : tests unitaires et TDD

- **Nom** : PASSY BASSOUAMINA
- **PrÃ©nom** : Premice Axel
- **Promotion** : B3
- **Campus** : ESGI Reims

## Partie A : dÃ©fauts trouvÃ©s

| RÃ¨gle   | Description du dÃ©faut  | Test qui le prouve                                                                           |
|---------|------------------------|----------------------------------------------------------------------------------------------|
| C3      | `crediter` acceptait un montant nul (`signum() < 0` au lieu de `<= 0`). | `CompteTest.shouldRejectCreditWhenAmountIsNotStrictlyPositive` (cas `0` et `0.00`)           |
| C5      | Un dÃ©bit qui amenait le solde exactement Ã  `-dÃ©couvertAutorisÃ©` Ã©tait refusÃ© (`<= 0` au lieu de `< 0`). | `CompteTest.shouldAcceptDebitUpToOverdraftLimit` (cas `300.00 -> -200.00`)                   |
| C6      | `estVide()` utilisait `equals`, qui compare aussi l'Ã©chelle : un solde de `0.00` n'Ã©tait pas considÃ©rÃ© comme vide. RemplacÃ© par `compareTo`. | `CompteTest.shouldBeEmptyOnlyWhenBalanceIsZero` (cas `0.00`)                                 |
| F3      | La tenue de compte Ã©tait offerte jusqu'Ã  26 ans inclus (`<=` au lieu de `<`). | `CalculFraisTest.shouldOfferAccountFeeOnlyUnder26` (cas `26 ans -> 2.00`)                    |
| F4      | La tenue n'Ã©tait offerte qu'au-dessus de 1 000 â‚¬ (`> 0` au lieu de `>= 0`) : un solde moyen d'exactement 1 000 â‚¬ payait les frais. | `CalculFraisTest.shouldOfferAccountFeeFrom1000EurosAverageBalance` (cas `1000` et `1000.00`) |
| F5      | Quand la tenue Ã©tait offerte, la mÃ©thode renvoyait `0` directement et oubliait le supplÃ©ment premium de 5,00 â‚¬. | `CalculFraisTest.shouldAlwaysChargePremiumCardSupplement` (cas `20 ans` et `solde 1000`)     |

Les rÃ¨gles C1, C2, C4, C7, F1 et F2 Ã©taient respectÃ©es : leurs tests passent sans correction.

## Couverture de code (IntelliJ, Run with Coverage)

![Couverture](coverage.png)

## Partie B : rÃ¨gles non terminÃ©es

Aucune : les rÃ¨gles V1 Ã  V7 sont traitÃ©es en TDD (cycles `red:` / `green:` / `refactor:` dans l'historique git).
