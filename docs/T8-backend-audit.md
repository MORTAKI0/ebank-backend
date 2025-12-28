# Audit backend T8 (UC-1..UC-5 + RG_1..RG_15)

## Synthese rapide
- UC-1, UC-3, UC-4, UC-5 : conformes apres corrections mineures (messages/erreurs/ownership).
- UC-2 : conforme, avec fallback d'envoi email via logs (RG_7 partiel mais acceptable).

## UC (Use Cases)

| UC | Statut | Observations |
| --- | --- | --- |
| UC-1 Authentification | Conforme | Endpoints `/api/auth/login`, `/api/auth/change-password`, `/api/me` presentes. JWT obligatoire via filtre + SecurityConfig. Messages 401 alignes. Hypothese: `/api/me` reserve aux CLIENT (si AGENT requis, ajuster la regle). |
| UC-2 Ajout client | Conforme (RG_7 partiel) | Creation client ok, unicite identity/email, validations DTO. Email: fallback log si SMTP absent (documente). |
| UC-3 Nouveau compte | Conforme | Validation RIB + identityRef. Statut OPEN force en service + PrePersist. |
| UC-4 Dashboard client | Conforme | Ownership check, 10 dernieres operations, pagination, default account = dernier mouvement. |
| UC-5 Nouveau virement | Conforme | Ownership, RG_11/12, debit/credit, 2 transactions horodatees identiques. |

## RG (Regles de gestion)

| RG | Statut | Observations |
| --- | --- | --- |
| RG_1 MDP chiffres (BCrypt) | Conforme | `PasswordConfig` utilise BCrypt, creation client encodee. |
| RG_2 Login/MDP errones | Conforme | `AuthService` renvoie 401 avec "Login ou mot de passe errones". |
| RG_3 TTL JWT 1h | Conforme | `security.jwt.expiration-in-ms` par defaut 3600000. |
| RG_4 Identity unique | Conforme | Contrainte DB + verification service. |
| RG_5 Champs obligatoires client | Conforme | DTO validation `@NotBlank/@NotNull`. |
| RG_6 Email unique | Conforme | Contrainte DB + verification service. |
| RG_7 Envoi mail credentials | Partiel (fallback) | SMTP si configure, sinon fallback log (acceptable pour environnement de devoir). |
| RG_8 IdentityRef existe | Conforme | `AccountService` verifie client. |
| RG_9 RIB valide | Conforme | `RibValidator` applique validation. |
| RG_10 Statut compte OPEN | Conforme | Service force OPEN + `@PrePersist`. |
| RG_11 Source pas BLOCKED/CLOSED | Conforme | Verifie dans `TransferService`. |
| RG_12 Solde >= montant | Conforme | Comparaison `BigDecimal` avant debit. |
| RG_13 Debit source | Conforme | Mise a jour du solde source. |
| RG_14 Credit destination | Conforme | Mise a jour du solde destination. |
| RG_15 2 transactions meme timestamp | Conforme | Debit/Credit avec `createdAt` identique. |

## Corrections appliquees (minimales)
- Messages exacts RG_2/RG_3/403 alignes et exceptions mappees.
- Ownership en UC-4/UC-5 via `AccessDeniedException` (403 + message contractuel).
- Tests unitaires ajoutes pour login, token expire, ownership, solde insuffisant.

## Messages contractuels verifies
- 403: "Vous n’avez pas le droit d’accéder à cette fonctionnalité. Veuillez contacter votre administrateur"
- 401 token expire/invalide: "Session invalide, veuillez s’authentifier"
- 401 login/MDP: "Login ou mot de passe erronés"
