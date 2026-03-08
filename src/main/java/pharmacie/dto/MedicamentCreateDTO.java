package pharmacie.dto;

import lombok.Data;

/**
 * DTO pour créer un médicament avec le code de catégorie au lieu de l'objet Categorie
 */
@Data
public class MedicamentCreateDTO {
    private Integer categorieCode;  // Code de la catégorie
    private String nom;
    private String quantiteParUnite;
    private double prixUnitaire;
    private int unitesEnStock;
    private int unitesCommandees;
    private int niveauDeReappro;
    private boolean indisponible;
    private String imageURL;
}
