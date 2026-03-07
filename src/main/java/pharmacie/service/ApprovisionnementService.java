package pharmacie.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pharmacie.dao.MedicamentRepository;
import pharmacie.dao.SupplierRepository;
import pharmacie.entity.Categorie;
import pharmacie.entity.Medicament;
import pharmacie.entity.Supplier;

@Slf4j
@Service
public class ApprovisionnementService {

    private final MedicamentRepository medicamentDao;
    private final SupplierRepository supplierDao;
    private final JavaMailSender mailSender;

    public ApprovisionnementService(MedicamentRepository medicamentDao, SupplierRepository supplierDao,
            JavaMailSender mailSender) {
        this.medicamentDao = medicamentDao;
        this.supplierDao = supplierDao;
        this.mailSender = mailSender;
    }

    /**
     * Service métier d'approvisionnement :
     * - Trouve les médicaments à réapprovisionner (unitesEnStock < niveauDeReappro)
     * - Envoie un mail personnalisé à chaque fournisseur susceptible de les fournir
     * - Le mail récapitule, catégorie par catégorie, tous les médicaments à réapprovisionner
     *
     * @return Un message récapitulatif des actions effectuées
     */
    @Transactional(readOnly = true)
    public String executerApprovisionnement() {
        log.info("Service : Lancement du processus d'approvisionnement");

        // 1. Trouver tous les médicaments à réapprovisionner
        List<Medicament> medicamentsAReapprovisionner = medicamentDao.findAll()
                .stream()
                .filter(med -> med.getUnitesEnStock() < med.getNiveauDeReappro() && !med.isIndisponible())
                .collect(Collectors.toList());

        if (medicamentsAReapprovisionner.isEmpty()) {
            log.info("Aucun médicament à réapprovisionner");
            return "Aucun médicament n'a besoin d'être réapprovisionné";
        }

        log.info("Nombre de médicaments à réapprovisionner : {}", medicamentsAReapprovisionner.size());

        // 2. Récupérer tous les fournisseurs
        List<Supplier> suppliers = supplierDao.findAll();

        // 3. Pour chaque fournisseur, déterminer les médicaments qu'il peut fournir
        // et lui envoyer un mail
        int nbMailsEnvoyes = 0;
        for (Supplier supplier : suppliers) {
            // Récupérer les catégories que ce fournisseur peut fournir
            List<Categorie> categories = supplier.getCategories();

            // Filtrer les médicaments à réapprovisionner pour ce fournisseur
            Map<String, List<Medicament>> medicamentsParCategorie = new HashMap<>();
            for (Medicament med : medicamentsAReapprovisionner) {
                if (categories.contains(med.getCategorie())) {
                    medicamentsParCategorie
                            .computeIfAbsent(med.getCategorie().getLibelle(), k -> new java.util.LinkedList<>())
                            .add(med);
                }
            }

            // Si ce fournisseur peut fournir au moins un médicament, lui envoyer un mail
            if (!medicamentsParCategorie.isEmpty()) {
                envoyerMailApprovisionnement(supplier, medicamentsParCategorie);
                nbMailsEnvoyes++;
            }
        }

        String message = String.format("Approvisionnement effectué : %d mail(s) envoyé(s)", nbMailsEnvoyes);
        log.info(message);
        return message;
    }

    /**
     * Envoie un mail de demande de devis de réapprovisionnement à un fournisseur
     *
     * @param supplier               Le fournisseur destinataire
     * @param medicamentsParCategorie Les médicaments à réapprovisionner, groupés par catégorie
     */
    private void envoyerMailApprovisionnement(Supplier supplier, Map<String, List<Medicament>> medicamentsParCategorie) {
        log.debug("Envoi du mail de réapprovisionnement à {}", supplier.getEmail());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(supplier.getEmail());
        message.setSubject("Demande de devis de réapprovisionnement en médicaments");

        // Construire le corps du mail
        StringBuilder body = new StringBuilder();
        body.append("Bonjour ").append(supplier.getNom()).append(",\n\n");
        body.append("Nous vous demandons de nous transmettre un devis pour les médicaments suivants :\n\n");

        // Afficher les médicaments par catégorie
        for (Map.Entry<String, List<Medicament>> entry : medicamentsParCategorie.entrySet()) {
            body.append("Catégorie : ").append(entry.getKey()).append("\n");
            body.append("-----------------------------------\n");
            for (Medicament med : entry.getValue()) {
                body.append("  - ").append(med.getNom()).append(" (Ref: ").append(med.getReference()).append(")\n");
                body.append("    Stock actuel: ").append(med.getUnitesEnStock()).append(" unités\n");
                body.append("    Niveau minimum: ").append(med.getNiveauDeReappro()).append(" unités\n");
                body.append("    À réapprovisionner: ").append(med.getNiveauDeReappro() - med.getUnitesEnStock())
                        .append(" unités\n\n");
            }
        }

        body.append("Merci de nous envoyer votre meilleur tarif dans les plus brefs délais.\n\n");
        body.append("Cordialement,\nL'équipe Pharmacie");

        message.setText(body.toString());

        try {
            mailSender.send(message);
            log.info("Mail envoyé avec succès à {}", supplier.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du mail à {}: {}", supplier.getEmail(), e.getMessage());
        }
    }
}
