package pharmacie.rest;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pharmacie.dao.CategorieRepository;
import pharmacie.dao.MedicamentRepository;
import pharmacie.dto.MedicamentCreateDTO;
import pharmacie.entity.Categorie;
import pharmacie.entity.Medicament;

/**
 * Contrôleur personnalisé pour gérer les médicaments et le mappage du categorieCode
 */
@RestController
@RequestMapping(path = "/api/medicaments")
@Slf4j
public class MedicamentController {
    private final MedicamentRepository medicamentDao;
    private final CategorieRepository categorieDao;

    public MedicamentController(MedicamentRepository medicamentDao, CategorieRepository categorieDao) {
        this.medicamentDao = medicamentDao;
        this.categorieDao = categorieDao;
    }

    /**
     * GET - Liste tous les médicaments
     */
    @GetMapping
    public List<Medicament> getAllMedicaments() {
        return medicamentDao.findAll();
    }

    /**
     * POST - Crée un nouveau médicament avec mappage du categorieCode vers Categorie
     */
    @PostMapping
    public ResponseEntity<?> createMedicament(@RequestBody MedicamentCreateDTO dto) {
        try {
            log.info("Création d'un médicament avec les données: {}", dto);

            // Récupérer la catégorie par son code
            if (dto.getCategorieCode() == null) {
                return ResponseEntity.badRequest()
                        .body("L'erreur: categorieCode est obligatoire");
            }

            Categorie categorie = categorieDao.findById(dto.getCategorieCode())
                    .orElseThrow(() -> new RuntimeException(
                            "Catégorie non trouvée avec le code: " + dto.getCategorieCode()));

            // Créer le médicament
            Medicament medicament = new Medicament();
            medicament.setNom(dto.getNom());
            medicament.setQuantiteParUnite(dto.getQuantiteParUnite());
            medicament.setPrixUnitaire(BigDecimal.valueOf(dto.getPrixUnitaire()));
            medicament.setUnitesEnStock(dto.getUnitesEnStock());
            medicament.setUnitesCommandees(dto.getUnitesCommandees());
            medicament.setNiveauDeReappro(dto.getNiveauDeReappro());
            medicament.setIndisponible(dto.isIndisponible());
            medicament.setImageURL(dto.getImageURL());
            medicament.setCategorie(categorie);

            // Sauvegarder
            Medicament saved = medicamentDao.save(medicament);
            log.info("Médicament créé avec succès: {}", saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Erreur lors de la création du médicament", e);
            return ResponseEntity.badRequest()
                    .body("Erreur: " + e.getMessage());
        }
    }
}
