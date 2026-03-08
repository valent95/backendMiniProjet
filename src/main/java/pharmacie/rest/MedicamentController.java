package pharmacie.rest;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
            log.info("  - categorieCode reçu: {}", dto.getCategorieCode());

            // Récupérer la catégorie par son code
            if (dto.getCategorieCode() == null) {
                log.error("categorieCode est null! DTO complet: {}", dto);
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
    
    //permet d'enregidstrer les modifications à un médicament
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedicament(@PathVariable Integer id, @RequestBody MedicamentCreateDTO dto) {
        try {
            log.info("Modification du médicament réf: {} avec les données: {}", id, dto);

            // 1. Vérifier si le médicament existe déjà en base
            Medicament existingMedicament = medicamentDao.findById(id)
                    .orElseThrow(() -> new RuntimeException("Médicament non trouvé avec la référence: " + id));

            // 2. Vérifier et récupérer la nouvelle catégorie par son code
            if (dto.getCategorieCode() == null) {
                log.error("categorieCode est null lors de la modification !");
                return ResponseEntity.badRequest()
                        .body("L'erreur: categorieCode est obligatoire");
            }

            Categorie categorie = categorieDao.findById(dto.getCategorieCode())
                    .orElseThrow(() -> new RuntimeException(
                            "Catégorie non trouvée avec le code: " + dto.getCategorieCode()));

            // 3. Mettre à jour tous les champs du médicament existant
            existingMedicament.setNom(dto.getNom());
            existingMedicament.setQuantiteParUnite(dto.getQuantiteParUnite());
            existingMedicament.setPrixUnitaire(BigDecimal.valueOf(dto.getPrixUnitaire()));
            existingMedicament.setUnitesEnStock(dto.getUnitesEnStock());
            existingMedicament.setUnitesCommandees(dto.getUnitesCommandees());
            existingMedicament.setNiveauDeReappro(dto.getNiveauDeReappro());
            existingMedicament.setIndisponible(dto.isIndisponible());
            existingMedicament.setImageURL(dto.getImageURL());
            
            // On écrase l'ancienne catégorie par la nouvelle
            existingMedicament.setCategorie(categorie);

            // 4. Sauvegarder en base de données
            Medicament updated = medicamentDao.save(existingMedicament);
            log.info("Médicament modifié avec succès: {}", updated);

            return ResponseEntity.ok(updated);
            
        } catch (Exception e) {
            log.error("Erreur lors de la modification du médicament", e);
            return ResponseEntity.badRequest()
                    .body("Erreur: " + e.getMessage());
        }
    }
}
